# ONBOARDING: T2 COORDINATION AGENT (Selection & Tactical Hub)

## 1. MISSION CONTEXT
We are finishing Epoch II (The Admiral). We need a way for the player to select entities in the world and have the `CommandMatrixView` (Tactical Matrix) update to control the selection instead of the player ship.

## 2. COMPLIANCE MANDATES
- **Fleks 2.x:** Square-bracket access only.
- **Zero Allocation:** Use `tempVec` for world-to-screen coordinate conversion.
- **Stable References:** Use `world.run` for UI-to-ECS communication.

## 3. CORE ARCHITECTURAL ANCHORS
- `SelectionComponents.kt`: `SelectedComponent` definition.
- `CommandMatrixView.kt`: The UI widget that needs a new data source.
- `PlayScreen.kt`: The touch input entry point.

## 4. ASSIGNMENT OBJECTIVES
1. **Create `SelectionSystem.kt`**:
    - Listen for selection events (or scan for a `PlayerSelectionRequest` component).
    - Iterate over `family { all(PositionComponent, TextureRegionComponent) }`.
    - Check if the touch point is within the entity's bounds.
    - If hit:
        - Remove `SelectedComponent` from all current holders.
        - Add `SelectedComponent` to the hit entity.
        - Update `tacticalMatrix.targetEntity` to the hit entity.
2. **Update `CommandMatrixView.kt`**:
    - Add a `targetEntity: Entity` property.
    - Update `onTick` (or `act`) to read channel data from `targetEntity` instead of a hardcoded player reference.
3. **Registration**:
    - Update `WorldModules.kt` to include `SelectionSystem`.

## 5. ASSUMPTIONS
- Selection only works on `SquadRole.LEADER` or `SquadRole.INDEPENDENT` entities to avoid selecting individual squad members.
- The `navMatrix` remains fixed to the Player's ship for movement, while the `tacticalMatrix` switches to the selection.
