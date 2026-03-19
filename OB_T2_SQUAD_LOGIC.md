# ONBOARDING: T2 COORDINATION AGENT (Tactical Squads)

## 1. MISSION CONTEXT
We are preparing for Epoch II (The Admiral). We need to move away from individual entity AI and towards **Squad-Based Command**. This requires new components to group entities and store orders.

## 2. COMPLIANCE MANDATES
- **Fleks 2.x:** Square-bracket access only.
- **Zero Allocation:** Use Enums for orders. No string-based command parsing.

## 3. CORE ARCHITECTURAL ANCHORS
- `PhysicsComponents.kt`: The home for physical and structural components.
- `MissionSystems.kt`: The home for AI and tactical logic.

## 4. ASSIGNMENT OBJECTIVES
1. **Define the Data Layer:**
    - Add `SquadComponent(val squadId: Int, var role: SquadRole)` to `PhysicsComponents.kt`.
    - Add `OrderComponent(var type: OrderType, val targetPos: Vector2 = Vector2(), var targetEntity: Entity = Entity.NONE)` to `PhysicsComponents.kt`.
    - Define `SquadRole` (LEADER, MEMBER, INDEPENDENT) and `OrderType` (IDLE, MOVE_TO, ATTACK_TARGET, DEFEND_POSITION).
2. **Refactor AI Bridge:**
    - Modify `EnemyAISystem` in `MissionSystems.kt` to check for an `OrderComponent`.
    - If an order exists, use it to derive the `steerVec` and `aimVec` instead of the default "Maintain distance" heuristic.
3. **Plan for T3:**
    - Provide exact `replace_text` instructions for the component additions.

## 5. ASSUMPTIONS
- A Squad Leader's `OrderComponent` is what the Player/AI interacts with.
- Members copy or offset the Leader's order (to be implemented in Phase 5).
