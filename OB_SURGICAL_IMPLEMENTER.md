# MISSION: Phase 9.1 - Drone Control Crosstalk Fix (Surgical Implementer)

## 1. MISSION GOALS
- Ensure each equipped drone module is controlled by its own unique `TACTICAL_STICK`.
- Prevent a single joystick input from controlling all drone modules simultaneously.

## 2. SURGICAL TASKS

### STEP 1: Unique Action Keys in ModuleGenerator.kt
- **File:** `C:/Users/steve/AndroidStudioProjects/SeedsOfSovereignty/core/src/main/kotlin/com/srw/seedsofsovereignty/util/ModuleGenerator.kt`
- **Change:** Modify `generateDroneModule(tier: Int, rarity: Rarity = rollRarity())` to include an optional `slotIndex: Int = 0` parameter.
- **Change:** Update the `InputChannel` with `actionKey = "DRONE_MOVE"` to use `actionKey = "DRONE_MOVE_$slotIndex"`.
- **Change:** Update the `InputChannel` with `actionKey = "DRONE_DEPLOY"` to use `actionKey = "DRONE_DEPLOY_$slotIndex"`.
- **Change:** Update the `InputChannel` with `signalKey = "DRONES_ACTIVE"` to use `signalKey = "DRONES_ACTIVE_$slotIndex"`.
- **Change:** Update the `InputChannel` with `signalKey = "DRONES_REMAINING"` to use `signalKey = "DRONES_REMAINING_$slotIndex"`.

### STEP 2: Update Callers of generateDroneModule
- **File:** `C:/Users/steve/AndroidStudioProjects/SeedsOfSovereignty/core/src/main/kotlin/com/srw/seedsofsovereignty/manager/EvolutionManager.kt`
- **Change:** In `syncDroneSlots`, pass the `newSlotIndex` to `ModuleGenerator.generateDroneModule(tier, Rarity.COMMON, slotIndex = newSlotIndex)`.
- **Note:** Check other callers in `WorldSystems.kt` and `MarketTable.kt` and ensure they pass a reasonable default or the intended slot index if available.

### STEP 3: ECS Signal Consumption in DroneSystems.kt
- **File:** `C:/Users/steve/AndroidStudioProjects/SeedsOfSovereignty/core/src/main/kotlin/com/srw/seedsofsovereignty/system/DroneSystems.kt`
- **Change:** In `DroneRegenSystem.onTickEntity`:
    - Use `launcher.slotIndex` to format keys: `DRONES_ACTIVE_${launcher.slotIndex}`, `DRONES_REMAINING_${launcher.slotIndex}`, and `DRONE_DEPLOY_${launcher.slotIndex}`.
- **Change:** In `DroneFormationSystem.onTickEntity`:
    - Retrieve the drone's `slotIndex` from its `DroneComponent`.
    - Use this `slotIndex` to pull the vector from `hub.vectors["DRONE_MOVE_$slotIndex"]`.

## 3. PROTOCOL
- Perform the changes accurately.
- Report completion when finished.
