---
name: Android Wi-Fi verification
description: Best-effort behavior and fallback handling for Wi-Fi network verification in Secrets.
---

Android's `WifiNetworkSpecifier` is a temporary connection request, not a universal password verifier. A correct password can still produce `onUnavailable` because the device, OS, network, or current connection does not permit the request.

**Why:** Treating `onUnavailable` as proof of a wrong password left scanned Wi-Fi records unsaved and made the feature appear broken.

**How to apply:** Preserve the scanned network details and entered password, save the complete record with an honest verification status, and use a bounded timeout so the flow never becomes a dead end.