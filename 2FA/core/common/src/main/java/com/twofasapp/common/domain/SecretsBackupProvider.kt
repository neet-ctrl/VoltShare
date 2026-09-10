package com.twofasapp.common.domain

/**
 * Provides the complete Secret vault backup, including encrypted attachment data.
 *
 * keyEncoded is used only by cloud synchronization after the cloud password has
 * already been verified. It allows background sync to keep working without
 * storing the user's password.
 */
interface SecretsBackupProvider {
    data class Stats(
        val entryCount: Int,
        val trashEntryCount: Int,
    )

    fun exportBackup(password: String? = null, keyEncoded: String? = null): ByteArray

    fun importBackup(
        bytes: ByteArray,
        password: String? = null,
        keyEncoded: String? = null,
    ): Boolean

    fun backupUpdatedAt(): Long

    fun backupStats(): Stats
}