package com.twofasapp.feature.home.ui.settings

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twofasapp.common.domain.SecretsBackupProvider
import com.twofasapp.data.services.BackupRepository
import com.twofasapp.data.services.domain.BackupContent
import com.twofasapp.data.services.domain.CloudSyncTrigger
import com.twofasapp.data.services.domain.UniversalBackupCodec
import com.twofasapp.data.services.exceptions.DecryptWrongPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class UniversalBackupViewModel(
    private val context: Application,
    private val json: Json,
    private val backupRepository: BackupRepository,
    private val secretsBackupProvider: SecretsBackupProvider,
) : ViewModel() {

    val uiState = MutableStateFlow(UniversalBackupUiState())

    fun export(uri: Uri, password: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val passwordValue = password?.takeIf(String::isNotEmpty)
                val tokenBackup = backupRepository.createBackupContentSerialized(password = passwordValue)
                val serialized = UniversalBackupCodec.encode(
                    json = json,
                    tokenBackup = tokenBackup,
                    secretsBackup = secretsBackupProvider.exportBackup(passwordValue),
                    passwordProtected = passwordValue != null,
                    secretsUpdatedAt = secretsBackupProvider.backupUpdatedAt(),
                ).toByteArray(Charsets.UTF_8)
                writeBackup(uri, serialized)
            }.onSuccess {
                publishEvent(UniversalBackupUiEvent.ExportSuccess)
            }.onFailure { exception ->
                Log.e(TAG, "Universal backup export failed for $uri", exception)
                publishEvent(UniversalBackupUiEvent.ExportError)
            }
        }
    }

    fun restore(uri: Uri, password: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val envelope = runCatching { readEnvelope(uri) }
                .getOrElse {
                    publishEvent(UniversalBackupUiEvent.RestoreError)
                    return@launch
                }

            if (envelope.passwordProtected && password.isNullOrEmpty()) {
                publishEvent(UniversalBackupUiEvent.PasswordRequired(uri))
                return@launch
            }

            runCatching {
                val tokenContent = json.decodeFromString<BackupContent>(
                    envelope.tokenBackup,
                )
                val decryptedTokenContent = if (tokenContent.isEncrypted) {
                    backupRepository.decryptBackupContent(tokenContent, password)
                } else {
                    tokenContent
                }

                require(
                    secretsBackupProvider.importBackup(
                        bytes = envelope.secretsBackup,
                        password = password,
                    )
                ) {
                    "Unable to restore Secret backup"
                }

                backupRepository.import(decryptedTokenContent)
                backupRepository.dispatchCloudSync(CloudSyncTrigger.SecretsChanged)
            }.onSuccess {
                publishEvent(UniversalBackupUiEvent.RestoreSuccess)
            }.onFailure { exception ->
                when (exception) {
                    is DecryptWrongPassword -> publishEvent(UniversalBackupUiEvent.WrongPassword)
                    else -> publishEvent(UniversalBackupUiEvent.RestoreError)
                }
            }
        }
    }

    fun consumeEvent(event: UniversalBackupUiEvent) {
        uiState.update { it.copy(events = it.events - event) }
    }

    private suspend fun readEnvelope(uri: Uri): com.twofasapp.data.services.domain.UniversalBackupFile {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: error("Unable to open universal backup file")

        require(bytes.size <= UniversalBackupCodec.MAX_BACKUP_SIZE) {
            "Universal backup file is too large"
        }
        return UniversalBackupCodec.decode(json, bytes.toString(Charsets.UTF_8))
    }

    private fun publishEvent(event: UniversalBackupUiEvent) {
        uiState.update { it.copy(events = it.events + event) }
    }

    private fun writeBackup(uri: Uri, bytes: ByteArray) {
        val resolver = context.contentResolver
        val output = runCatching {
            resolver.openOutputStream(uri, "wt")
        }.getOrElse {
            resolver.openOutputStream(uri, "w")
        } ?: error("Unable to open universal backup file")

        output.use {
            it.write(bytes)
            it.flush()
        }
    }

    private companion object {
        const val TAG = "UniversalBackup"
    }

}

internal data class UniversalBackupUiState(
    val events: List<UniversalBackupUiEvent> = emptyList(),
)

internal sealed interface UniversalBackupUiEvent {
    data object ExportSuccess : UniversalBackupUiEvent
    data object ExportError : UniversalBackupUiEvent
    data class PasswordRequired(val uri: Uri) : UniversalBackupUiEvent
    data object WrongPassword : UniversalBackupUiEvent
    data object RestoreSuccess : UniversalBackupUiEvent
    data object RestoreError : UniversalBackupUiEvent
}
