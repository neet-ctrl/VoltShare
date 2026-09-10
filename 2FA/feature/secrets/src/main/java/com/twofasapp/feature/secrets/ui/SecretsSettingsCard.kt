package com.twofasapp.feature.secrets.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.settings.SettingsLink
import com.twofasapp.feature.secrets.data.BackupPasswordRequirement
import com.twofasapp.feature.secrets.data.SecretsRepository
import org.koin.compose.koinInject

@Composable
fun SecretsSettingsCard(repository: SecretsRepository = koinInject()) {
    val context = LocalContext.current
    var showActions by remember { mutableStateOf(false) }
    var askPassword by remember { mutableStateOf(false) }
    var feedbackStep by remember { mutableStateOf(FeedbackSettingsStep.NONE) }
    var password by remember { mutableStateOf("") }
    var exportPassword by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var operation by remember { mutableStateOf<BackupOperation?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val hasFeedback = repository.read().unlockRating > 0f && repository.read().unlockComment.isNotBlank()

    fun restore(uri: Uri, suppliedPassword: String?): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.let { repository.importEncrypted(it, suppliedPassword) } ?: false
        }.getOrDefault(false)

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val ok = runCatching {
            context.contentResolver.openOutputStream(uri)?.let { repository.exportEncrypted(it, exportPassword) } ?: error("Unable to open file")
            true
        }.getOrDefault(false)
        status = if (ok) "Encrypted backup saved" else "Backup failed"
        exportPassword = null
        password = ""
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val requirement = runCatching {
            context.contentResolver.openInputStream(uri)?.let(repository::inspectBackup)
                ?: BackupPasswordRequirement.INVALID
        }.getOrDefault(BackupPasswordRequirement.INVALID)
        when (requirement) {
            BackupPasswordRequirement.NOT_REQUIRED -> {
                status = if (restore(uri, null)) {
                    "Vault restored successfully"
                } else {
                    "Restore failed — the backup may be damaged"
                }
            }
            BackupPasswordRequirement.REQUIRED -> {
                pendingImportUri = uri
                operation = BackupOperation.IMPORT
                password = ""
                askPassword = true
            }
            BackupPasswordRequirement.UNKNOWN -> {
                if (restore(uri, null)) {
                    status = "Vault restored successfully"
                } else {
                    pendingImportUri = uri
                    operation = BackupOperation.IMPORT
                    password = ""
                    askPassword = true
                }
            }
            BackupPasswordRequirement.INVALID -> {
                status = "Restore failed — invalid or unsupported backup file"
            }
        }
    }

    SettingsLink(
        title = "Secret's Setting",
        subtitle = "Feedback preferences, encrypted backup and restore",
        icon = TwIcons.Secrets,
        onClick = { showActions = true },
    )

    if (showActions) {
        VaultSettingsDialog(
            hasFeedback = hasFeedback,
            onDismiss = { showActions = false },
            onExport = {
                operation = BackupOperation.EXPORT
                password = ""
                exportPassword = null
                showActions = false
                askPassword = true
            },
            onImport = {
                operation = BackupOperation.IMPORT
                showActions = false
                importLauncher.launch(arrayOf("*/*"))
            },
            onFeedback = {
                showActions = false
                feedbackStep = if (hasFeedback) FeedbackSettingsStep.ACTIONS else FeedbackSettingsStep.SETUP
            },
        )
    }

    when (feedbackStep) {
        FeedbackSettingsStep.NONE -> Unit
        FeedbackSettingsStep.SETUP -> key(feedbackStep) {
            FeedbackFormDialog(
                eyebrow = "FEEDBACK SETUP",
                title = "Choose your response",
                actionLabel = "Save feedback",
                onDismiss = { feedbackStep = FeedbackSettingsStep.NONE },
                onSubmit = { rating, comment ->
                    val current = repository.read()
                    repository.write(current.copy(unlockRating = rating, unlockComment = comment.trim()))
                    feedbackStep = FeedbackSettingsStep.NONE
                    status = "Feedback check-in is ready"
                    true
                },
            )
        }
        FeedbackSettingsStep.ACTIONS -> FeedbackControlsDialog(
            onDismiss = { feedbackStep = FeedbackSettingsStep.NONE },
            onChange = { feedbackStep = FeedbackSettingsStep.VERIFY_CHANGE },
            onDisable = { feedbackStep = FeedbackSettingsStep.VERIFY_DISABLE },
        )
        FeedbackSettingsStep.VERIFY_CHANGE -> key(feedbackStep) {
            FeedbackFormDialog(
                eyebrow = "FEEDBACK CHECK",
                title = "Confirm your response",
                actionLabel = "Verify response",
                onDismiss = { feedbackStep = FeedbackSettingsStep.NONE },
                onSubmit = { rating, comment ->
                    val current = repository.read()
                    val matches = current.unlockRating == rating && current.unlockComment == comment.trim()
                    if (matches) feedbackStep = FeedbackSettingsStep.CHANGE
                    matches
                },
            )
        }
        FeedbackSettingsStep.CHANGE -> key(feedbackStep) {
            FeedbackFormDialog(
                eyebrow = "FEEDBACK UPDATE",
                title = "Create a new response",
                actionLabel = "Save changes",
                onDismiss = { feedbackStep = FeedbackSettingsStep.NONE },
                onSubmit = { rating, comment ->
                    val current = repository.read()
                    repository.write(current.copy(unlockRating = rating, unlockComment = comment.trim()))
                    feedbackStep = FeedbackSettingsStep.NONE
                    status = "Feedback check-in updated"
                    true
                },
            )
        }
        FeedbackSettingsStep.VERIFY_DISABLE -> key(feedbackStep) {
            FeedbackFormDialog(
                eyebrow = "FEEDBACK CHECK",
                title = "Confirm before disabling",
                actionLabel = "Verify response",
                onDismiss = { feedbackStep = FeedbackSettingsStep.NONE },
                onSubmit = { rating, comment ->
                    val current = repository.read()
                    val matches = current.unlockRating == rating && current.unlockComment == comment.trim()
                    if (matches) {
                        repository.write(current.copy(unlockRating = 0f, unlockComment = ""))
                        feedbackStep = FeedbackSettingsStep.NONE
                        status = "Feedback check-in disabled"
                    }
                    matches
                },
            )
        }
    }
    if (askPassword) {
        AlertDialog(
            onDismissRequest = { askPassword = false; password = "" },
            containerColor = TwTheme.color.glassSurfaceStrong,
            title = { Text(if (operation == BackupOperation.EXPORT) "Protect backup" else "Unlock backup") },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            if (operation == BackupOperation.EXPORT) {
                                "Backup password (optional)"
                            } else {
                                "Backup password"
                            }
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(onClick = {
                    askPassword = false
                    if (operation == BackupOperation.EXPORT) {
                        exportPassword = password.takeIf { it.isNotEmpty() }
                        exportLauncher.launch("secrets-backup.secrets")
                    } else {
                        val uri = pendingImportUri
                        val ok = uri != null && restore(uri, password)
                        status = if (ok) "Vault restored successfully" else "Restore failed — check the password"
                        pendingImportUri = null
                        password = ""
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = {
                    askPassword = false
                    pendingImportUri = null
                    password = ""
                }) { Text("Cancel") }
            },
        )
    }
    status?.let {
        AlertDialog(
            onDismissRequest = { status = null },
            title = { Text("Secret's Setting") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { status = null }) { Text("Done") } },
        )
    }
}

private enum class BackupOperation { EXPORT, IMPORT }