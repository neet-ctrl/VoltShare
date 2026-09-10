---
name: Universal backup diagnostics
description: Failure-reporting and password-error behavior for the 2FAS universal backup flow.
---

Universal backup export, temporary-share preparation, share launch, restore read, and restore/import failures should surface the complete exception chain immediately in the app's styled dialog with a copy action. Wrong-password failures should remain a dedicated retry prompt rather than a diagnostic failure dialog.

**Why:** Users need actionable logs for damaged files, provider failures, and handoff errors, while a wrong password is an expected recoverable input error and should not expose an internal cryptographic trace.

**How to apply:** Route every actual failure branch through the same diagnostic event path, keep file-write verification before reporting success, and preserve the separate wrong-password branch when extending universal backup handling.