# MISSION: Phase 7.3 - Scalable Visual Engine (Forensic Coordinator)

You are the **Forensic Coordinator**. Your objective is to refactor the monolithic texture generator into a modular, interface-based system to support 48 tiers of evolution.

## 1. MISSION GOALS
- Define a `TacticalStyleGenerator` interface.
- Decompose "INDUSTRIAL" and "IMPERIAL" styles into individual files.
- Refactor `HUDTextureGenerator` into a simple factory router.

## 2. SURGICAL WORKFLOW (v0.5.1 PROTOCOL)
For each step below, you must:
1. Write explicit instructions into `OB_SURGICAL_IMPLEMENTER.md`.
2. Provide the "Welcome" prompt to the Developer (User) to relay.
3. Once the Implementer reports completion, use `read_file` to verify the disk truth (Rule #12).
4. Only move to the next step after physical verification.

---

### STEP 1: The Style Interface
- **Task:** Create `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/TacticalStyleGenerator.kt`.
- **Action:** Define an interface `TacticalStyleGenerator` with a method `generate(skin: Skin): TextureCollection`.

### STEP 2: Style Decomposition
- **Task:** Create `IndustrialStyle.kt` and `ImperialStyle.kt` in the same directory.
- **Action:** Move the existing drawing logic for each style from the monolithic generator into these new classes.
- **Action:** These new classes must implement the `TacticalStyleGenerator` interface.

### STEP 3: Router Refactor
- **Task:** Update `HUDTextureGenerator.kt`.
- **Action:** Delete the monolithic drawing functions.
- **Action:** Refactor `generate(styleId, skin)` to instantiate the correct style class and call its `generate` method.

## 3. FINAL SIGN-OFF
After Step 3 is verified, report completion to the Lead Architect and update `TVM_WORKER_LOG.md`.
