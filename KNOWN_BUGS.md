# KNOWN BUGS & TECHNICAL DEBT (v0.5.3)

This document tracks known issues and planned architectural refinements.

## Command Matrix (TVM)

1.  **[RESOLVED] Drone Control Crosstalk:** Drone modules now use unique action keys (e.g., `DRONE_MOVE_1`).
2.  **[RESOLVED] Non-Persistent Stick Position:** Stick positions are now persisted per-module via `TacticalPersistenceComponent`.
3.  **[RESOLVED] Inactive Deploy Button:** The "DEPLOY" button is now wired into the drone schema and functional.
4.  **[RESOLVED] Drone Readout Occlusion:** Chained relative positioning now supports `relativeTo` anchors for non-occluded layouts.
5.  **[REFACTOR] Right-to-Left Layout:** The module `loadoutBar` in `CommandMatrixWidget.kt` is a standard `Table` that builds left-to-right. It needs to be refactored to build right-to-left to support the leftward-growing instrument cluster.
