---
name: Cloud universal sync encryption
description: Google Drive uses the universal backup container while background sync avoids storing the user's password.
---

Google Drive stores the same universal token-plus-Secret backup container used by manual Universal Backup. When a password-protected sync runs in the background, the already verified token encryption key is also used as the Secret backup's internal encryption input; the user password is never persisted.

**Why:** Background WorkManager jobs cannot ask for a password, but another device must still be able to unlock both token and Secret payloads with the same password.

**How to apply:** Preserve the key-reuse path whenever changing cloud encryption or the universal envelope, and keep legacy token-only Drive JSON readable.

Synchronization details must use counts captured only after the Drive upload succeeds, not live local counts; older status records should show counts as unavailable.

**Why:** Local changes can exist after the last upload, and presenting them as synced would make the recovery status misleading.

**How to apply:** Persist item counts with the local last-successful-sync status and update them atomically with that success.