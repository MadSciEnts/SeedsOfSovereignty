package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.MathUtils

/**
 * Defines the ship's physical firing infrastructure (barrels, sequence, current state).
 */
data class WeaponComponent(
    var cooldown: Float = 0f,
    var isFiring: Boolean = false,
    var barrels: Int = 1,
    var currentBarrel: Int = 0,
    var aimAngleRad: Float = MathUtils.PI / 2f,
    val barrelSequence: MutableList<Int> = mutableListOf(0)
) : Component<WeaponComponent> {
    override fun type() = WeaponComponent
    companion object : ComponentType<WeaponComponent>()
}

/**
 * The swappable "Brain" of the weapon. Defines projectile stats and fire rate.
 */
data class WeaponModuleComponent(
    var name: String = "Stock Kinetic",
    var description: String = "Standard kinetic stream.",
    var projectileTier: Int = 1,
    var damage: Float = 10f,
    var projectileSpeed: Float = 1000f,
    var fireRate: Float = 0.5f // Base cooldown duration
) : Component<WeaponModuleComponent> {
    override fun type() = WeaponModuleComponent
    companion object : ComponentType<WeaponModuleComponent>()
}

data class ProjectileComponent(
    var tier: Int = 1,
    var damage: Float = 10f,
    var ownerIsEnemy: Boolean = false
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

data class EnemyComponent(
    var health: Float = 10f,
    var type: String = "Swarm",
    var state: State = State.HUNTING
) : Component<EnemyComponent> {
    enum class State { HUNTING, RETREATING }
    override fun type() = EnemyComponent
    companion object : ComponentType<EnemyComponent>()
}
