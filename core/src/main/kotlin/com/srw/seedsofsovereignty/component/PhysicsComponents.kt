package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2

/**
 * POSITION: Physical location in the game world.
 */
data class PositionComponent(val pos: Vector2 = Vector2()) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

/**
 * VELOCITY: Speed, direction, and physical constraints.
 * Standardized: Properties are mutable for zero-allocation updates.
 */
data class VelocityComponent(
    val v: Vector2 = Vector2(),
    var acceleration: Float = 0f,
    var friction: Float = 2.0f,
    var maxSpeed: Float = 15f
) : Component<VelocityComponent> {
    override fun type() = VelocityComponent
    companion object : ComponentType<VelocityComponent>()
}

/**
 * SHIP MANIFEST: Hardware registry for equipped modules.
 * Standardized: Direct wiring to prevent global scans.
 */
data class ShipManifestComponent(
    val modules: MutableList<Entity> = mutableListOf(),
    var lastMaintenance: Float = 0f
) : Component<ShipManifestComponent> {
    override fun type() = ShipManifestComponent
    companion object : ComponentType<ShipManifestComponent>()
}

/**
 * SHIP STATE BUS: High-performance categorical resource bus.
 * Standardized: Fixed-size array for zero-allocation Android execution.
 */
data class ShipStateBusComponent(
    val powerLevels: FloatArray = FloatArray(PowerCategory.entries.size) { 0f },
    var totalOutput: Float = 0f,
    var isGlitched: Boolean = false
) : Component<ShipStateBusComponent> {
    override fun type() = ShipStateBusComponent
    companion object : ComponentType<ShipStateBusComponent>()
}

/**
 * SHIP POWER PRIORITY: Relative weights for power distribution.
 * Standardized: Controlled by UI sliders or AI settings.
 */
data class ShipPowerPriorityComponent(
    val priorities: FloatArray = FloatArray(PowerCategory.entries.size) { 1.0f }
) : Component<ShipPowerPriorityComponent> {
    override fun type() = ShipPowerPriorityComponent
    companion object : ComponentType<ShipPowerPriorityComponent>()
}

/**
 * PLAYER TAG: Marks the ship core controlled by the user.
 */
class PlayerComponent : Component<PlayerComponent> {
    override fun type() = PlayerComponent
    companion object : ComponentType<PlayerComponent>()
}

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
