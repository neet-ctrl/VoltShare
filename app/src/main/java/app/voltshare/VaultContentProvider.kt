package app.voltshare

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.FileNotFoundException

class VaultContentProvider : ContentProvider() {
    private val vault: VaultRepository
        get() = VaultRepository(requireNotNull(context))

    override fun onCreate(): Boolean = context != null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        val file = resolveFile(uri) ?: return null
        val columns = projection?.toList().orEmpty().ifEmpty {
            listOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE, "mime_type")
        }
        return MatrixCursor(columns.toTypedArray()).apply {
            addRow(columns.map { column ->
                when (column) {
                    OpenableColumns.DISPLAY_NAME -> file.name
                    OpenableColumns.SIZE -> file.sizeBytes
                    "mime_type" -> file.mimeType
                    else -> null
                }
            }.toTypedArray())
        }
    }

    override fun getType(uri: Uri): String? = resolveFile(uri)?.mimeType

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r" && mode != "rw") {
            throw FileNotFoundException("VoltShare vault is read-only")
        }
        checkUnlocked()
        val file = resolveFile(uri) ?: throw FileNotFoundException("Vault file not found")
        return ParcelFileDescriptor.open(
            vault.storedFile(file),
            ParcelFileDescriptor.MODE_READ_ONLY,
        )
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("VoltShare vault is read-only")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("VoltShare vault is read-only")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = throw UnsupportedOperationException("VoltShare vault is read-only")

    private fun resolveFile(uri: Uri): VaultFile? {
        checkUnlocked()
        if (uri.authority != VaultShareContract.AUTHORITY) return null
        if (uri.pathSegments.size != 2 || uri.pathSegments.firstOrNull() != VaultShareContract.PATH_FILES) return null
        val id = uri.pathSegments.last()
        return vault.listFiles().firstOrNull { it.id == id && vault.storedFile(it).isFile }
    }

    private fun checkUnlocked() {
        check(VaultSession.unlocked) { "VoltShare vault is locked" }
    }
}