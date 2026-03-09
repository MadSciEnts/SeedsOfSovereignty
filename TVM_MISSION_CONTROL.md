# TVM MISSION CONTROL: v0.4.0 Strategic Payloads

## 1. ARCHITECTURAL BASELINE
- **Version:** v0.4.0 (Chained Instrument DNA Stable).
- **Core Goal:** Transition from "Input HUD" to "Tactical Instrument" via Bidirectional Feedback Loops and procedural Texture Collections.

## 2. PHASE 6.2: THE VISUAL CORE (Texture Collections)
Establish the engine for generating classified hardware visual styles.

### Step 1: Define TextureCollection
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDTextureGenerator.kt` (New File).
- **Action:** Implement a `TextureCollection` data class and a `generate(styleId: String)` utility using procedurally generated Pixmaps.
- **Goal:** Support "INDUSTRIAL", "HIGH_TECH", and "ALIEN" collections.

### Step 2: Refactor HUDFactory
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDFactory.kt`.
- **Action:** Refactor `createSkin()` to register these procedural collections.

## 3. PHASE 6.3: THE BIDIRECTIONAL HUB (Feedback Loops)
Bridge the ECS state back to the UI actors for dynamic labels and colors.

### Step 1: Implement TacticalHubComponent
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/TacticalActionComponent.kt`.
- **Action:** Rename to `TacticalHubComponent`. Add `statusSignals: MutableMap<String, StatusSignal>`.
- **Action:** Propagate rename project-wide.

### Step 2: Create Feedback-Aware Actors
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDFactory.kt`.
- **Action:** Update `createTacticalInput` to return actors that can override their `act()` method to "pull" signals from the hub.

## 4. JUNIOR AGENT: RULES OF ENGAGEMENT
1. **The Handover:** STOP and wait for review after every Phase (6.2, 6.3, etc.).
2. **Persistence:** Update `TVM_WORKER_LOG.md` before starting each phase.
3. **Strict ROE:** DO NOT modify this Mission Control file.
