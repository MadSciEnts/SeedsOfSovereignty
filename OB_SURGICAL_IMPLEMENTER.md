# MISSION: Phase 9.9.2 - Unified Addressing Pass (Surgical Implementer)

## STEP 1: UI Suffixing (Slot-Based) [COMPLETE]
- Refactored `TacticalInputController.kt` to use `slotIndex`.
- Updated `CommandMatrixWidget.kt` and `PlayScreen.kt` signatures.

## STEP 2: Widget Integration (Signal Parity)
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/CommandMatrixWidget.kt`
- **Action:** Ensure `updateTacticalSurface` is consistently passing `selectedIndex` (which is the slot index) to `inputController.processSchema`. 
- *Verification:* This was largely handled in Step 1, but confirm that `selectedIndex` is updated *before* any potential layout rebuilds that trigger `updateTacticalSurface`.

## STEP 3: System Verification (Signal Alignment)
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/DroneSystems.kt`
- **Action:** Verify all signal lookups in `DroneRegenSystem` and `DroneFormationSystem` use the correct `slotIdx` suffix.
- **DroneRegenSystem Logic:**
    - `DRONE_MODE_$slotIdx` (Scalar) -> Correct.
    - `DRONE_MODE_SIGNAL_$slotIdx` (Signal) -> Correct.
    - `DRONES_ACTIVE_$slotIdx` (Signal) -> Correct.
    - `DRONES_REMAINING_$slotIdx` (Signal) -> Correct.
    - `DRONE_DEPLOY_$slotIdx` (Scalar) -> Correct.
- **DroneFormationSystem Logic:**
    - `DRONE_MOVE_$slotIdx` (Vector) -> Correct.
    - `DRONE_MODE_SIGNAL_$slotIdx` (Signal) -> Correct.
- **Action:** Remove the fallback `hub.vectors["DRONE_MOVE"]` in `DroneFormationSystem`. All tactical movement must now be slot-indexed.
  ```kotlin
  // Old:
  val rawOffset = hub.vectors["DRONE_MOVE_$slotIdx"] ?: hub.vectors["DRONE_MOVE"] ?: Vector2.Zero
  // New:
  val rawOffset = hub.vectors["DRONE_MOVE_$slotIdx"] ?: Vector2.Zero
  ```
