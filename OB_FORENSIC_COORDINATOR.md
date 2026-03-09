# MISSION: Phase 7.1 - Drone Avionics Pilot Test (Forensic Coordinator)

You are the **Forensic Coordinator**. Your objective is to oversee the implementation of the first bidirectional feedback loop using the Drone Module.

## 1. MISSION GOALS
- Add `DATA_READOUT` to the hardware DNA.
- Implement signal-aware labels in the UI factory.
- Refactor the Drone schema to a chained instrument model.
- Bridge the ECS `DroneSystem` to the `TacticalHubComponent`.

## 2. SURGICAL WORKFLOW
For each step below, you must:
1. Formulate a precise prompt for the **Surgical Implementation Agent** based on the task description.
2. Provide the prompt to the Developer (User) to relay.
3. Once the Implementer reports completion, use `read_file` to verify the disk truth (Rule #12).
4. Only move to the next step after physical verification.

---

### STEP 1: DNA Extension
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/TacticalSchema.kt`
- **Action:** Add `DATA_READOUT` to the `ControlClassification` enum.

### STEP 2: UI Factory Case
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDFactory.kt`
- **Action:** Add a case for `ControlClassification.DATA_READOUT` in `createTacticalInput`.
- **Logic:** Return a `Label` inside a `Container` that overrides `act(delta)` to pull numeric values from the hub signals.

### STEP 3: Drone Schema Refactor
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/util/ModuleGenerator.kt`
- **Action:** Update the `DroneModuleComponent` schema to use the chained model (Stick -> Button -> Readout 1 -> Readout 2).

### STEP 4: ECS Signal Bridge
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/DroneSystems.kt`
- **Action:** Update the system to push `DRONES_ACTIVE` and `DRONES_REMAINING` signals to the hub and listen for the manual deploy trigger.

## 3. FINAL SIGN-OFF
After Step 4 is verified, report completion to the Lead Architect.
