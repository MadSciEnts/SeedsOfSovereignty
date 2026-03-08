# MISSION: ATO v0.2.0 HUD ELEVATION

## 1. The Core Problem
The current HUD uses manual `drawer.filledRectangle` calls in a Fleks system. This is rigid, hard to place, and doesn't scale well with tactical complexity.

## 2. The ATO Solution
Transition to a hierarchical Scene2D Widget structure (Adaptive Tactical Overlay).
- **Vitals:** `HudBar` (Primitive) -> `VitalsClusterWidget` (Container). Auto-generates bars for Health, Shield, and Evolution.
- **Command Matrix:** `HUDModuleSlotButton` (Interactive loadout tile) -> `CommandMatrixWidget` (Orchestrator).
- **Control DNA:** The module equipped determines the control method (Button, Aim Stick, or Focus Stick).

## 3. Implementation Checklist
- [ ] Add `ControlType` DNA to `WeaponModuleComponent` in `CombatComponents.kt`.
- [ ] Create `HudBar.kt` (Handles its own shimmer/pulse).
- [ ] Create `VitalsClusterWidget.kt` (Stacks attribute bars automatically).
- [ ] Create `HUDModuleSlotButton.kt` (Interactive tiles for Command Matrix).
- [ ] Create `CommandMatrixWidget.kt` (Interactive list + morphing input).
- [ ] Initialize in `PlayScreen.kt` and update via the render loop.
- [ ] Purge manual draw calls in `UISystems.kt`.

## 4. Verification
Build must be stable after every file write. Use `gradle :core:compileKotlin` to verify.
Do NOT reference legacy "Essence" or "Ghost" tasks.
