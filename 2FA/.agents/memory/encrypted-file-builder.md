---
name: EncryptedFile builder API
description: Version-specific AndroidX Security Crypto construction behavior for encrypted attachment files.
---

The AndroidX Security Crypto version used by this project exposes `EncryptedFile.Builder` with the argument order `(Context, File, MasterKey, FileEncryptionScheme)` and requires a final `.build()` call to obtain the `EncryptedFile`.

**Why:** A constructor with the wrong argument order can produce misleading follow-on errors around `build()`, so both the signature and the return type need to be checked together.

**How to apply:** When changing encrypted attachment storage, verify the resolved dependency API before editing the helper; do not assume a constructor call itself returns the final encrypted-file object.