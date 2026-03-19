# ONBOARDING: T2 COORDINATION AGENT (Squad Command System)

## 1. MISSION CONTEXT
We have the data structures (`SquadComponent`, `OrderComponent`) and the AI bridge. Now we need the **Propagation Logic**. We need a system that ensures squad members follow their leader's orders automatically.

## 2. COMPLIANCE MANDATES
- **Fleks 2.x:** Square-bracket access only.
- **Zero Allocation:** No list creation during the update loop. Use a world family for members.
- **Efficiency:** Propagation should be O(N) where N is the number of squad members.

## 3. CORE ARCHITECTURAL ANCHORS
- `PhysicsComponents.kt`: References for components.
- `WorldModules.kt`: Registration point for systems.

## 4. ASSIGNMENT OBJECTIVES
1. **Create `SquadCommandSystem.kt`**:
    - Iterate over `family { all(SquadComponent, OrderComponent) }`.
    - If `role == LEADER`, find all `MEMBER` entities with the same `squadId`.
    - Copy the Leader's `OrderComponent` values (type, targetPos, targetEntity) to the Members.
2. **System Registration**:
    - Update `WorldModules.kt` to include `SquadCommandSystem`.
3. **Validation Planning**:
    - Plan a surgical update to `PlayScreen.kt` to spawn a squad for verification.

## 5. ASSUMPTIONS
- For now, members will fly directly to the leader's target (no offsets/formations yet).
- We assume `squadId` is a simple `Int` for matching.
