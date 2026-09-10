# 2FAS for Android

The official open-source Android authenticator from [2FAS](https://2fas.com).

2FAS protects online accounts with a second authentication factor. It stores
your authenticator tokens on your device and generates one-time passwords
when you need to sign in.

> **Project status:** This repository contains the Android application source
> and its supporting modules. Distribution, product information, and service
> documentation are maintained at [2fas.com](https://2fas.com).

## Contents

- [What the app does](#what-the-app-does)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [Security and data handling](#security-and-data-handling)
- [Requirements](#requirements)
- [Local development](#local-development)
- [Build variants](#build-variants)
- [Testing and static checks](#testing-and-static-checks)
- [Continuous integration and releases](#continuous-integration-and-releases)
- [Contributing](#contributing)
- [Bug reports and security issues](#bug-reports-and-security-issues)
- [Licensing and graphics](#licensing-and-graphics)
- [Support the project](#support-the-project)

## What the app does

### Authenticator tokens

- Generates time-based one-time passwords (TOTP).
- Supports HMAC-based one-time passwords (HOTP).
- Supports Steam-style OTP links where provided by the service.
- Works with services that implement the standard TOTP or HOTP formats,
  including services such as Google, Microsoft, and Dropbox.
- Organizes authenticator services into groups and provides the primary vault
  experience for viewing and using codes.
- Supports adding services by scanning QR codes or importing an `otpauth`
  link.

### Vault and account management

- Add, edit, reorder, group, focus, and delete authenticator services.
- Recover or permanently dispose of deleted services through Trash.
- Store additional secrets and encrypted attachments in the Secrets area.
- Use application settings, localization, theme, notification, and privacy
  controls.
- Protect access with no lock, a PIN, or biometric authentication.
- Configure PIN attempt limits and lock behavior.
- Add an Android home-screen widget for quick access to supported service
  information.

### Backup, migration, and synchronization

- Create encrypted local backups and restore them through the backup flow.
- Import `.2fas` backup files from Android file/content providers.
- Import data from supported external authenticator formats, including
  Google Authenticator migration payloads, Aegis, and LastPass where the
  corresponding parser accepts the source data.
- Synchronize supported backup data with Google Drive.
- Pair with the 2FAS browser extension and handle extension requests from the
  app.
- Receive browser-extension and application notifications through Firebase
  Cloud Messaging where configured.

The app also accepts Android deep links for authenticator links and `.2fas`
files. Exact import support is determined by the parser and validation rules
in the current source.

## Project structure

The repository is a Gradle multi-module Android project. `:app` is the only
application module; the other modules are Android or Kotlin libraries that
are assembled into it.

### Application

| Module | Responsibility |
| --- | --- |
| `:app` | Application composition root, Android manifest, activities, dependency injection, navigation shell, services, providers, widgets, Firebase integration, and final APK packaging. |

### Core modules

| Module | Responsibility |
| --- | --- |
| `:core:common` | Shared domain contracts, coroutine and dispatcher helpers, time/environment abstractions, extensions, and common dependency injection. |
| `:core:android` | Android-specific helpers, navigation routes and arguments, deep-link handling, transitions, and navigation extensions. |
| `:core:designsystem` | Shared Jetpack Compose theme, colors, typography, dialogs, buttons, surfaces, lists, and reusable UI components. |
| `:core:locale` | Compose-backed localization resources and locale helpers. |
| `:core:network` | Shared HTTP client configuration, serialization, logging, and networking dependency injection. |
| `:core:storage` | Storage abstractions, preference storage, Android security-crypto integration, and storage dependency injection. |
| `:core:cipher` | Encryption/decryption primitives and key or salt generation used by backup flows. |
| `:core:otp` | OTP generation and related OTP implementation built on Commons Codec. |

### Data modules

| Module | Responsibility |
| --- | --- |
| `:data:services` | Primary service and token data layer, Room entities and DAOs, mappers, OTP parsing/code generation, backup content, repositories, and background work. |
| `:data:session` | Session state, app settings, authentication/security repositories, local sources, mappers, and lifecycle-related work. |
| `:data:cloud` | Google account authentication and Google Drive backup integration. |
| `:data:browserext` | Browser-extension pairing data, local persistence, remote operations, and extension integration. |
| `:data:notifications` | Notification repositories and local/remote notification data. |
| `:data:push` | Firebase Cloud Messaging services, notification channels, and push-notification handling. |

### Feature modules

| Module | Responsibility |
| --- | --- |
| `:feature:startup` | Startup, onboarding, update checks, and initial navigation. |
| `:feature:home` | Main authenticator vault, service and group management, edit/add flows, guides, notifications, bottom navigation, and the home settings tab. |
| `:feature:security` | PIN setup and entry, biometric authentication, locking, and security settings. |
| `:feature:secrets` | Secrets vault and encrypted attachment UI and data access. |
| `:feature:backup` | Backup settings, encrypted export, import, restore, and related progress/error states. |
| `:feature:browserext` | Browser-extension pairing, permissions, pending requests, request approval, and extension details. |
| `:feature:externalimport` | External authenticator import selection, parsing, validation, and results. |
| `:feature:qrscan` | Camera and barcode/QR scanning UI shared by add-service and import flows. |
| `:feature:trash` | Deleted-service recovery and permanent disposal. |
| `:feature:appsettings` | Application preferences and settings screens. |
| `:feature:about` | About screen, project information, and open-source licenses. |
| `:feature:widget` | Android Glance widget and widget settings/synchronization behavior. |

### Supporting modules

| Module | Responsibility |
| --- | --- |
| `:base` | Shared authentication status, tracking, and lifecycle contracts. |
| `:prefs` | Typed preference models and encrypted/plain preference infrastructure. |
| `:parsers` | Supported-service definitions, import/OTP parsers, and bundled service icon assets. |
| `:secure-storage` | Local checked-in secure-storage library wrapper used by the app. |
| `:truetime` | Local checked-in TrueTime library used for time synchronization. |

The complete module set is declared in [`settings.gradle.kts`](settings.gradle.kts).

## Architecture

The project is intentionally evolving rather than being a single-style
application:

- Newer screens use Jetpack Compose, Material 3, MVVM, ViewModels, lifecycle
  state collection, and Kotlin Coroutines.
- Older areas use classic Android views, MVP patterns, and RxJava. This code
  remains important for users upgrading from older app versions and is being
  refactored incrementally.
- `:app` owns the application shell. `StartActivity` handles the startup and
  authentication gate, while `MainActivity` hosts the authenticated Compose
  experience.
- `MainScreen` and `MainNavHost` coordinate the root navigation graph,
  modal sheets, onboarding, home, settings, security, backup, import,
  browser-extension, trash, and informational routes.
- `:core:designsystem` provides the shared visual language. `MainAppTheme`
  supports automatic, light, dark, and Android 12+ dynamic color behavior.
- Koin is used for dependency injection. Shared conventions live in the
  included `buildlogic` build instead of being repeated in every module.
- Room and SQLCipher are the current encrypted database path. ObjectBox and
  the older secure-preferences infrastructure remain in the application for
  migration compatibility with installations upgraded from older releases.
- WorkManager is used for background operations such as synchronization,
  migration, notifications, and time-related work.

The dependency graph is pragmatic and partially layered. Feature modules
share common UI and domain modules, some features depend on other features
such as QR scanning, and data services connect to parsers, preferences,
cloud, and cipher code where required by the existing product behavior.

## Security and data handling

Security-sensitive changes should be reviewed carefully and should preserve
the existing migration and recovery behavior.

### Local data

- Primary service and token data is stored in a Room database named
  `database-2fas`.
- SQLCipher is used to encrypt the database at rest.
- Database keys and security-related preferences are protected through the
  app's encrypted preference and storage layers.
- PIN and biometric lock state is handled through the security/session
  features.
- Secret attachments remain encrypted at rest and are streamed through the
  app's secure access path rather than being written as plaintext cache files.
- Legacy ObjectBox and older secure-preferences components are retained so
  users can migrate data from old versions safely.

### Backup and cloud

- Backup data is encrypted before it leaves the device.
- The cipher layer owns backup key/salt generation and backup
  encryption/decryption behavior.
- Google Drive access uses Google account authentication and Google Drive API
  clients.
- Background work is coordinated through WorkManager and the cloud/session
  repositories.
- Treat backup passwords, exported files, and decrypted attachments as
  sensitive data. Never include them in issues, logs, pull requests, or
  screenshots.

### Network and third-party services

- The shared Ktor client uses JSON content negotiation and the app's API
  base URL.
- Firebase Crashlytics is used for crash reporting when enabled by the
  application configuration and user preference.
- Firebase Cloud Messaging supports push and browser-extension notifications.
- Google authentication and Drive are optional service integrations that
  require appropriate configuration for the build environment.
- The app uses platform/OkHttp HTTPS trust. Review network changes carefully;
  the project does not define a separate application certificate-pinning
  policy.

For the complete vulnerability-reporting process, see
[SECURITY.md](SECURITY.md). Do not report security vulnerabilities in public
GitHub issues.

## Requirements

The checked-in build configuration currently targets:

- **JDK:** 17
- **Gradle:** 9.0.0 through the Gradle Wrapper
- **Android compile SDK:** 36
- **Android target SDK:** 36
- **Minimum Android SDK:** 23
- **Android Gradle Plugin:** 8.13.0
- **Kotlin:** 2.2.21
- **Jetpack Compose:** 1.9.4
- **Material 3:** 1.4.0
- **Kotlin Coroutines:** 1.10.2
- **Ktor:** 3.3.2
- **Room:** 2.8.3
- **KSP:** 2.3.2

Versions are centralized in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). Use the Gradle
Wrapper rather than a globally installed Gradle version.

## Local development

### 1. Clone the repository

```bash
git clone https://github.com/twofas/2fas-android.git
cd 2fas-android
```

### 2. Provision local signing inputs

The shared build logic reads `config/config.properties` during Gradle
configuration. Create a local debug keystore at
`config/debug_signing.jks`, then create the untracked
`config/config.properties` file:

```properties
debug.storePassword=your-debug-keystore-password
debug.keyAlias=your-debug-key-alias
debug.keyPassword=your-debug-key-password
```

Do not commit keystores, passwords, or other signing credentials. The
repository may contain encrypted configuration artifacts, but those files are
not a substitute for the plaintext local files expected by the build logic.

### 3. Handle Google Services locally

The repository does not include a shareable `google-services.json`. For a
dependency-free local debug setup, comment out this plugin line in
`app/build.gradle.kts`:

```kotlin
id("com.google.gms.google-services")
```

This is a local development change only. Do not commit a private
`google-services.json` or credentials to the repository. Release automation
provides its own Google Services input securely.

### 4. Open and develop

Open the root directory in Android Studio with the Android SDK installed, or
use the Gradle Wrapper commands below. Android Studio should use JDK 17 for
Gradle and Kotlin compilation.

The application namespace is `com.twofasapp`. Debug builds use the
application ID suffix `.debug`, so they can be installed alongside a
production build.

## Build variants

The application defines three build types:

| Variant | Purpose | Characteristics |
| --- | --- | --- |
| `debug` | Local development | Debuggable, not minified, application ID suffix `.debug`. |
| `releaseLocal` | Local/CI release-style verification | Minified, signed with the release-local signing configuration, and intended for release APK generation outside the production upload path. |
| `release` | Production release configuration | Minified and signed with the release upload signing configuration. |

Useful commands:

```bash
# Development APK
./gradlew assembleDebug

# Release-style APK using the releaseLocal configuration
./gradlew assembleReleaseLocal
```

APK output names are generated from the app version and version code. The
current application version declared in `app/build.gradle.kts` is `5.6.0`.

## Testing and static checks

Use the wrapper for checks appropriate to the environment:

```bash
# JVM unit tests for all modules
./gradlew test

# Instrumented checks when a connected device or emulator is available
./gradlew connectedCheck

# Android lint
./gradlew lint

# Remove generated build outputs
./gradlew clean
```

When changing Room schemas, review the generated schema output under
`app/schemas`. When changing navigation, deep links, import formats,
encryption, migration code, or security behavior, add or update focused unit
and UI coverage where the affected module supports it.

## Continuous integration and releases

The repository's GitHub Actions workflow is
[`release-apk.yml`](.github/workflows/release-apk.yml).

It:

1. Runs on pushes and can also be started manually.
2. Uses Ubuntu and Temurin JDK 17 with Gradle caching.
3. Provisions release signing and Google Services inputs from GitHub Actions
   secrets.
4. Runs:

   ```bash
   ./gradlew --no-daemon --build-cache assembleReleaseLocal
   ```

5. Finds the generated APK and attaches it to a GitHub release.

The workflow expects these repository secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`
- `GOOGLE_SERVICES_JSON`

Never place these values in source control or in an issue. Manual releases
use the workflow's tag input; push-triggered releases use a generated
`build-<run number>` tag according to the workflow configuration.

## Contributing

Contributions are welcome. Please read
[`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull request.

At a high level:

1. Search existing issues before reporting a problem.
2. Fork the repository and create a focused branch.
3. Follow the local setup instructions above.
4. Keep changes scoped and follow the conventions of the module being
   changed.
5. Add tests for new or changed behavior where practical.
6. Describe the change, affected areas, dependencies, and validation in the
   pull request.
7. Open pull requests against the development branch specified by the
   contribution guide.

The codebase is mid-migration toward a more consistent modern Android
architecture. Prefer Compose and MVVM for new screens when the surrounding
feature supports it, reuse the shared design system, use Coroutines for new
asynchronous code, and preserve compatibility paths needed for upgrades from
older releases.

## Bug reports and security issues

### Bugs and product issues

Search the [existing GitHub issues](https://github.com/twofas/2fas-android/issues)
first. If the issue is new, include:

- A clear title and description.
- Steps to reproduce the behavior.
- Relevant logs or error messages with secrets removed.
- Android version, device model, app version, and build variant.

### Security vulnerabilities

Do **not** disclose vulnerabilities in a public issue. Email
**security@2fas.com** and follow the process in
[`SECURITY.md`](SECURITY.md). When possible, use the linked PGP key from the
security policy. Security reports should identify the affected product,
impact, reproduction steps, environment, and proof of concept when
available.

## Licensing and graphics

The source code is licensed under the
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).

Copyright (c) Two Factor Authentication Service, Inc. All rights reserved.

The graphics used in the app are not necessarily part of this open-source
repository and may be subject to separate licensing terms. Check the
applicable asset and third-party license information before reusing artwork,
icons, illustrations, or other visual materials.

## Support the project

If 2FAS is useful to you, you can
[support development with a donation](https://2fas.com/donate). Donations
help fund ongoing development and maintenance.