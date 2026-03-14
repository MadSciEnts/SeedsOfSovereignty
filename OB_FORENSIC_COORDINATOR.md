# ONBOARDING: [T2 COORD] (Forensic Coordinator)

## 1. STRATEGIC CONTEXT
- **Project:** Seeds of Sovereignty.
- **Goal:** Modularize the Diegetic HUD and migrate the Command Matrix from Scene2D to the AR Engine.
- **Architecture:** Fleks ECS. Pure constructor injection. Zero-allocation rendering.

## 2. PHYSICAL STATE (AUDITED)
- **Stable Base:** Build is successful. Registry is synchronized.
- **Structural Foundation:** `CommandMatrixFactory.kt` exists. `COMMAND_MATRIX_ANCHOR` is defined at BOTTOM_RIGHT.
- **Component Sync:** `HUDElementComponent` has `pivotX/Y` and `parentId` fields.

## 3. CURRENT MISSION: Phase 25.4 - Hierarchical Layout (The Shelf Stack)
**Status:** [ 2 / 4 : STEPS COMPLETE ]
**Objective:** Physically stack the Category and Module shelves relative to the Anchor.

### NEXT EXECUTION STEPS:
1. **Step 3: Hierarchical Layout (HUDFactory.kt)** - Update `createCommandMatrixEntities` to spawn the Category Shelf (Bottom) and link it to the Root Anchor.
2. **Step 4: Module Shelf Integration** - Update `updateModuleShelf` to link module buttons to the same Anchor.

## 4. RULES OF ENGAGEMENT
- Refer to `RULES_OF_ENGAGEMENT.md` (v2.6.0).
- **MANDATORY:** You MUST perform a `read_file` on any target file before planning or reporting.
- **MANDATORY:** Update `OB_SURGICAL_IMPLEMENTER.md` before every delegation.
