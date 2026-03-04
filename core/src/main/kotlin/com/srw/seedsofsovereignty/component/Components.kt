package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

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
    var fireRate: Float = 1.0f, // Delay between shots (combined)
    var cooldown: Float = 0f,
    var isFiring: Boolean = false,
    var barrels: Int = 1,
    var currentBarrel: Int = 0
) : Component<WeaponComponent> {
    override fun type() = WeaponComponent
    companion object : ComponentType<WeaponComponent>()
}

// Marker for projectiles
class ProjectileComponent : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

// Entity lifespan (for bullets, effects, etc)
data class LifeSpanComponent(
    var remaining: Float = 2f
) : Component<LifeSpanComponent> {
    override fun type() = LifeSpanComponent
    companion object : ComponentType<LifeSpanComponent>()
}

// Sensor radius for finding objects in the void
data class SensorComponent(
    var radius: Float = 400f
) : Component<SensorComponent> {
    override fun type() = SensorComponent
    companion object : ComponentType<SensorComponent>()
}

// Marker for entities that are currently visible/detected
class VisibleComponent : Component<VisibleComponent> {
    override fun type() = VisibleComponent
    companion object : ComponentType<VisibleComponent>()
}

// Marker for enemy entities
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

// Tracks evolution progress for the player
data class EvolutionComponent(
    var currentTier: Int = 1,
    var subTier: Int = 1, // 1=a, 2=b, 3=c, 4=d
    var essenceCollected: Int = 0,
    var essenceRequired: Int = 5 // Reduced for faster testing of sub-tiers
) : Component<EvolutionComponent> {
    override fun type() = EvolutionComponent
    companion object : ComponentType<EvolutionComponent>()
}
