package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity

// The physical location in the infinite void
data class PositionComponent(val pos: Vector2 = Vector2()) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

// Current orientation in radians
data class OrientationComponent(var angleRad: Float = MathUtils.PI / 2f) : Component<OrientationComponent> {
    override fun type() = OrientationComponent
    companion object : ComponentType<OrientationComponent>()
}

// Current speed and direction
data class VelocityComponent(
    val v: Vector2 = Vector2(),
    var acceleration: Float = 500f,
    var friction: Float = 2.5f
) : Component<VelocityComponent> {
    override fun type() = VelocityComponent
    companion object : ComponentType<VelocityComponent>()
}

// Tags the player-controlled entity
class PlayerComponent : Component<PlayerComponent> {
    override fun type() = PlayerComponent
    companion object : ComponentType<PlayerComponent>()
}

// Weapon state for an entity
data class WeaponComponent(
    var fireRate: Float = 1.0f,
    var cooldown: Float = 0f,
    var isFiring: Boolean = false,
    var barrels: Int = 1,
    var currentBarrel: Int = 0
) : Component<WeaponComponent> {
    override fun type() = WeaponComponent
    companion object : ComponentType<WeaponComponent>()
}

// Marker for projectiles with tier and damage info
data class ProjectileComponent(
    var tier: Int = 1,
    var damage: Float = 10f
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

// Entity lifespan
data class LifeSpanComponent(
    var remaining: Float = 2f
) : Component<LifeSpanComponent> {
    override fun type() = LifeSpanComponent
    companion object : ComponentType<LifeSpanComponent>()
}

// Sensor radius
data class SensorComponent(
    var radius: Float = 400f
) : Component<SensorComponent> {
    override fun type() = SensorComponent
    companion object : ComponentType<SensorComponent>()
}

// Visibility marker
class VisibleComponent : Component<VisibleComponent> {
    override fun type() = VisibleComponent
    companion object : ComponentType<VisibleComponent>()
}

// Enemy marker
data class EnemyComponent(
    var health: Float = 10f,
    val type: String = "Swarm"
) : Component<EnemyComponent> {
    override fun type() = EnemyComponent
    companion object : ComponentType<EnemyComponent>()
}

// Essence dropped by enemies
data class EssenceComponent(
    var amount: Int = 1,
    var isCollected: Boolean = false
) : Component<EssenceComponent> {
    override fun type() = EssenceComponent
    companion object : ComponentType<EssenceComponent>()
}

// Evolution progress
data class EvolutionComponent(
    var currentTier: Int = 1,
    var subTier: Int = 1,
    var essenceCollected: Int = 0,
    var essenceRequired: Int = 5
) : Component<EvolutionComponent> {
    override fun type() = EvolutionComponent
    companion object : ComponentType<EvolutionComponent>()
}

// Player drones
data class DroneComponent(
    val owner: Entity,
    var orbitAngle: Float = 0f,
    var orbitRadius: Float = 50f,
    var orbitSpeed: Float = 2f
) : Component<DroneComponent> {
    override fun type() = DroneComponent
    companion object : ComponentType<DroneComponent>()
}

// Station/Docking
data class StationComponent(
    val name: String = "Outpost",
    val radius: Float = 150f
) : Component<StationComponent> {
    override fun type() = StationComponent
    companion object : ComponentType<StationComponent>()
}

data class DockingComponent(
    var isDocked: Boolean = false,
    var nearestStation: Entity? = null,
    var proximity: Float = 0f
) : Component<DockingComponent> {
    override fun type() = DockingComponent
    companion object : ComponentType<DockingComponent>()
}

// Handles the "Aperture" opening animation for UI
data class ApertureComponent(
    var openProgress: Float = 0f, // 0 to 1
    var state: State = State.CLOSED
) : Component<ApertureComponent> {
    enum class State { CLOSED, OPENING, OPEN, CLOSING }
    override fun type() = ApertureComponent
    companion object : ComponentType<ApertureComponent>()
}
