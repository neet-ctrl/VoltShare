package com.twofasapp.feature.home.ui.settings

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.common.TwButton
import com.twofasapp.designsystem.common.TwOutlinedButton
import com.twofasapp.designsystem.dialog.ExportPasswordRegex
import com.twofasapp.designsystem.ktx.toastShort
import com.twofasapp.designsystem.settings.SettingsLink
import org.koin.androidx.compose.koinViewModel
import java.io.File

private const val VOLTSHARE_PACKAGE = "app.voltshare"
private const val VOLTSHARE_PICK_ACTION = "app.voltshare.action.PICK_VAULT_FILES"

@Composable
internal fun UniversalBackupCard(
    viewModel: UniversalBackupViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var operation by remember { mutableStateOf<UniversalBackupOperation?>(null) }
    var password by remember { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVoltShareUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVoltShareFile by remember { mutableStateOf<File?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf<UniversalBackupSource?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.export(uri, password)
        password = ""
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.restore(uri)
    }
    val voltShareRestorePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uris = result.data?.voltShareResultUris().orEmpty()
        when {
            uris.isEmpty() -> Unit
            uris.size > 1 -> context.toastShort("Select one universal backup file")
            else -> viewModel.restore(uris.single())
        }
    }

    uiState.events.firstOrNull()?.let { event ->
        LaunchedEffect(event) {
            when (event) {
                UniversalBackupUiEvent.ExportSuccess -> {
                    val voltShareUri = pendingVoltShareUri
                    val voltShareFile = pendingVoltShareFile
                    if (voltShareUri != null) {
                        pendingVoltShareUri = null
                        pendingVoltShareFile = null
                        if (!context.openUniversalBackupInVoltShare(voltShareUri)) {
                            voltShareFile?.delete()
                        }
                    } else {
                        context.toastShort("Universal backup saved")
                    }
                }

                UniversalBackupUiEvent.ExportError -> {
                    pendingVoltShareFile?.delete()
                    pendingVoltShareUri = null
                    pendingVoltShareFile = null
                    context.toastShort("Universal backup failed")
                }

                is UniversalBackupUiEvent.PasswordRequired -> {
                    pendingImportUri = event.uri
                    operation = UniversalBackupOperation.IMPORT
                    password = ""
                    showPasswordDialog = true
                }

                UniversalBackupUiEvent.WrongPassword ->
                    run {
                        password = ""
                        operation = UniversalBackupOperation.IMPORT
                        showPasswordDialog = true
                    }

                UniversalBackupUiEvent.RestoreSuccess -> {
                    pendingImportUri = null
                    context.toastShort("Universal backup restored successfully")
                }

                UniversalBackupUiEvent.RestoreError ->
                    context.toastShort("Universal restore failed — the file may be damaged")
            }
            viewModel.consumeEvent(event)
        }
    }

    SettingsLink(
        title = "Universal Backup",
        subtitle = "Back up and restore tokens and Secrets in one file",
        icon = TwIcons.Cloud,
        showEmptySpaceWhenNoIcon = false,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TwButton(
            text = "Universal backup file",
            leadingIcon = TwIcons.Export,
            leadingIconTint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showSourceDialog = UniversalBackupSource.BACKUP
            },
        )
        TwOutlinedButton(
            text = "Restore universal backup file",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showSourceDialog = UniversalBackupSource.RESTORE
            },
        )
    }

    showSourceDialog?.let { source ->
        UniversalBackupSourceDialog(
            source = source,
            onDismiss = { showSourceDialog = null },
            onDeviceStorage = {
                showSourceDialog = null
                if (source == UniversalBackupSource.BACKUP) {
                    operation = UniversalBackupOperation.EXPORT
                    password = ""
                    showPasswordDialog = true
                } else {
                    operation = UniversalBackupOperation.IMPORT
                    importLauncher.launch(arrayOf("*/*"))
                }
            },
            onVoltShare = {
                showSourceDialog = null
                if (source == UniversalBackupSource.BACKUP) {
                    operation = UniversalBackupOperation.EXPORT_TO_VOLTSHARE
                    password = ""
                    showPasswordDialog = true
                } else {
                    openVoltShareRestorePicker(
                        launcher = voltShareRestorePicker,
                        context = context,
                    )
                }
            },
        )
    }

    if (showPasswordDialog) {
        val isExport = operation == UniversalBackupOperation.EXPORT ||
            operation == UniversalBackupOperation.EXPORT_TO_VOLTSHARE
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                pendingImportUri = null
                password = ""
            },
            containerColor = TwTheme.color.glassSurfaceStrong,
            title = { Text(if (isExport) "Protect universal backup" else "Unlock universal backup") },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (isExport) "Backup password (optional)" else "Backup password")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    enabled = if (isExport) {
                        password.isBlank() || ExportPasswordRegex.matches(password)
                    } else {
                        password.isNotBlank()
                    },
                    onClick = {
                        showPasswordDialog = false
                        if (isExport) {
                            if (operation == UniversalBackupOperation.EXPORT_TO_VOLTSHARE) {
                                val file = File(
                                    context.cacheDir,
                                    "2fas-universal-backup-${System.currentTimeMillis()}.universal",
                                )
                                runCatching {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName,
                                        file,
                                    )
                                    pendingVoltShareFile = file
                                    pendingVoltShareUri = uri
                                    viewModel.export(uri, password)
                                }.onFailure {
                                    file.delete()
                                    pendingVoltShareFile = null
                                    pendingVoltShareUri = null
                                    context.toastShort("Unable to prepare the VoltShare backup")
                                }
                            } else {
                                exportLauncher.launch("2fas-universal-backup.universal")
                            }
                        } else {
                            pendingImportUri?.let { viewModel.restore(it, password) }
                            password = ""
                        }
                    },
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    pendingImportUri = null
                    password = ""
                }) {
                    Text("Cancel")
                }
            },
        )
    }
}

private enum class UniversalBackupOperation {
    EXPORT,
    EXPORT_TO_VOLTSHARE,
    IMPORT,
}

private enum class UniversalBackupSource {
    BACKUP,
    RESTORE,
}

@Composable
private fun UniversalBackupSourceDialog(
    source: UniversalBackupSource,
    onDismiss: () -> Unit,
    onDeviceStorage: () -> Unit,
    onVoltShare: () -> Unit,
) {
    val isBackup = source == UniversalBackupSource.BACKUP
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TwTheme.color.glassSurfaceStrong,
        title = {
            Text(if (isBackup) "Choose backup destination" else "Choose restore source")
        },
        text = {
            Text(
                if (isBackup) {
                    "Create the same universal backup file using your device or open it directly in VoltShare's Share tab."
                } else {
                    "Restore using the existing device picker or choose a universal backup file from your VoltShare vault."
                },
                color = TwTheme.color.onSurfaceSecondary,
            )
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TwButton(
                    text = "Device storage",
                    leadingIcon = TwIcons.Download,
                    leadingIconTint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDeviceStorage,
                )
                TwButton(
                    text = if (isBackup) "Open in VoltShare Share" else "VoltShare Vault",
                    leadingIcon = if (isBackup) TwIcons.Share else TwIcons.Lock,
                    leadingIconTint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onVoltShare,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun openVoltShareRestorePicker(
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    context: Context,
) {
    try {
        launcher.launch(
            Intent(VOLTSHARE_PICK_ACTION)
                .setPackage(VOLTSHARE_PACKAGE),
        )
    } catch (_: ActivityNotFoundException) {
        context.toastShort("VoltShare is not installed on this device")
    } catch (_: SecurityException) {
        context.toastShort("VoltShare is not authorized for this app")
    }
}

private fun Context.openUniversalBackupInVoltShare(uri: Uri): Boolean {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        setPackage(VOLTSHARE_PACKAGE)
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "2FAS universal backup")
        clipData = ClipData.newRawUri("2FAS universal backup", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        startActivity(shareIntent)
        true
    } catch (_: ActivityNotFoundException) {
        toastShort("VoltShare is not installed on this device")
        false
    } catch (_: SecurityException) {
        toastShort("VoltShare cannot receive this file")
        false
    }
}

private fun Intent.voltShareResultUris(): List<Uri> {
    val result = LinkedHashSet<Uri>()
    data?.let(result::add)
    clipData?.let { clip ->
        for (index in 0 until clip.itemCount) {
            clip.getItemAt(index).uri?.let(result::add)
        }
    }
    @Suppress("DEPRECATION")
    getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(result::add)
    @Suppress("DEPRECATION")
    getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach(result::add)
    return result.toList()
}