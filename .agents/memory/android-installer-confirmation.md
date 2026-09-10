---
name: APK install confirmation
description: Android PackageInstaller status callbacks can require a second user-confirmation step before reporting success.
---

PackageInstaller callbacks must treat `STATUS_PENDING_USER_ACTION` as an intermediate state, launch the returned confirmation intent, and keep the PendingIntent reusable for the final success/failure callback.

**Why:** Treating the intermediate status as a terminal failure makes a valid APK install appear to fail even when unknown-source permission is already granted.

**How to apply:** Preserve the original vault display name for package classification, launch Android's confirmation intent when supplied, and surface the final status message plus complete exception chain to the app's diagnostics UI.