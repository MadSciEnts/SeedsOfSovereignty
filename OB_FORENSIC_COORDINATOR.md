# MISSION: Phase 9.1 - Drone Control Crosstalk Fix (Forensic Coordinator)

You are the **Forensic Coordinator**. Your objective is to resolve the technical debt identified in `KNOWN_BUGS.md`, starting with the Drone Control Crosstalk bug.

## 1. MISSION GOALS
- Ensure each equipped drone module is controlled by its own unique `TACTICAL_STICK`.
- Prevent a single joystick input from controlling all drone modules simultaneously.

## 2. SURGICAL WORKFLOW (v0.5.3 PROTOCOL)
For each step below, you must:
1. Write explicit instructions into `OB_SURGICAL_IMPLEMENTER.md`.
2. Provide the "Welcome" prompt (using `[SESSION: NEW]` or `[SESSION: CONTINUING]`) to the Developer to relay.
3. After the Implementer reports completion, perform a Rule #12 audit (`read_file`).
4. Maintain a single Implementer session per the "Single Blade" rule.

---

### STEP 1: Unique Action Keys
- **Task:** Update `ModuleGenerator.kt`.
- **Action:** When generating the schema for a `DroneModuleComponent`, ensure the `actionKey` for the `TACTICAL_STICK` is unique (e.g., `"DRONE_FOCUS_0"`, `"DRONE_FOCUS_1"`). This will require passing the module's slot index into the generation logic.

### STEP 2: ECS Signal Consumption
- **Task:** Update `DroneSystems.kt`.
- **Action:** The drone control system must now read from its module-specific `actionKey` (e.g., `hub.vectors["DRONE_FOCUS_0"]`) instead of a generic key.

## 3. FINAL SIGN-OFF
After Step 2 is verified, report completion to the Lead Architect and update `TVM_WORKER_LOG.md`.
