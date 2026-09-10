# VoltShare Vault Import Integration

This document describes the completed provider-side setup in VoltShare and the
completed attachment integration in the companion app under `2FA/`.

The result is an offline attachment flow:

1. The companion app keeps its normal Android Storage Access Framework (SAF)
   picker.
2. The companion app adds a second option named **VoltShare Vault**.
3. That option opens VoltShare's existing vault screen in multi-select mode.
4. The user selects unlocked files and taps **Import selected**.
5. VoltShare returns read-only `content://` URIs.
6. The companion app copies those streams into its own private storage
   immediately.

No internet, server, shared folder, or direct filesystem access is used.

## What is already implemented in VoltShare

VoltShare now provides:

- `VaultPickerActivity`, opened with
  `app.voltshare.action.PICK_VAULT_FILES`.
- The existing vault UI in selection-only mode, including folders and
  multi-selection.
- A signature-level permission:
  `app.voltshare.permission.ACCESS_VAULT`.
- A read-only provider at:
  `content://app.voltshare.vaultprovider/files/<file-id>`.
- Temporary read grants in the picker result.
- Standard `Intent.EXTRA_STREAM`, `Intent.EXTRA_ALLOW_MULTIPLE`, `data`, and
  `ClipData` result fields.
- Unlock protection at the vault level.
- File-level lock protection. Locked files cannot be selected or streamed.
- Provider-side rejection of write modes, inserts, updates, and deletes.

The current VoltShare vault files remain in VoltShare's private app storage.
The companion app receives streams only; it never receives a filesystem path.

## Required signing setup

The two applications must be signed with the same release certificate. This
is required because the provider permission uses Android's `signature`
protection level.

The debug APK of the companion app will not be authorized if VoltShare is
installed as a release APK signed by a different key. For development, install
both debug apps signed with the same debug key, or configure both projects to
use the same development keystore. For production, use the same release
keystore/certificate for both apps.

Do not copy VoltShare's private files into shared external storage. Do not use
shared UID. Do not add an internet service for this feature.

## Companion app manifest

The 2FA companion app now declares this permission before the `<application>`
element:

```xml
<uses-permission android:name="app.voltshare.permission.ACCESS_VAULT" />
```

The 2FA app also declares `app.voltshare` in its `<queries>` package list so
the source can be diagnosed cleanly on Android versions with package
visibility restrictions. The companion app does not declare VoltShare's
provider or activity. It only needs the permission and the explicit intent
described below.

## Implemented 2FA box-manager flow

The implementation lives in:

- `2FA/app/src/main/AndroidManifest.xml`
- `2FA/feature/secrets/src/main/java/com/twofasapp/feature/secrets/ui/SecretsScreen.kt`
- `2FA/feature/secrets/src/main/java/com/twofasapp/feature/secrets/data/SecretsRepository.kt`

Inside every box manager's secure-entry editor, **Attach any file** first opens
a source dialog with:

- **VoltShare Vault** — launches the protected VoltShare picker using the
  existing vault UI and supports multiple selected files.
- **Device storage** — launches the original `OpenDocument` SAF flow.

Both paths call the existing encrypted attachment import method. VoltShare
URIs are copied immediately into the 2FA app's encrypted private attachment
storage and are not retained as the attachment's durable URI. If an encrypted
copy fails after creating its destination, the destination is deleted so a
partial attachment is not left behind.

The complete 2FA-specific verification checklist is in
`2FA/VOLTSHARE_ATTACHMENT_INTEGRATION.md`.

The same VoltShare bridge is also used by the 2FA app's Universal Backup card:

- **Universal backup file → Open in VoltShare Share** creates the normal
  universal backup with the existing view model, sends it as an explicit
  `ACTION_SEND` with `EXTRA_STREAM`, and opens VoltShare's Share tab.
- **Restore universal backup file → VoltShare Vault** opens the protected
  multi-select vault picker and passes one selected returned URI into the
  existing universal restore flow.

The original Device storage behavior remains available for both actions.

## Companion app: launch the VoltShare picker

Keep the existing SAF launcher for **Device storage**. Add a separate action
for **VoltShare Vault**.

Kotlin example using the Activity Result API:

```kotlin
private val voltSharePicker =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val uris = result.data?.voltShareResultUris().orEmpty()
        if (uris.isNotEmpty()) {
            importVoltShareFiles(uris)
        }
    }

private fun openVoltShareVault() {
    val intent = Intent("app.voltshare.action.PICK_VAULT_FILES").apply {
        // The action is explicit by package so another app cannot be selected.
        setPackage("app.voltshare")
    }

    try {
        voltSharePicker.launch(intent)
    } catch (_: ActivityNotFoundException) {
        // VoltShare is not installed. Show the companion app's normal
        // "Install VoltShare" or unavailable-source message.
    } catch (_: SecurityException) {
        // The two apps are not signed with the same certificate, or the
        // companion manifest is missing ACCESS_VAULT.
    }
}
```

The companion app should show two attachment choices:

- **Device storage**: launch the existing `ACTION_OPEN_DOCUMENT` or
  `OpenMultipleDocuments` flow.
- **VoltShare Vault**: call `openVoltShareVault()`.

The VoltShare action returns multiple files by default. The user can cancel
without changing anything.

## Reading the returned URIs

The result can contain the returned URIs in more than one standard location.
Use all of these locations and remove duplicates:

```kotlin
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
```

For Android API-level compatibility, use the typed
`getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)` and
`getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)` overloads
where the companion app's minimum SDK supports them. The older overloads are
shown above for broad compatibility.

## Copy each file into the companion app

Copy the bytes immediately while the returned grant is active. Use a private
directory such as `filesDir/attachments`, not shared external storage.

```kotlin
private fun importVoltShareFiles(uris: List<Uri>) {
    val destinationDir = File(filesDir, "attachments").apply { mkdirs() }

    lifecycleScope.launch {
        val imported = withContext(Dispatchers.IO) {
            uris.mapNotNull { uri ->
                runCatching {
                    val name = resolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null,
                    )?.use { cursor ->
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                    } ?: "attachment-${System.currentTimeMillis()}"

                    val safeName = name
                        .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                        .take(120)
                        .ifBlank { "attachment" }

                    val target = uniqueFile(destinationDir, safeName)
                    resolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    } ?: error("VoltShare returned no readable stream")

                    target
                }.getOrNull()
            }
        }

        // Update the companion app's attachment list on the main thread.
        onVoltShareImportFinished(imported)
    }
}

private fun uniqueFile(directory: File, requestedName: String): File {
    var candidate = File(directory, requestedName)
    var suffix = 2
    while (candidate.exists()) {
        candidate = File(
            directory,
            "${requestedName.substringBeforeLast('.', requestedName)}-$suffix" +
                requestedName.substringAfterLast('.', "").let {
                    if (it.isBlank()) "" else ".$it"
                },
        )
        suffix += 1
    }
    return candidate
}
```

If the companion app needs the MIME type, call
`contentResolver.getType(uri)` before or during the copy. If it needs the
display name and size, query `OpenableColumns.DISPLAY_NAME` and
`OpenableColumns.SIZE`.

The companion app should not persist the VoltShare URI as its long-term
storage model. The URI is a source for the immediate copy; the copied private
file is the companion app's durable attachment.

## Error handling

Handle these cases in the companion app:

- `RESULT_CANCELED`: the user backed out or canceled; keep the current
  attachment list unchanged.
- Empty URI list: treat as a canceled/empty selection.
- `ActivityNotFoundException`: VoltShare is not installed.
- `SecurityException` while launching: the permission is missing or the two
  APKs are signed with different certificates.
- `SecurityException` or `FileNotFoundException` while copying: VoltShare is
  locked, the selected file is locked, the URI was not copied promptly, or
  the file was removed.
- A failed individual copy: delete the partial destination file and report
  which attachment failed. Do not keep a zero-byte or partial attachment.

The companion app should perform the copy on `Dispatchers.IO` and show a
progress/loading state for large files. Do not block the UI thread.

## Contract constants

The companion app can define these constants locally:

```kotlin
const val VOLTSHARE_PACKAGE = "app.voltshare"
const val VOLTSHARE_PICK_ACTION = "app.voltshare.action.PICK_VAULT_FILES"
const val VOLTSHARE_PERMISSION = "app.voltshare.permission.ACCESS_VAULT"
const val VOLTSHARE_PROVIDER_AUTHORITY = "app.voltshare.vaultprovider"
```

The provider URI shape is:

```text
content://app.voltshare.vaultprovider/files/<file-id>
```

The `<file-id>` is opaque. The companion app must never construct provider
URIs itself; it should use only the URIs returned by the picker.

## Security model

- The picker activity is protected by the signature-level permission.
- The provider is protected by the same permission.
- VoltShare checks that its vault session is unlocked before metadata or
  file bytes are returned.
- Locked individual files are rejected even when the vault is unlocked.
- Provider operations are read-only.
- The result grants read access only; no write or delete operation is exposed.
- The companion app copies into its own private storage immediately.

The companion app should avoid logging returned URIs, file contents, or
private file names in production logs.
