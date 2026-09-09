# VoltShare Android

VoltShare is a native Android vault and device-to-device sharing app. Files are copied into encrypted app-private storage, so they do not appear in normal shared storage. Transfers use local service discovery and a direct socket between two devices running VoltShare; there is no application server.

## Native scope

- Kotlin + Jetpack Compose only; no web UI and no Expo.
- App lock with PIN, pattern/password modes, and Android biometric/device unlock.
- AES-GCM encrypted vault files with Android Keystore-backed key material.
- File-level lock gate using the vault lock.
- Folder creation, nested folders, rename/delete, file move, private-file search, eight sort modes, and custom up/down ordering.
- Share actions for any file, complete folders as ZIP archives, typed or pasted text as private `.txt` files, gallery photos, and user-installed Android apps as APK or split APKS packages.
- Android Sharesheet receiving for single files, multiple files, and text from other apps; incoming items wait behind the VoltShare lock and are encrypted into the private vault.
- Specialized viewer routing for images, video, text/code, PDFs, and APK/XAPK/APKS package installation.
- Direct local transfer host/discovery/send/receive flow using Android NSD and buffered TCP sockets, with protocol versioning, exact-size validation, SHA-256 verification, and safe rejection of partial files.
- Premium dark/volt-green UI with layered elevation, gradient glow edges, rounded controls, and spring/fade motion.

## GitHub release build

Every branch push and every manual dispatch runs only `:app:assembleRelease`. It does not run lint, unit tests, connected tests, a debug build, or any other Gradle task. Ordinary pushes only verify/build the signed APK; a `v*` tag additionally attaches that APK directly to a GitHub Release. No Actions artifact is uploaded.

Configure these repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

The workflow uses GitHub's automatic `GITHUB_TOKEN`; you do not need to create or paste a personal access token.

Create a release with:

```bash
git tag v1.0.0
git push origin v1.0.0
```