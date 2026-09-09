package app.voltshare

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class VaultFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val locked: Boolean,
)

class VaultRepository(private val context: Context) {
    private val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
    private val metadataFile = File(vaultDir, "index.json")
    private val viewCacheDir = File(context.cacheDir, "view-cache").apply { mkdirs() }

    fun listFiles(): List<VaultFile> {
        if (!metadataFile.exists()) return emptyList()
        return runCatching {
            val array = JSONArray(metadataFile.readText())
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        VaultFile(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            mimeType = item.optString("mimeType", "application/octet-stream"),
                            sizeBytes = item.optLong("sizeBytes", 0),
                            locked = item.optBoolean("locked", false),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun importUri(uri: Uri): VaultFile? {
        val resolver = context.contentResolver
        val displayName = queryDisplayName(uri) ?: "untitled-${System.currentTimeMillis()}"
        val mimeType = resolver.getType(uri) ?: mimeFromName(displayName)
        val id = UUID.randomUUID().toString()
        val encryptedFile = File(vaultDir, "$id.vault")
        var size = 0L
        return runCatching {
            resolver.openInputStream(uri)?.use { input ->
                size = encrypt(input, encryptedFile)
            } ?: return null

            val record = VaultFile(id, displayName, mimeType, size, locked = false)
            writeFiles(listFiles() + record)
            record
        }.getOrNull()
    }

    fun importIncoming(name: String, mimeType: String, input: InputStream, size: Long): VaultFile? {
        val id = UUID.randomUUID().toString()
        val encryptedFile = File(vaultDir, "$id.vault")
        return runCatching {
            encrypt(input, encryptedFile)
            val record = VaultFile(id, name.sanitizeName(), mimeType, size, locked = false)
            writeFiles(listFiles() + record)
            record
        }.getOrNull()
    }

    fun toggleLocked(file: VaultFile): VaultFile {
        val updated = file.copy(locked = !file.locked)
        writeFiles(listFiles().map { if (it.id == file.id) updated else it })
        return updated
    }

    fun prepareViewing(file: VaultFile): File? {
        val source = File(vaultDir, "${file.id}.vault")
        if (!source.exists()) return null
        val safeName = file.name.sanitizeName()
        val output = File(viewCacheDir, "${file.id}-$safeName")
        return runCatching {
            decrypt(source, output)
            output
        }.getOrNull()
    }

    fun encryptedFile(file: VaultFile): File = File(vaultDir, "${file.id}.vault")

    fun clearViewCache() {
        viewCacheDir.listFiles()?.forEach { it.delete() }
    }

    private fun writeFiles(files: List<VaultFile>) {
        val array = JSONArray()
        files.forEach { file ->
            array.put(
                JSONObject()
                    .put("id", file.id)
                    .put("name", file.name)
                    .put("mimeType", file.mimeType)
                    .put("sizeBytes", file.sizeBytes)
                    .put("locked", file.locked),
            )
        }
        metadataFile.writeText(array.toString())
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) it.getString(index) else null
        }
    }

    private fun encrypt(input: InputStream, destination: File): Long {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        var count = 0L
        FileOutputStream(destination).use { output ->
            output.write(cipher.iv)
            CipherOutputStream(output, cipher).use { encrypted ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    encrypted.write(buffer, 0, read)
                    count += read
                }
            }
        }
        return count
    }

    private fun decrypt(source: File, destination: File) {
        FileInputStream(source).use { input ->
            val iv = ByteArray(12)
            input.read(iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            CipherInputStream(input, cipher).use { decrypted ->
                FileOutputStream(destination).use { output -> decrypted.copyTo(output) }
            }
        }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private fun mimeFromName(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg", "png", "webp", "gif", "heic" -> "image/*"
        "mp4", "mkv", "webm", "mov", "avi" -> "video/*"
        "mp3", "wav", "m4a", "flac" -> "audio/*"
        "pdf" -> "application/pdf"
        "apk", "xapk", "apks" -> "application/vnd.android.package-archive"
        "txt", "md", "json", "xml", "csv", "log", "kt", "java", "js", "ts", "html", "css" -> "text/plain"
        else -> "application/octet-stream"
    }

    private fun String.sanitizeName(): String =
        replace(Regex("[^A-Za-z0-9._ -]"), "_").take(120).ifBlank { "shared-file" }

    companion object {
        private const val KEY_ALIAS = "voltshare-vault-key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}