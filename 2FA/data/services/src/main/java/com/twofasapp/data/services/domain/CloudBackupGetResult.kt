package com.twofasapp.data.services.domain

sealed interface CloudBackupGetResult {
    data class Success(
        val backupContent: BackupContent,
        val secretsBackup: ByteArray? = null,
        val secretsUpdatedAt: Long = 0L,
    ) : CloudBackupGetResult

    data class Failure(
        val error: CloudSyncError,
    ) : CloudBackupGetResult
}