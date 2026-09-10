---
name: Android export and Incognito quirks
description: Platform behaviors that are easy to miss when launching Chrome Incognito or writing generated backups through Android providers.
---

For this VoltShare build, the user has verified that Chrome opens the selected URL in Incognito with a package-scoped ACTION_VIEW intent carrying `com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB`.

**Why:** The package-scoped ACTION_VIEW flow is the tested behavior on the user's device; replacing it with the launcher/new-tab variant caused the previously reported behavior to remain broken.

**How to apply:** Keep the explicit Chrome package, URL data, and Incognito extra together. For generated files, finish building the bytes before opening the destination, use truncating provider write modes with a fallback, and pre-create cache files before generating FileProvider URIs.