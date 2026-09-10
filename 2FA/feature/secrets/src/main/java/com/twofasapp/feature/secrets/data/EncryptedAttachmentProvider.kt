package com.twofasapp.feature.secrets.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.Executors

/**
 * Streams an encrypted Secret attachment directly to an explicitly selected
 * external viewer without creating a plaintext temporary file on disk.
 */
class EncryptedAttachmentProvider : ContentProvider() {

    private val executor = Executors.newCachedThreadPool()

    private val attachmentDirectory: File
        get() = File(requireNotNull(context).filesDir, ATTACHMENT_DIRECTORY)

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(requireNotNull(context))
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String =
        uri.getQueryParameter(QUERY_MIME_TYPE)?.takeIf(String::isNotBlank)
            ?: "application/octet-stream"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Secret attachments are read-only")
        val file = resolveAttachment(uri)
        if (!file.isFile) throw FileNotFoundException("Attachment is unavailable")

        val pipe = ParcelFileDescriptor.createPipe()
        executor.execute {
            ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { output ->
                runCatching {
                    encryptedFile(file).openFileInput().use { input -> input.copyTo(output) }
                }
            }
        }
        return pipe[0]
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        val file = resolveAttachment(uri)
        if (!file.isFile) return null

        val columns = projection?.toList()?.toTypedArray()
            ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns).apply {
            addRow(columns.map { column ->
                when (column) {
                    OpenableColumns.DISPLAY_NAME ->
                        uri.getQueryParameter(QUERY_DISPLAY_NAME) ?: file.name
                    OpenableColumns.SIZE ->
                        uri.getQueryParameter(QUERY_SIZE)?.toLongOrNull() ?: -1L
                    else -> null
                }
            }.toTypedArray())
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    private fun resolveAttachment(uri: Uri): File {
        val storageName = uri.pathSegments.singleOrNull()
            ?: throw FileNotFoundException("Invalid Secret attachment URI")
        val directory = attachmentDirectory
        val file = File(directory, storageName)
        if (storageName.isBlank() || file.name != storageName ||
            file.canonicalFile.parentFile != directory.canonicalFile
        ) {
            throw FileNotFoundException("Invalid Secret attachment")
        }
        return file
    }

    private fun encryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            requireNotNull(context),
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()

    companion object {
        private const val ATTACHMENT_DIRECTORY = "secrets_attachments"
        private const val QUERY_DISPLAY_NAME = "display_name"
        private const val QUERY_MIME_TYPE = "mime_type"
        private const val QUERY_SIZE = "size"

        fun uriFor(context: android.content.Context, attachment: SecretAttachment): Uri {
            require(attachment.storageName.isNotBlank()) { "Attachment is not stored in the encrypted vault" }
            return Uri.Builder()
                .scheme("content")
                .authority("${context.packageName}.secret-attachments")
                .appendPath(attachment.storageName)
                .appendQueryParameter(QUERY_DISPLAY_NAME, attachment.name)
                .appendQueryParameter(QUERY_MIME_TYPE, attachment.mimeType)
                .appendQueryParameter(QUERY_SIZE, attachment.size.toString())
                .build()
        }
    }
}