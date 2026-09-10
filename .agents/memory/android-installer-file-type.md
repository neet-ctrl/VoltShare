---
name: APK installer naming
description: The VoltShare vault stores payload bytes under an internal filename that does not preserve the user-facing extension.
---

APK installation must classify the payload using the vault record’s original display name, not the internal storage file name.

**Why:** Vault payloads are stored with an internal `.data` suffix. Treating that path as the file type routes a normal APK into the archive/container flow, which can submit an empty or invalid install session and look like no response.

**How to apply:** Whenever a stored vault file is opened, installed, or exported based on its type, keep the original `VaultFile.name` available alongside the internal `File` path.