---
name: 2FA build inputs
description: The private configuration required to compile the separate 2FA Android repository locally.
---

The separate 2FA Android project cannot configure its Gradle application plugin without `config/config.properties`; the checked-in encrypted config files do not substitute for the generated private config.

**Why:** Release builds create the private config from GitHub Actions secrets, while local checkouts intentionally omit the plaintext signing and service configuration.

**How to apply:** Do not interpret a local Gradle configuration failure caused by the missing file as a Kotlin compile failure. Validate the source through the repository’s GitHub Actions workflow when the user does not provide local build inputs.