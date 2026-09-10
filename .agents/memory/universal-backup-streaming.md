---
name: Large universal backups
description: Architecture rule for universal backup files larger than the legacy in-memory JSON format can safely support.
---

New user-selected universal backups must use the length-delimited binary envelope and streamed, authenticated Secrets payload rather than the legacy JSON/Base64 envelope. Restore must process the Secrets section incrementally and install attachment files only after authentication succeeds. Keep the legacy decoder for compatibility with existing backups.

**Why:** The previous format materialized encrypted bytes, Base64 text, JSON text, and decoded copies simultaneously, causing Android heap failures well below large-file sizes.

**How to apply:** Keep cloud/string synchronization and legacy restore separate from the large-file path. Do not reintroduce a whole-file `ByteArray`, Base64 string, or JSON envelope in manual export/restore.