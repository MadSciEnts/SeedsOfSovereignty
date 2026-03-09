# KNOWN BUGS & TECHNICAL DEBT (v0.5.2)

This document tracks known issues and planned architectural refinements.

## Command Matrix (TVM)

1.  **[BUG] Drone Control Crosstalk:** All drone modules are currently controlled by a single `TACTICAL_STICK` input signal. Each drone module needs to be mapped to its own unique `actionKey`.
2.  **[BUG] Non-Persistent Stick Position:** The `TACTICAL_STICK` position resets when switching between drone modules. The stick's `knobPercent` needs to be persisted per-module.
3.  **[BUG] Inactive Deploy Button:** The "Deploy Drone" button exists in the schema but is not yet wired up in `DroneSystems.kt` to launch drones.
4.  **[BUG] Drone Readout Occlusion:** The `DATA_READOUT` widgets for drone counts are being placed at the Command Matrix origin (0,0) instead of their correct chained position, causing them to be occluded by the main joystick.
5.  **[REFACTOR] Right-to-Left Layout:** The module `loadoutBar` in `CommandMatrixWidget.kt` is a standard `Table` that builds left-to-right. It needs to be refactored to build right-to-left to support the leftward-growing instrument cluster.
