# VoltShare attachments in the 2FAS box manager

This feature adds a second attachment source to each secure-entry editor
without changing the existing attachment behavior.

## User flow

When the user opens a box manager, taps **Add entry**, and then taps
**Attach any file**, the app now shows a source dialog:

1. **VoltShare Vault**
   - Opens VoltShare's existing vault picker.
   - Allows the user to browse folders and select multiple unlocked files.
   - Returns read-only `content://` URIs.
2. **Device storage**
   - Runs the existing Android `OpenDocument` SAF picker exactly as before.

Every returned URI, from either source, goes through the existing
`SecretsRepository.saveAttachment()` method. The bytes are copied into
2FAS's encrypted private attachment storage before the entry is saved.

The existing attachment list, preview, delete, encrypted backup, sync, and
trash behavior are unchanged. The VoltShare source itself is app-private but
not encrypted at rest; 2FAS still copies the selected bytes into its own
encrypted attachment storage.

## Universal Backup and Restore integration

The Universal Backup card in the 2FA Settings screen also offers two source
choices for each action.

### Universal backup file

The source dialog offers:

- **Device storage** — the existing behavior. It opens the password dialog and
  then the normal Android `CreateDocument` flow.
- **Open in VoltShare Share** — creates the exact same universal backup format
  using the existing `UniversalBackupViewModel`, writes it to a temporary
  private `FileProvider` URI, and sends that URI directly to:

  ```text
  app.voltshare
  ```

  using `Intent.ACTION_SEND` and `Intent.EXTRA_STREAM`. VoltShare receives the
  file and opens its Share tab so the user only needs to choose the Android
  device. The temporary file is retained in the 2FA cache while the share
  handoff is active. VoltShare acknowledges the handoff after copying the
  source into its own pending-share cache, and 2FAS deletes the temporary
  file. A bounded expiry cleanup also removes it if the user cancels, leaves
  the flow unfinished, or either app is stopped.

### Restore universal backup file

The source dialog offers:

- **Device storage** — the existing `OpenDocument` behavior.
- **VoltShare Vault** — launches the protected
  `app.voltshare.action.PICK_VAULT_FILES` picker. One selected VoltShare URI
  is passed to the existing `UniversalBackupViewModel.restore()` flow, which
  performs the same universal backup validation, password handling, token
  restore, Secrets restore, and sync dispatch as device-selected files.

The restore option requires exactly one selected file. If multiple files are
selected in VoltShare, the 2FA app asks the user to select one backup file.

## Files changed

### Companion app

- `app/src/main/AndroidManifest.xml`
  - Declares `app.voltshare.permission.ACCESS_VAULT`.
  - Declares VoltShare in package visibility queries.
- `feature/secrets/src/main/java/com/twofasapp/feature/secrets/ui/SecretsScreen.kt`
  - Shows the attachment-source dialog.
  - Keeps the original SAF picker.
  - Launches the protected VoltShare picker.
  - Reads single and multiple URI results.
  - Saves every URI through the existing encrypted attachment repository.

### VoltShare

The provider-side implementation is in the main VoltShare app:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/app/voltshare/VaultPickerActivity.kt`
- `app/src/main/java/app/voltshare/VaultContentProvider.kt`
- `app/src/main/java/app/voltshare/VaultShareContract.kt`
- `app/src/main/java/app/voltshare/MainActivity.kt`

The provider returns:

```text
content://app.voltshare.vaultprovider/files/<opaque-file-id>
```

The companion app never constructs this URI. It only uses the URI returned by
the VoltShare picker.

## Security and signing requirement

VoltShare protects its picker activity and provider with:

```text
app.voltshare.permission.ACCESS_VAULT
```

That permission uses Android's `signature` protection level. Both APKs must be
signed with the same certificate:

- For development, install both debug builds signed with the same debug key.
- For release, sign both APKs with the same release certificate.

If the certificates differ, VoltShare will not open for the companion app and
the companion app will show an authorization error.

## Release verification steps

1. Build and install VoltShare.
2. Build and install the companion app with the same signing certificate.
3. Open VoltShare once and ensure the vault contains at least one unlocked
   file.
4. Open the companion app and enter any box manager.
5. Tap **Add entry**.
6. Tap **Attach any file**.
7. Confirm that the source dialog contains **VoltShare Vault** and
   **Device storage**.
8. Choose **Device storage** and confirm the original SAF flow still imports
   and encrypts a file.
9. Open the source dialog again and choose **VoltShare Vault**.
10. Unlock VoltShare if prompted.
11. Select one unlocked file and tap **Import selected**.
12. Confirm the file appears in the companion app's attachment list.
13. Repeat with multiple files.
14. Open each attachment and confirm preview still works.
15. Remove an attachment, save the entry, reopen it, and confirm it remains
   removed.
16. Lock a file in VoltShare and confirm it cannot be selected for export.
17. Cancel both dialogs and confirm the entry editor remains unchanged.
18. Test with VoltShare absent and with mismatched signing keys; both cases
   should show an error without affecting the old Device storage option.

## Why the existing attachment behavior remains safe

The new route does not add a second storage implementation. It only supplies
additional source URIs to the existing import callback:

```text
VoltShare URI
    -> SecretsRepository.saveAttachment(uri)
    -> encrypted 2FAS attachment file
    -> SecretAttachment metadata
    -> saved SecretEntry
```

The old route remains:

```text
SAF URI
    -> SecretsRepository.saveAttachment(uri)
    -> encrypted 2FAS attachment file
    -> SecretAttachment metadata
    -> saved SecretEntry
```

VoltShare files therefore do not remain linked to VoltShare after import.
They are copied into the companion app's own encrypted storage immediately.

## Expected failure behavior

- Canceling either picker leaves the editor unchanged.
- If VoltShare is not installed, the existing Device storage option still
  works.
- If the signing certificate or permission is wrong, the app shows an
  authorization message and does not create a partial attachment.
- If one selected VoltShare file cannot be read, successfully copied files
  remain available and the user sees how many files were imported.
- Locked VoltShare files are rejected by VoltShare and are not copied.
