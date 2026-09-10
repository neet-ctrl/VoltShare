package com.twofasapp.feature.secrets.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.twofasapp.common.domain.SecretsBackupProvider
import com.twofasapp.data.services.domain.CloudSyncTrigger
import com.twofasapp.data.services.remote.CloudSyncWorkDispatcher
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SecretsRepository(
    private val androidContext: Context,
    private val json: Json,
    private val cloudSyncWorkDispatcher: CloudSyncWorkDispatcher,
) : SecretsBackupProvider {
    private val attachmentDirectory by lazy {
        File(androidContext.filesDir, "secrets_attachments").apply { mkdirs() }
    }
    private val masterKey by lazy {
        MasterKey.Builder(androidContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    private val preferences by lazy {
        EncryptedSharedPreferences.create(
            androidContext,
            "secrets_vault",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Synchronized
    fun read(): SecretsVault {
        val stored = preferences.getString(DATA_KEY, null)?.let {
            runCatching { json.decodeFromString<SecretsVault>(it) }.getOrNull()
                ?: runCatching { json.decodeFromString<LegacySecretsVault>(it).toCurrentVault() }.getOrNull()
        } ?: SecretsVault()
        fun migrateEntry(entry: SecretEntry): SecretEntry {
            val migratedAttachments = entry.attachments.map { attachment ->
                if (attachment.storageName.isNotBlank() || attachment.legacyUri.isBlank()) {
                    attachment
                } else {
                    saveAttachment(Uri.parse(attachment.legacyUri))?.copy(id = attachment.id)
                        ?: attachment
                }
            }
            return entry.copy(attachments = migratedAttachments)
        }
        val migrated = stored.copy(
            entries = stored.entries.map(::migrateEntry),
            // Deleted entries can also contain attachments from the first
            // implementation, so migrate trash before it is exported/restored.
            trash = stored.trash.map { it.copy(entry = migrateEntry(it.entry)) },
        )
        if (migrated != stored) {
            writeInternal(migrated, updateTimestamp = false, triggerSync = false)
        }
        return migrated
    }

    @Synchronized
    fun write(vault: SecretsVault) {
        writeInternal(vault, updateTimestamp = true, triggerSync = true)
    }

    @Synchronized
    private fun writeInternal(
        vault: SecretsVault,
        updateTimestamp: Boolean,
        triggerSync: Boolean,
    ) {
        val persisted = if (updateTimestamp) {
            vault.copy(updatedAt = System.currentTimeMillis())
        } else {
            vault
        }
        preferences.edit().putString(DATA_KEY, json.encodeToString(persisted)).apply()
        if (triggerSync) {
            cloudSyncWorkDispatcher.tryDispatch(CloudSyncTrigger.SecretsChanged)
        }
    }

    @Synchronized
    fun saveAttachment(uri: Uri): SecretAttachment? {
        val id = java.util.UUID.randomUUID().toString()
        val storageName = "$id.bin"
        return try {
            val destination = encryptedFile(storageName)
            val size = androidContext.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Unable to open attachment" }
                destination.openFileOutput().use { output -> input.copyTo(output) }
            }
            val name = androidContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else "Attachment"
            } ?: "Attachment"
            val mimeType = androidContext.contentResolver.getType(uri) ?: "application/octet-stream"
            SecretAttachment(
                id = id,
                name = name,
                mimeType = mimeType,
                storageName = storageName,
                size = size,
            )
        } catch (_: Exception) {
            // A provider can fail after encrypted output has been created.
            // Do not leave an unusable file behind for the next prune pass.
            File(attachmentDirectory, storageName).delete()
            null
        }
    }

    fun readAttachment(attachment: SecretAttachment): ByteArray? {
        if (attachment.storageName.isBlank()) return null
        return runCatching { encryptedFile(attachment.storageName).openFileInput().use(InputStream::readBytes) }.getOrNull()
    }

    @Synchronized
    fun deleteAttachment(attachment: SecretAttachment) {
        if (attachment.storageName.isBlank()) return
        runCatching { File(attachmentDirectory, attachment.storageName).delete() }
    }

    @Synchronized
    fun pruneUnusedAttachments(vault: SecretsVault) {
        val used = (vault.entries.flatMap { it.attachments } + vault.trash.flatMap { it.entry.attachments })
            .map { it.storageName }
            .filter(String::isNotBlank)
            .toSet()
        attachmentDirectory.listFiles()?.forEach { file ->
            if (file.name !in used) file.delete()
        }
    }

    fun exportEncrypted(output: OutputStream, password: String?) {
        output.use { it.write(exportBackup(password)) }
    }

    override fun exportBackup(password: String?, keyEncoded: String?): ByteArray {
        val vault = read()
        val vaultAttachments = vault.entries.flatMap { it.attachments } +
            vault.trash.flatMap { it.entry.attachments }
        require(vaultAttachments.all { it.storageName.isNotBlank() }) {
            "One or more attachments could not be migrated into the encrypted vault"
        }
        val backup = SecretsBackup(
            vault = vault,
            attachments = vaultAttachments
                .distinctBy { it.storageName }
                .map { attachment ->
                    val bytes = requireNotNull(readAttachment(attachment)) {
                        "Unable to read encrypted attachment ${attachment.name}"
                    }
                    BackupAttachment(attachment.storageName, Base64.encodeToString(bytes, Base64.NO_WRAP))
                },
        )
        val payload = json.encodeToString(backup).toByteArray(Charsets.UTF_8)
        val passwordValue = password?.takeIf(String::isNotEmpty)
        val keyValue = keyEncoded?.takeIf(String::isNotEmpty)
        val encryptionValue = passwordValue ?: keyValue
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            deriveKey(encryptionValue ?: NO_PASSWORD_MARKER, salt),
            GCMParameterSpec(128, iv),
        )
        val encrypted = cipher.doFinal(payload)
        return ByteArrayOutputStream().apply {
            write(MAGIC_V2)
            write(if (passwordValue == null && keyValue == null) 0 else 1)
            write(salt)
            write(iv)
            write(ByteBuffer.allocate(4).putInt(encrypted.size).array())
            write(encrypted)
        }.toByteArray()
    }

    fun importEncrypted(input: InputStream, password: String?): Boolean {
        val imported = importBackup(input.use { it.readBytes() }, password)
        if (imported) {
            cloudSyncWorkDispatcher.tryDispatch(CloudSyncTrigger.SecretsChanged)
        }
        return imported
    }

    override fun importBackup(bytes: ByteArray, password: String?, keyEncoded: String?): Boolean {
        return runCatching {
            val header = parseBackupHeader(bytes)
            require(
                header.requiresPassword != true ||
                    !password.isNullOrEmpty() ||
                    !keyEncoded.isNullOrEmpty()
            ) {
                "Password required for this backup"
            }
            val saltStart = header.saltStart
            val ivStart = saltStart + 16
            val sizeStart = ivStart + 12
            val payloadStart = sizeStart + 4
            require(bytes.size > payloadStart)
            val payloadSize = ByteBuffer.wrap(bytes, sizeStart, 4).int
            require(payloadSize > 0 && payloadStart + payloadSize <= bytes.size)
            val decryptionPasswords = when (header.requiresPassword) {
                true -> listOf(password.orEmpty(), keyEncoded.orEmpty()).filter(String::isNotEmpty)
                false -> listOf(NO_PASSWORD_MARKER, "")
                null -> listOf(password.orEmpty(), keyEncoded.orEmpty()).filter(String::isNotEmpty)
            }
            val decryptedPayload = decryptionPasswords.asSequence()
                .mapNotNull { candidate ->
                    runCatching {
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(
                            Cipher.DECRYPT_MODE,
                            deriveKey(candidate, bytes.copyOfRange(saltStart, ivStart)),
                            GCMParameterSpec(128, bytes.copyOfRange(ivStart, sizeStart)),
                        )
                        cipher
                            .doFinal(bytes.copyOfRange(payloadStart, payloadStart + payloadSize))
                            .toString(Charsets.UTF_8)
                    }.getOrNull()
                }
                .firstOrNull()
                ?: error("Unable to decrypt Secret backup")
            // Backups created before attachments were copied into the encrypted
            // envelope contain a raw SecretsVault. Keep those backups restorable;
            // read() migrates any legacy attachment URI into encrypted storage.
            val backup = runCatching {
                json.decodeFromString<SecretsBackup>(decryptedPayload)
            }.getOrElse {
                SecretsBackup(json.decodeFromString<SecretsVault>(decryptedPayload))
            }
            val referencedAttachments = (backup.vault.entries.flatMap { it.attachments } +
                backup.vault.trash.flatMap { it.entry.attachments })
                .map { it.storageName }
                .filter(String::isNotBlank)
                .toSet()
            val suppliedAttachments = backup.attachments.map { it.storageName }.toSet()
            require(suppliedAttachments.size == backup.attachments.size)
            require(referencedAttachments.all { it in suppliedAttachments }) {
                "Backup is missing attachment data"
            }
            val decodedAttachments = backup.attachments.map { attachment ->
                val safeName = File(attachmentDirectory, attachment.storageName).name
                require(safeName == attachment.storageName)
                require(attachment.storageName.isNotBlank())
                attachment.storageName to Base64.decode(attachment.data, Base64.DEFAULT)
            }
            decodedAttachments.forEach { (storageName, data) ->
                encryptedFile(storageName).openFileOutput().use { output ->
                    output.write(data)
                }
            }
            writeInternal(backup.vault, updateTimestamp = false, triggerSync = false)
            pruneUnusedAttachments(backup.vault)
            true
        }.getOrDefault(false)
    }

    override fun backupUpdatedAt(): Long = read().updatedAt

    override fun backupStats(): SecretsBackupProvider.Stats {
        val vault = read()
        return SecretsBackupProvider.Stats(
            entryCount = vault.entries.size,
            trashEntryCount = vault.trash.size,
        )
    }

    fun inspectBackup(input: InputStream): BackupPasswordRequirement {
        return runCatching {
            val prefix = ByteArray(MAGIC_V2.size + 1)
            var offset = 0
            while (offset < prefix.size) {
                val count = input.read(prefix, offset, prefix.size - offset)
                if (count < 0) break
                offset += count
            }
            when {
                offset >= MAGIC_V2.size &&
                    prefix.copyOfRange(0, MAGIC_V2.size).contentEquals(MAGIC_V2) -> {
                    if (prefix[MAGIC_V2.size].toInt() == 1) {
                        BackupPasswordRequirement.REQUIRED
                    } else {
                        BackupPasswordRequirement.NOT_REQUIRED
                    }
                }
                offset >= MAGIC_V1.size &&
                    prefix.copyOfRange(0, MAGIC_V1.size).contentEquals(MAGIC_V1) ->
                    BackupPasswordRequirement.UNKNOWN
                else -> BackupPasswordRequirement.INVALID
            }
        }.getOrDefault(BackupPasswordRequirement.INVALID).also {
            runCatching { input.close() }
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun parseBackupHeader(bytes: ByteArray): BackupHeader {
        require(bytes.size > MAGIC_V1.size + 16 + 12 + 4)
        return when {
            bytes.copyOfRange(0, MAGIC_V2.size).contentEquals(MAGIC_V2) -> {
                require(bytes.size > MAGIC_V2.size + 1 + 16 + 12 + 4)
                BackupHeader(
                    saltStart = MAGIC_V2.size + 1,
                    requiresPassword = bytes[MAGIC_V2.size].toInt() == 1,
                )
            }
            bytes.copyOfRange(0, MAGIC_V1.size).contentEquals(MAGIC_V1) ->
                BackupHeader(saltStart = MAGIC_V1.size, requiresPassword = null)
            else -> error("Invalid Secret backup")
        }
    }

    private fun encryptedFile(storageName: String): EncryptedFile {
        val file = File(attachmentDirectory, storageName)
        require(file.parentFile?.canonicalFile == attachmentDirectory.canonicalFile)
        return EncryptedFile.Builder(
            androidContext,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
    }

    private companion object {
        const val DATA_KEY = "encrypted_vault"
        const val NO_PASSWORD_MARKER = "twofas-secrets-backup-no-password-v2"
        val MAGIC_V1 = "SECRETS1".toByteArray(Charsets.US_ASCII)
        val MAGIC_V2 = "SECRETS2".toByteArray(Charsets.US_ASCII)
    }
}

enum class BackupPasswordRequirement {
    NOT_REQUIRED,
    REQUIRED,
    UNKNOWN,
    INVALID,
}

private data class BackupHeader(
    val saltStart: Int,
    val requiresPassword: Boolean?,
)

@kotlinx.serialization.Serializable
private data class SecretsBackup(
    val vault: SecretsVault,
    val attachments: List<BackupAttachment> = emptyList(),
)

@kotlinx.serialization.Serializable
private data class BackupAttachment(
    val storageName: String,
    val data: String,
)

@kotlinx.serialization.Serializable
private data class LegacySecretsVault(
    val managers: List<SecretManager> = defaultSecretManagers(),
    val entries: List<SecretEntry> = emptyList(),
    val trash: List<SecretEntry> = emptyList(),
    val unlockRating: Float = 0f,
    val unlockComment: String = "",
    val updatedAt: Long = 0L,
)

private fun LegacySecretsVault.toCurrentVault() = SecretsVault(
    managers = managers,
    entries = entries,
    trash = trash.map { SecretTrashEntry(entry = it) },
    unlockRating = unlockRating,
    unlockComment = unlockComment,
    updatedAt = updatedAt,
)