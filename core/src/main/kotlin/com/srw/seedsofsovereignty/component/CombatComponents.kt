package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

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

data class ProjectileComponent(
    var tier: Int = 1,
    var damage: Float = 10f
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
