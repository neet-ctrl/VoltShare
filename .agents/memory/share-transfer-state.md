---
name: Share transfer state
description: Durable UI-state separation for VoltShare’s nearby-device sharing flow.
---

Hosting, nearby discovery, and active batch transfer are different states and should not be represented by one boolean. The device visibility switch must remain usable while a transfer is running, while the send action should only be disabled during an actual transfer.

**Why:** A single active-state flag made hosting look like an in-progress transfer and disabled the send action at the exact moment a hosted device was ready to receive files.

**How to apply:** Preserve separate status fields for device hosting and transfer-busy state whenever the nearby sharing UI or transfer manager is extended.