---
name: Share transfer state
description: Durable UI-state separation for VoltShare’s nearby-device sharing flow.
---

Hosting, nearby discovery, and active batch transfer are different states and should not be represented by one boolean. The device visibility switch must remain usable while a transfer is running, while the send action should only be disabled during an actual transfer.

**Why:** A single active-state flag made hosting look like an in-progress transfer and disabled the send action at the exact moment a hosted device was ready to receive files.

**How to apply:** Preserve separate status fields for device hosting and transfer-busy state whenever the nearby sharing UI or transfer manager is extended.

Outgoing picks should stay as URI or temporary-cache sources until a verified send completes; only then should they be imported into the encrypted vault. Incoming verified transfers can be committed directly to the vault.

**Why:** The Share screen is a transfer queue, not a second view of vault contents. Importing on selection made remove actions destructive and exposed every stored file as a share candidate.

**How to apply:** Keep pending-share cleanup separate from vault deletion, and commit successful outgoing items after the transfer callback rather than when a picker returns.

ACTION_SEND integrations should accept URI values from data, ClipData, and EXTRA_STREAM, because Android share targets do not all populate the same field.

**Why:** Some valid third-party senders provide only the Intent data URI; accepting only EXTRA_STREAM makes the share appear to succeed while VoltShare receives no file.

**How to apply:** Normalize and de-duplicate all three URI sources before creating a pending share, while preserving the existing text-only share behavior.