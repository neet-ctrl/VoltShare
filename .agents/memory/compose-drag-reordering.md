---
name: Compose drag reordering
description: Reliable long-press drag behavior for reorderable rows rendered inside a Compose column.
---

Reorderable rows rendered with a manual `forEach` must be wrapped in stable item keys, and long-lived pointer-input handlers must read callback values through `rememberUpdatedState`.

**Why:** Without stable keys, moving an item changes the composition slot and can cancel the active gesture. Without updated callbacks, the gesture may keep using the first render's list position and stop after one move.

**How to apply:** For any Compose reorder interaction, key rows by immutable item ID, avoid competing parent tap detectors during reorder mode, and use current callback state inside pointer-input blocks that outlive recompositions.