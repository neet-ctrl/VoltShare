---
name: Secret attachment privacy
description: Privacy boundary for local Secret-tab storage, previews, and explicit handoffs.
---

Secret-tab vault metadata and attachment files must remain encrypted at rest. Attachment previews should use memory-only buffers, and external viewing should stream through a read-only provider instead of writing decrypted files to cache or shared storage.

**Why:** Viewing, copying, and opening an attachment require temporary plaintext in memory or an explicit handoff to another Android component; those operations cannot function if their payload remains encrypted end-to-end.

**How to apply:** Preserve Android Keystore-backed encryption for stored vault data and attachments, mark sensitive clipboard content and clear it promptly, and treat external viewers as an explicit disclosure boundary rather than silently creating plaintext files.