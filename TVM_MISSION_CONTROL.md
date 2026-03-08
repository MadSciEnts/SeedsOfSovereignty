# TVM MISSION CONTROL: Phase 1 Component Integration

## 1. ARCHITECTURAL BASELINE
- **Current Version:** v0.2.1 Stable (Modular HUD active).
- **Core Goal:** Transition the Command Matrix from hardcoded logic to a **Data-Driven Tactical Schema (TVM v0.3.0)**.
- **Reference File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/TacticalSchema.kt`.

## 2. JUNIOR AGENT: RULES OF ENGAGEMENT
You are the **Surgical Implementation Agent**. You must adhere to these rules strictly:
1. **Scope Locking:** ONLY modify the files explicitly listed in the task steps. 
2. **One-File-One-Turn:** Complete one file modification per response.
3. **No Hallucinations:** If a parameter is missing, do not guess. Check `TacticalSchema.kt`.
4. **Verification Log:** After every write, update `TVM_WORKER_LOG.md` with:
    - [ ] File modified.
    - [ ] Logic added.
    - [ ] Any syntax errors encountered.
5. **The Handover:** When a file is verified, stop and wait for the Lead Architect to review your entry in the log.

## 3. TASK: INTEGRATING THE TACTICAL DNA
We need to bridge the new `TacticalSchema` into our hardware modules so they can carry their own UI blueprints.

### Step 1: Update CombatComponents.kt
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/CombatComponents.kt`
- **Action:** 
    - Keep the existing `ControlType` enum and `controlType` property (to prevent build regression).
    - Add a new property to `WeaponModuleComponent`: 
      `var tacticalSchema: TacticalSchema = TacticalSchema(listOf(InputRequirement(TacticalInputType.MOMENTARY_BUTTON, MatrixAnchor.CENTER, "FIRE")))`
- **Verification:** Ensure `TacticalSchema` and `InputRequirement` are correctly imported.

### Step 2: Update DroneComponents.kt
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/DroneComponents.kt`
- **Action:** Add the same `tacticalSchema` property to `DroneModuleComponent`.
- **Default Schema:** 
  `TacticalSchema(listOf(InputRequirement(TacticalInputType.TACTICAL_FOCUS, MatrixAnchor.CENTER, "DRONE_FOCUS")))`

## 4. INSTRUCTIONS FOR RESPONSE
1. Create `TVM_WORKER_LOG.md` if it doesn't exist.
2. Report that you have read this directive and acknowledge the Junior ROE.
3. Proceed with **Step 1: CombatComponents.kt**.
4. STOP and wait for review.
