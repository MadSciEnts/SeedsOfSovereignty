# ONBOARDING: T2 COORDINATION AGENT (Hardware Bus Integration)

## 1. MISSION CONTEXT
We are implementing the **Hardware Bus Refactor (V2)**. The core `ShipExecutionSystem` is functional but uses hardcoded values for power distribution and lacks logic for several Tier 1 modules. Your task is to plan the surgical implementation of dynamic power priorities and expanded module logic.

## 2. COMPLIANCE MANDATES (NON-NEGOTIABLE)
- **Fleks 2.x:** Use square-bracket access `entity[Component]`. No `ComponentMapper`.
- **Zero Allocation:** Use `FloatArray` for resource buckets (already in `ShipStateBusComponent`). Use class-level `tmp` variables for math.
- **Hardware Bus Lifecycle:**
    1. **Resource Generation:** Sum output from `PowerCoreComponent`.
    2. **Distribution:** Apply priorities to `bus.powerLevels`.
    3. **Functional Execution:** Scale module effects by `efficiency`.

## 3. CORE ARCHITECTURAL ANCHORS
- **Bus Data:** `ShipStateBusComponent` (in `PhysicsComponents.kt`)
- **Execution Hub:** `ShipExecutionSystem` (in `system` directory)
- **Module Metadata:** `ModuleRegistry` (in `ModuleDefinitions.kt`)
- **Input/Output:** `ModuleChannelsComponent` (in `CombatComponents.kt`)

## 4. ASSIGNMENT OBJECTIVES
1. **Dynamic Power Distribution:**
    - Create `ShipPowerPriorityComponent` with a `FloatArray(PowerCategory.entries.size)`.
    - Update `ShipExecutionSystem` Phase 2 to calculate `bus.powerLevels[cat]` using `totalOutput * priority[cat]`.
2. **Module Implementation:**
    - Implement `SHIELD_GEN` logic: Regenerate shield health based on power efficiency.
    - Implement `DRONE_HIVE` logic: Reduce deployment cooldown based on power efficiency.
3. **UI Bridge Planning:**
    - Map the `POWER_CORE` sliders in the `CommandMatrixView` to the new `ShipPowerPriorityComponent`.

## 5. ASSUMPTIONS & LIMITATIONS
- **Tier 1 Focus:** Ignore fleet-level logic for now. Focus on the Player's ship.
- **Linear Scaling:** Efficiency is `min(1.0, available / demand)`.
- **No Structural Changes:** Do not add/remove components inside the loop. Use flags or data updates.

## 6. NEXT STEP FOR T2
Write the `OB_T3_SURGEON.md` file with exact `replace_text` instructions for the files identified.
