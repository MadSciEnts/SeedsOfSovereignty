# MISSION: Phase 9.2 - Persistent Drone Geometry (Forensic Coordinator)

You are the **Forensic Coordinator**. Your objective is to resolve the remaining items in the `KNOWN_BUGS.md` file related to the Drone Module HUD.

## 1. MISSION GOALS
- Implement persistent stick positions for each drone module.
- Activate the "Deploy Drone" button logic.
- Fix the occlusion of the drone count `DATA_READOUT` labels.

## 2. SURGICAL WORKFLOW (v0.5.3 PROTOCOL)
For each step below, you must:
1. Write explicit instructions into `OB_SURGICAL_IMPLEMENTER.md`.
2. Provide the `[SESSION: CONTINUING]` prompt to the Developer (User) to relay.
3. After the Implementer reports completion, perform a Rule #12 audit (`read_file`).
4. Maintain a single Implementer session per the "Single Blade" rule.

---

### STEP 1: Persistent State Component
- **Task:** Create a new component `TacticalPersistenceComponent`.
- **Action:** This component will hold a `MutableMap<String, Vector2>` to store the last known knob position for any `TACTICAL_STICK` that has `isPersistent = true` in its DNA.
- **Action:** Add this component to the Player entity in `PlayScreen.kt`.

### STEP 2: Save & Restore Stick State
- **Task:** Update `CommandMatrixWidget.kt`.
- **Action:** When a stick's `ChangeListener` fires, save its `knobPercent` to the `TacticalPersistenceComponent` map, using the stick's `actionKey` as the map key.
- **Action:** When a new tactical surface is rendered, check if a stick's `actionKey` exists in the persistence map. If it does, set the touchpad's knob position to the saved value.

### STEP 3: Activate Deploy Button
- **Task:** Update `DroneSystems.kt`.
- **Action:** In the `DroneRegenSystem`, listen for the `DRONE_DEPLOY_<slotIndex>` action key in the `TacticalHubComponent` scalars. When the value is `1f`, deploy one drone if available.

### STEP 4: Fix Readout Layout
- **Task:** Update `TacticalClusterWidget.kt`.
- **Action:** The current logic places all controls relative to the widget's origin. Refactor the layout loop to correctly apply the `relX` and `relY` offsets from the `InputChannel` DNA, using the `relativeTo` key to find the anchor actor.

## 3. FINAL SIGN-OFF
After Step 4 is verified, report completion to the Lead Architect and update `TVM_WORKER_LOG.md`.
