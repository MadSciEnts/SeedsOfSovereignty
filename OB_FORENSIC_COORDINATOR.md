# MISSION: Phase 9.9.2 - Unified Addressing Pass (Forensic Coordinator)

You are the **Forensic Coordinator**. Your objective is to unify the UI and ECS signal paths using Slot-Based addressing to resolve crosstalk and persistence failures.

## 1. STRATEGIC SHIFT: SLOT-BASED ADDRESSING
- **DECISION:** We are abandoning Entity ID suffixing. All tactical keys will use the module's **Slot Index** as the suffix (e.g., `DRONE_MOVE_0`).
- **REASON:** This ensures signal parity between UI/Systems and allows persistence to survive module swaps.

## 2. MISSION GOALS
- UI Refactor: Update `TacticalInputController` to suffix keys with `slotIndex`.
- Persistence: Ensure `CommandMatrixWidget` passes the correct `selectedIndex` (which is the slot index) to the controller.
- Lifecycle: Verify `SovereigntyTouchpad` is using the `knobRestore` pass in `act()`.

## 3. SURGICAL WORKFLOW (v0.5.6 PROTOCOL)
1. Write explicit instructions into `OB_SURGICAL_IMPLEMENTER.md`.
2. Provide the handoff prompt to the Developer (User).
3. **MANDATORY:** Perform a Rule #12 audit (`read_file`) after the implementer finishes each step.
4. **STOP** and wait for Developer confirmation.

---

### STEP 1: UI Suffixing (Slot-Based)
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/TacticalInputController.kt`
- **Action:** Refactor `processSchema`. It should no longer take a `slotEntity`. It must take a `slotIndex: Int`.
- **Logic:** Suffix all tactical keys with `_$slotIndex`.

### STEP 2: Widget Integration
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/CommandMatrixWidget.kt`
- **Action:** In `updateTacticalSurface`, pass the `selectedIndex` to `inputController.processSchema`.
- **Logic:** This "Hard-Wires" the visible UI to the correct signal slot.

### STEP 3: System Verification
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/DroneSystems.kt`
- **Action:** Ensure all signal lookups are using `slotIdx` or `launcher.slotIndex`. (Note: This may already be correct, but must be verified).
