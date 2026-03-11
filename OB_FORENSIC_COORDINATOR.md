# MISSION COORDINATOR PROTOCOL (v0.8.0)

## 1. THE PRESERVATION MANDATE (CRITICAL)
- **DO NOT ALTER ARCHITECTURAL ANNOTATIONS.** 
- Any comment block starting with `Technical Specification` or `ARCHITECTURAL WARNING` is a permanent Lead Architect mandate.

## 2. SURGICAL WORKFLOW
1. Formulate precise code instructions based on T1 ARCHITECT directives.
2. Write instructions to `OB_SURGICAL_IMPLEMENTER.md`.
3. Provide the handoff prompt to the Developer.
4. Perform a Rule #12 audit (`read_file`) after completion.

---

# CURRENT MISSION: Phase 21.0 - Diegetic Engine Stabilization (v2.3.7)
**Status:** [ 0 / 3 : INITIALIZING ] - Consensus Reached at High Table.

## STEP 1: CALIBRATE UNIT PROJECTION (PPM)
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/UISystems.kt`
- Define `const val DIEGETIC_PPM = 12.0f`.
- Refactor `DiegeticHUDSystem` to scale world-offsets by this constant before screen mapping.
- Align `RadarRenderSystem` to the same scale.

## STEP 2: HARDEN SENSOR LIFECYCLE (STATE-SYNC)
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/SensorSystems.kt`
- **STOP** the frame-by-frame clear/respawn cycle.
- Implement a persistent `Map<Entity, Entity>` to sync world targets to their AR markers.
- Only create/destroy markers when an entity enters/leaves sensor range.

## STEP 3: RESTORE FLEET VISIBILITY
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/SensorSystems.kt`
- Ensure the `VisibleComponent` is correctly attached to detected entities to allow the `EntityRenderSystem` to draw them.

## STEP 4: APPLY Z-INDEX DEPTH STACK
**Target:** All HUD creation calls in `HUDFactory` and `SensorSystems`.
- Ships: 0, Tethers: 1, Labels: 2, Static HUD: 3.
