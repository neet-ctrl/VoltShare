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
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class TransferDirection(val label: String) {
    LOCAL("Local"),
    SENT("Sent"),
    RECEIVED("Received"),
}

data class VaultFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val locked: Boolean,
    val folderPath: String = "/",
    val createdAt: Long = System.currentTimeMillis(),
    val order: Long = createdAt,
    val transferDirection: TransferDirection = TransferDirection.LOCAL,
    val contentHash: String? = null,
)

enum class VaultSort(val label: String) {
    CUSTOM("Custom order"),
    NAME_ASC("Name A–Z"),
    NAME_DESC("Name Z–A"),
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    LARGEST("Largest first"),
    SMALLEST("Smallest first"),
    TYPE("File type"),
}

class VaultRepository(private val context: Context) {
    private val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
    private val metadataFile = File(vaultDir, "index.json")
    private val viewCacheDir = File(context.cacheDir, "view-cache").apply { mkdirs() }
    private val preferences = context.getSharedPreferences("voltshare-vault", Context.MODE_PRIVATE)

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
                            folderPath = item.optString("folderPath", "/").ifBlank { "/" },
                            createdAt = item.optLong("createdAt", 0L).takeIf { it > 0 } ?: System.currentTimeMillis(),
                            order = item.optLong("order", index.toLong()),
                            transferDirection = runCatching {
                                TransferDirection.valueOf(item.optString("transferDirection", TransferDirection.LOCAL.name))
                            }.getOrDefault(TransferDirection.LOCAL),
                            contentHash = item.optString("contentHash", "").ifBlank { null },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun importUri(uri: Uri, folderPath: String = primaryFolder()): VaultFile? {
        val resolver = context.contentResolver
        val displayName = queryDisplayName(uri) ?: "untitled-${System.currentTimeMillis()}"
        val mimeType = resolver.getType(uri) ?: mimeFromName(displayName)
        val id = UUID.randomUUID().toString()
        val encryptedFile = File(vaultDir, "$id.vault")
        var encoded: EncodedFile? = null
        return runCatching {
            resolver.openInputStream(uri)?.use { input ->
                encoded = encrypt(input, encryptedFile)
            } ?: return null

            val now = System.currentTimeMillis()
            val requestedFolder = normalizeFolderPath(folderPath)
            val targetFolder = if (requestedFolder == "/" && primaryFolder() != "/") primaryFolder() else requestedFolder
            ensureFolderPath(targetFolder)
            val existing = encoded?.sha256?.let(::findFileByHash)
            if (existing != null) {
                encryptedFile.delete()
                return existing
            }
            val record = VaultFile(
                id = id,
                name = displayName,
                mimeType = mimeType,
                sizeBytes = encoded?.sizeBytes ?: 0L,
                locked = false,
                folderPath = targetFolder,
                createdAt = now,
                order = now,
                transferDirection = TransferDirection.LOCAL,
                contentHash = encoded?.sha256,
            )
            writeFiles(listFiles() + record)
            record
        }.getOrNull()
    }

    fun importGeneratedFile(
        source: File,
        displayName: String,
        mimeType: String,
        folderPath: String = primaryFolder(),
    ): VaultFile? {
        return runCatching {
            source.inputStream().use { input ->
                importStream(input, displayName, mimeType, folderPath)
            }
        }.getOrNull()
    }

    fun createTextFile(
        text: String,
        displayName: String = "VoltShare-note-${System.currentTimeMillis()}.txt",
        folderPath: String = primaryFolder(),
    ): VaultFile? {
        return runCatching {
            ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)).use { input ->
                importStream(input, displayName, "text/plain", folderPath)
            }
        }.getOrNull()
    }

    fun importIncoming(
        name: String,
        mimeType: String,
        input: InputStream,
        size: Long,
        expectedSha256: String? = null,
        folderPath: String = primaryFolder(),
    ): VaultFile? {
        val id = UUID.randomUUID().toString()
        val encryptedFile = File(vaultDir, "$id.vault.tmp")
        return runCatching {
            val digest = encryptExact(input, encryptedFile, size)
            check(expectedSha256 == null || digest.equals(expectedSha256, ignoreCase = true)) { "Transfer checksum mismatch" }
            val existing = findFileByHash(digest)
            if (existing != null) {
                encryptedFile.delete()
                return existing
            }
            val finalFile = File(vaultDir, "$id.vault")
            check(encryptedFile.renameTo(finalFile)) { "Could not commit received file" }
            val now = System.currentTimeMillis()
            val requestedFolder = normalizeFolderPath(folderPath)
            val targetFolder = if (requestedFolder == "/" && primaryFolder() != "/") primaryFolder() else requestedFolder
            ensureFolderPath(targetFolder)
            val record = VaultFile(
                id = id,
                name = name.sanitizeName(),
                mimeType = mimeType,
                sizeBytes = size,
                locked = false,
                folderPath = targetFolder,
                createdAt = now,
                order = now,
                transferDirection = TransferDirection.RECEIVED,
                contentHash = digest,
            )
            writeFiles(listFiles() + record)
            record
        }.onFailure { encryptedFile.delete() }.getOrNull()
    }

    fun toggleLocked(file: VaultFile): VaultFile {
        val updated = file.copy(locked = !file.locked)
        writeFiles(listFiles().map { if (it.id == file.id) updated else it })
        return updated
    }

    fun markSent(file: VaultFile): VaultFile {
        val updated = file.copy(transferDirection = TransferDirection.SENT)
        writeFiles(listFiles().map { if (it.id == file.id) updated else it })
        return updated
    }

    fun renameFile(file: VaultFile, name: String): VaultFile? {
        val cleanName = name.sanitizeName()
        if (cleanName.isBlank()) return null
        val updated = file.copy(name = cleanName)
        writeFiles(listFiles().map { if (it.id == file.id) updated else it })
        return updated
    }

    fun deleteFile(file: VaultFile): Boolean {
        val existed = listFiles().any { it.id == file.id }
        val deleted = encryptedFile(file).delete()
        val remaining = listFiles().filterNot { it.id == file.id }
        writeFiles(remaining)
        return existed || deleted
    }

    fun setPrimaryFolder(path: String): String {
        val normalized = normalizeFolderPath(path)
        ensureFolderPath(normalized)
        preferences.edit().putString(PRIMARY_FOLDER_KEY, normalized).apply()
        return normalized
    }

    fun primaryFolder(): String = normalizeFolderPath(preferences.getString(PRIMARY_FOLDER_KEY, "/") ?: "/")

    fun moveFile(file: VaultFile, folderPath: String): VaultFile {
        val targetFolder = normalizeFolderPath(folderPath)
        ensureFolderPath(targetFolder)
        val updated = file.copy(folderPath = targetFolder)
        writeFiles(listFiles().map { if (it.id == file.id) updated else it })
        return updated
    }

    fun createFolder(name: String, parentPath: String = "/"): String? {
        val cleanName = name.cleanFolderName()
        if (cleanName.isBlank()) return null
        val path = if (parentPath == "/") "/$cleanName" else "$parentPath/$cleanName"
        val folders = listFolders().toMutableSet()
        if (!folders.add(path)) return null
        writeFolders(folders.toList())
        return path
    }

    fun renameFolder(path: String, name: String): String? {
        if (path == "/" || path !in listFolders()) return null
        val cleanName = name.cleanFolderName()
        if (cleanName.isBlank()) return null
        val parent = path.substringBeforeLast('/', "")
        val newPath = if (parent.isBlank()) "/$cleanName" else "$parent/$cleanName"
        if (newPath in listFolders()) return null
        val prefix = "$path/"
        writeFiles(
            listFiles().map {
                when {
                    it.folderPath == path -> it.copy(folderPath = newPath)
                    it.folderPath.startsWith(prefix) -> it.copy(folderPath = newPath + it.folderPath.removePrefix(path))
                    else -> it
                }
            },
        )
        writeFolders(listFolders().map { folder ->
            when {
                folder == path -> newPath
                folder.startsWith(prefix) -> newPath + folder.removePrefix(path)
                else -> folder
            }
        })
        if (primaryFolder() == path || primaryFolder().startsWith("$path/")) {
            preferences.edit().putString(PRIMARY_FOLDER_KEY, newPath + primaryFolder().removePrefix(path)).apply()
        }
        return newPath
    }

    fun deleteFolder(path: String): Boolean {
        if (path == "/" || path !in listFolders()) return false
        val prefix = "$path/"
        val updatedFiles = listFiles().map {
            if (it.folderPath == path || it.folderPath.startsWith(prefix)) it.copy(folderPath = "/") else it
        }
        writeFiles(updatedFiles)
        writeFolders(listFolders().filterNot { it == path || it.startsWith(prefix) })
        if (primaryFolder() == path || primaryFolder().startsWith(prefix)) {
            val parent = path.substringBeforeLast('/', "")
            preferences.edit().putString(PRIMARY_FOLDER_KEY, if (parent.isBlank()) "/" else parent).apply()
        }
        return true
    }

    fun listFolders(): List<String> {
        if (!foldersFile.exists()) return listOf("/")
        return runCatching {
            val array = JSONArray(foldersFile.readText())
            buildList {
                add("/")
                for (index in 0 until array.length()) {
                    val path = array.optString(index)
                    if (path.isNotBlank() && path != "/") add(path)
                }
            }.distinct().sortedWith(compareBy({ it.count { char -> char == '/' } }, { it.lowercase() }))
        }.getOrDefault(listOf("/"))
    }

    fun reorder(file: VaultFile, direction: Int): List<VaultFile> {
        val siblings = listFiles().filter { it.folderPath == file.folderPath }.sortedBy { it.order }.toMutableList()
        val index = siblings.indexOfFirst { it.id == file.id }
        val target = index + direction
        if (index < 0 || target !in siblings.indices) return siblings
        val current = siblings[index]
        siblings[index] = siblings[target]
        siblings[target] = current
        val normalized = siblings.mapIndexed { position, item -> item.copy(order = position.toLong()) }
        val ids = normalized.associateBy { it.id }
        writeFiles(listFiles().map { ids[it.id] ?: it })
        return normalized
    }

    fun reorderTo(file: VaultFile, targetIndex: Int): List<VaultFile> {
        val siblings = listFiles().filter { it.folderPath == file.folderPath }.sortedBy { it.order }.toMutableList()
        val currentIndex = siblings.indexOfFirst { it.id == file.id }
        if (currentIndex < 0 || targetIndex !in siblings.indices || currentIndex == targetIndex) return siblings
        val item = siblings.removeAt(currentIndex)
        siblings.add(targetIndex.coerceIn(0, siblings.size), item)
        val normalized = siblings.mapIndexed { position, entry -> entry.copy(order = position.toLong()) }
        val ids = normalized.associateBy { it.id }
        writeFiles(listFiles().map { ids[it.id] ?: it })
        return normalized
    }

    fun deleteFiles(files: Collection<VaultFile>) {
        val ids = files.map { it.id }.toSet()
        files.forEach { encryptedFile(it).delete() }
        writeFiles(listFiles().filterNot { it.id in ids })
    }

    fun sort(files: List<VaultFile>, sort: VaultSort): List<VaultFile> = when (sort) {
        VaultSort.CUSTOM -> files.sortedBy { it.order }
        VaultSort.NAME_ASC -> files.sortedBy { it.name.lowercase() }
        VaultSort.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
        VaultSort.NEWEST -> files.sortedByDescending { it.createdAt }
        VaultSort.OLDEST -> files.sortedBy { it.createdAt }
        VaultSort.LARGEST -> files.sortedByDescending { it.sizeBytes }
        VaultSort.SMALLEST -> files.sortedBy { it.sizeBytes }
        VaultSort.TYPE -> files.sortedWith(compareBy({ it.mimeType }, { it.name.lowercase() }))
    }

    fun openDecrypted(file: VaultFile): InputStream? {
        val source = File(vaultDir, "${file.id}.vault")
        if (!source.exists()) return null
        return runCatching {
            val input = FileInputStream(source)
            val iv = ByteArray(12)
            check(input.read(iv) == iv.size) { "Invalid encrypted file" }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            CipherInputStream(input, cipher)
        }.getOrNull()
    }

    fun sha256(file: VaultFile): String? {
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            openDecrypted(file)?.use { input ->
                val buffer = ByteArray(1024 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            } ?: return null
            digest.digest().joinToString("") { "%02x".format(it) }
        }.getOrNull()
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

    private fun importStream(
        input: InputStream,
        displayName: String,
        mimeType: String,
        folderPath: String,
    ): VaultFile {
        val id = UUID.randomUUID().toString()
        val encryptedFile = File(vaultDir, "$id.vault")
        return try {
            val encoded = encrypt(input, encryptedFile)
            val now = System.currentTimeMillis()
            val requestedFolder = normalizeFolderPath(folderPath)
            val targetFolder = if (requestedFolder == "/" && primaryFolder() != "/") primaryFolder() else requestedFolder
            ensureFolderPath(targetFolder)
            val existing = findFileByHash(encoded.sha256)
            if (existing != null) {
                encryptedFile.delete()
                return existing
            }
            val record = VaultFile(
                id = id,
                name = displayName.sanitizeName(),
                mimeType = mimeType,
                sizeBytes = encoded.sizeBytes,
                locked = false,
                folderPath = targetFolder,
                createdAt = now,
                order = now,
                contentHash = encoded.sha256,
            )
            writeFiles(listFiles() + record)
            record
        } catch (error: Throwable) {
            encryptedFile.delete()
            throw error
        }
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
                    .put("locked", file.locked)
                    .put("folderPath", file.folderPath)
                     .put("createdAt", file.createdAt)
                     .put("order", file.order)
                     .put("transferDirection", file.transferDirection.name)
                     .put("contentHash", file.contentHash ?: JSONObject.NULL),
            )
        }
        metadataFile.writeText(array.toString())
    }

    private fun findFileByHash(hash: String): VaultFile? {
        return listFiles().firstOrNull { file ->
            file.contentHash?.equals(hash, ignoreCase = true) == true ||
                (file.contentHash == null && sha256(file)?.equals(hash, ignoreCase = true) == true)
        }
    }

    private fun writeFolders(folders: List<String>) {
        val array = JSONArray()
        folders.filter { it != "/" }.distinct().forEach(array::put)
        foldersFile.writeText(array.toString())
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) it.getString(index) else null
        }
    }

    private data class EncodedFile(val sizeBytes: Long, val sha256: String)

    private fun encrypt(input: InputStream, destination: File): EncodedFile {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val digest = MessageDigest.getInstance("SHA-256")
        var count = 0L
        FileOutputStream(destination).use { output ->
            output.write(cipher.iv)
            CipherOutputStream(output, cipher).use { encrypted ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    encrypted.write(buffer, 0, read)
                    digest.update(buffer, 0, read)
                    count += read
                }
            }
        }
        return EncodedFile(count, digest.digest().joinToString("") { "%02x".format(it) })
    }

    private fun encryptExact(input: InputStream, destination: File, expectedSize: Long): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val digest = MessageDigest.getInstance("SHA-256")
        var count = 0L
        FileOutputStream(destination).use { output ->
            output.write(cipher.iv)
            CipherOutputStream(output, cipher).use { encrypted ->
                val buffer = ByteArray(1024 * 1024)
                while (count < expectedSize) {
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), expectedSize - count).toInt())
                    check(read > 0) { "Transfer ended early" }
                    encrypted.write(buffer, 0, read)
                    digest.update(buffer, 0, read)
                    count += read
                }
            }
        }
        check(count == expectedSize) { "Transfer size mismatch" }
        return digest.digest().joinToString("") { "%02x".format(it) }
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

    private fun String.cleanFolderName(): String =
        trim().replace(Regex("[/\\\\]"), "-").take(48)

    private fun normalizeFolderPath(path: String): String {
        val segments = path.split('/').map { it.cleanFolderName() }.filter { it.isNotBlank() }
        return if (segments.isEmpty()) "/" else "/${segments.joinToString("/")}"
    }

    private fun ensureFolderPath(path: String) {
        if (path == "/") return
        val folders = listFolders().toMutableSet()
        var current = ""
        path.removePrefix("/").split('/').forEach { segment ->
            current += "/$segment"
            folders.add(current)
        }
        writeFolders(folders.toList())
    }

    companion object {
        private const val KEY_ALIAS = "voltshare-vault-key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PRIMARY_FOLDER_KEY = "primary-folder"
    }

    private val foldersFile: File
        get() = File(vaultDir, "folders.json")
}