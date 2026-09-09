# VoltShare Android

Native Android vault and app-to-app sharing app. Files are encrypted in app-private storage and transferred directly between nearby VoltShare devices without an application server.

## Build

Open the repository as an Android Gradle project and build the `app` module. The release workflow is `.github/workflows/release.yml`; every push runs `:app:assembleRelease` only, without lint/tests/debug builds. A `v*` tag also attaches the signed APK to a GitHub Release.

## Product

- PIN, pattern/password mode, and biometric/device unlock.
- AES-GCM encrypted vault files backed by Android Keystore.
- File-level lock gate.
- Image, video, text/code, PDF, APK, XAPK/APKS, and generic-file viewer routes.
- Local NSD discovery plus direct TCP transfer between two devices running the same app.
- Pure dark / volt-green premium UI with elevated glass cards and glow controls.

## Architecture decisions

- No backend or cloud dependency: discovery is Android NSD and the transfer stream is a direct socket.
- Every imported file is encrypted before it is written under `filesDir/vault`; viewer previews are temporary cache files.
- Release signing is supplied only through GitHub Actions secrets; keystore files are ignored locally.
