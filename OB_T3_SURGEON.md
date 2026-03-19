# ONBOARDING: T3 SURGEON (Integration & Polish)

## 1. MISSION CONTEXT
The T2 Coordination Agent has implemented the Tactical Squad Layer. Your job is to verify the integration and prepare for Phase 5 (Squad Formation Logic).

## 2. COMPONENT SURGERY (PhysicsComponents.kt)
If the components were not added correctly, use this block:

```kotlin
// ADD TO PhysicsComponents.kt
enum class SquadRole { LEADER, MEMBER, INDEPENDENT }

enum class OrderType { IDLE, MOVE_TO, ATTACK_TARGET, DEFEND_POSITION }

/**
 * SQUAD: Groups entities for tactical coordination.
 */
data class SquadComponent(
    var squadId: Int = -1,
    var role: SquadRole = SquadRole.INDEPENDENT
) : Component<SquadComponent> {
    override fun type() = SquadComponent
    companion object : ComponentType<SquadComponent>()
}

/**
 * ORDER: Instructions for AI units.
 * Standardized: Used by EnemyAISystem to override heuristics.
 */
data class OrderComponent(
    var type: OrderType = OrderType.IDLE,
    val targetPos: Vector2 = Vector2(),
    var targetEntity: Entity = Entity.NONE
) : Component<OrderComponent> {
    override fun type() = OrderComponent
    companion object : ComponentType<OrderComponent>()
}
```

## 3. AI LOGIC SURGERY (MissionSystems.kt)
The `EnemyAISystem` should now have the following `onTickEntity` structure:

```kotlin
    override fun onTickEntity(entity: Entity) {
        if (entity[FactionComponent].type != FactionType.ENEMY) return

        val myPos = entity[PositionComponent].pos
        val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull()
        val playerPos = player?.get(PositionComponent)?.pos

        var isFiring = false
        val order = if (entity.has(OrderComponent)) entity[OrderComponent] else null

        // 1. DETERMINE INTENT (Order vs Heuristic)
        if (order != null && order.type != OrderType.IDLE) {
            // ... (Order Logic)
        } else if (playerPos != null) {
            // ... (Default Heuristics)
        }
        
        // ... (Bridge Signals to Manifest)
    }
```

## 4. VERIFICATION TASKS
1.  Ensure `OrderComponent` is utilized in `EnemyAISystem`.
2.  Verify that `steerVec` and `aimVec` are written to `ModuleChannelsComponent` via the `ShipManifestComponent`.
3.  Confirm zero-allocation compliance (no `Vector2()` inside `onTickEntity`).
