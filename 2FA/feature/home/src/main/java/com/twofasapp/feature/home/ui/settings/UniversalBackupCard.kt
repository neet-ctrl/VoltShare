package com.twofasapp.feature.home.ui.settings

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.designsystem.TwIcons
import com.twofasapp.designsystem.TwTheme
import com.twofasapp.designsystem.common.TwButton
import com.twofasapp.designsystem.common.TwOutlinedButton
import com.twofasapp.designsystem.dialog.ExportPasswordRegex
import com.twofasapp.designsystem.ktx.toastShort
import com.twofasapp.designsystem.settings.SettingsLink
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun UniversalBackupCard(
    viewModel: UniversalBackupViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var operation by remember { mutableStateOf<UniversalBackupOperation?>(null) }
    var password by remember { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

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

    uiState.events.firstOrNull()?.let { event ->
        LaunchedEffect(event) {
            when (event) {
                UniversalBackupUiEvent.ExportSuccess ->
                    context.toastShort("Universal backup saved")

                UniversalBackupUiEvent.ExportError ->
                    context.toastShort("Universal backup failed")

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
                operation = UniversalBackupOperation.EXPORT
                password = ""
                showPasswordDialog = true
            },
        )
        TwOutlinedButton(
            text = "Restore universal backup file",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                operation = UniversalBackupOperation.IMPORT
                importLauncher.launch(arrayOf("*/*"))
            },
        )
    }

    if (showPasswordDialog) {
        val isExport = operation == UniversalBackupOperation.EXPORT
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
                            exportLauncher.launch("2fas-universal-backup.universal")
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
    IMPORT,
}