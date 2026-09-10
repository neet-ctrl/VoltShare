---
name: Android export and Incognito quirks
description: Platform behaviors that are easy to miss when launching Chrome Incognito or writing generated backups through Android providers.
---

Chrome on Android may ignore its Incognito extra on a normal ACTION_VIEW intent. Use Chrome's explicit launcher activity with ACTION_MAIN, Browser.EXTRA_CREATE_NEW_TAB, the URL as data, and the Incognito extra.

**Why:** Chrome's supported internal launch path applies the Incognito request during launcher/new-tab handling; a package-scoped ACTION_VIEW can still open the URL in a regular tab.

**How to apply:** For generated files, finish building the bytes before opening the destination, use truncating provider write modes with a fallback, and pre-create cache files before generating FileProvider URIs.