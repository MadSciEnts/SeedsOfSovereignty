# MISSION: Phase 9.2 - Persistent Drone Geometry (Surgical Implementer)

## TASK 1: DNA & Persistence Components
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/TacticalSchema.kt`
    - Add `val relativeTo: String? = null` to the `InputChannel` data class.
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/CoreComponents.kt`
    - Add `TacticalPersistenceComponent` containing `val persistentInputs: MutableMap<String, Vector2> = mutableMapOf()`.
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/screen/PlayScreen.kt`
    - Add `it += TacticalPersistenceComponent()` to the player entity initialization.

## TASK 2: Save & Restore Stick State
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/CommandMatrixWidget.kt`
    - In `updateTacticalSurface`, retrieve the `TacticalPersistenceComponent` from the player.
    - When building a `TACTICAL_STICK`:
        1. Check `persistentInputs` for the `channel.actionKey`.
        2. If found, use `touchpad.setKnobPosition(saved.x, saved.y)` to restore state.
        3. In the `ChangeListener`, save the new `knobPercent` back to the map.

## TASK 3: Activate Deploy Button & Unique Keys
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/component/DroneComponents.kt`
    - Update the default `DroneModuleComponent` tactical schema to include:
        1. `InputChannel(ControlClassification.TACTICAL_STICK, "DRONE_MOVE", relX = 0f, relY = 0f)`
        2. `InputChannel(ControlClassification.ACTION_BUTTON, "DRONE_DEPLOY", label = "DEPLOY", relX = 260f, relY = 0f)`
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/CommandMatrixWidget.kt`
    - In `updateTacticalSurface`, when processing a schema, if the `selectedIndex` is > 0, suffix drone-related keys (`DRONE_MOVE`, `DRONE_DEPLOY`) with `_$selectedIndex` before passing them to the callback and listeners. This resolves the **Crosstalk Bug**.

## TASK 4: Anchor-Based Layout
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/TacticalClusterWidget.kt`
    - Refactor `renderSchema` to support the `relativeTo` property.
    - If `relativeTo` is null, use the previous actor in the list as the anchor (current behavior).
    - If `relativeTo` is provided, find the actor with that `actionKey` and position the new channel relative to it.

---
**Status:** Awaiting Execution.
