package com.twofasapp.data.services.domain

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class UniversalBackupFile(
    val passwordProtected: Boolean,
    val tokenBackup: String,
    val secretsBackup: ByteArray,
    val secretsUpdatedAt: Long,
)

object UniversalBackupCodec {
    const val FORMAT = "2fas-universal-backup"
    const val VERSION = 1
    const val MAX_BACKUP_SIZE = 100 * 1024 * 1024

    fun encode(
        json: Json,
        tokenBackup: String,
        secretsBackup: ByteArray,
        passwordProtected: Boolean,
        secretsUpdatedAt: Long,
    ): String {
        return json.encodeToString(
            UniversalBackupEnvelope(
                passwordProtected = passwordProtected,
                tokenBackup = Base64.encodeToString(
                    tokenBackup.toByteArray(Charsets.UTF_8),
                    Base64.NO_WRAP,
                ),
                secretsBackup = Base64.encodeToString(secretsBackup, Base64.NO_WRAP),
                secretsUpdatedAt = secretsUpdatedAt,
            )
        )
    }

    fun decode(json: Json, serialized: String): UniversalBackupFile {
        require(serialized.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_SIZE) {
            "Universal backup file is too large"
        }

        val envelope = json.decodeFromString<UniversalBackupEnvelope>(serialized)
        require(envelope.format == FORMAT)
        require(envelope.version == VERSION)
        require(envelope.tokenBackup.isNotBlank())
        require(envelope.secretsBackup.isNotBlank())

        return UniversalBackupFile(
            passwordProtected = envelope.passwordProtected,
            tokenBackup = Base64.decode(envelope.tokenBackup, Base64.DEFAULT)
                .toString(Charsets.UTF_8),
            secretsBackup = Base64.decode(envelope.secretsBackup, Base64.DEFAULT),
            secretsUpdatedAt = envelope.secretsUpdatedAt,
        )
    }

    @Serializable
    private data class UniversalBackupEnvelope(
        val format: String = FORMAT,
        val version: Int = VERSION,
        val passwordProtected: Boolean = false,
        val tokenBackup: String,
        val secretsBackup: String,
        val secretsUpdatedAt: Long = 0L,
    )
}