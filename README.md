# VoltShare Android

VoltShare is a native Android vault and device-to-device sharing app. Files are copied into encrypted app-private storage, so they do not appear in normal shared storage. Transfers use local service discovery and a direct socket between two devices running VoltShare; there is no application server.

## Native scope

- Kotlin + Jetpack Compose only; no web UI and no Expo.
- App lock with PIN, pattern/password modes, and Android biometric/device unlock.
- AES-GCM encrypted vault files with Android Keystore-backed key material.
- File-level lock gate using the vault lock.
- Folder creation, nested folders, rename/delete, file move, private-file search, eight sort modes, and custom up/down ordering.
- Specialized viewer routing for images, video, text/code, PDFs, and APK/XAPK/APKS package installation.
- Direct local transfer host/discovery/send/receive flow using Android NSD and buffered TCP sockets, with protocol versioning, exact-size validation, SHA-256 verification, and safe rejection of partial files.
- Premium dark/volt-green UI with layered elevation, gradient glow edges, rounded controls, and spring/fade motion.

## GitHub release build

The only CI build is `assembleRelease`; no debug APK is built or uploaded. On a `v*` tag, GitHub Actions attaches the signed APK directly to the GitHub Release and does not use an Actions artifact.

Configure these repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Create a release with:

```bash
git tag v1.0.0
git push origin v1.0.0
```