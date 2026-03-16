package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2

/**
 * COMBAT & WEAPONRY
 */

data class WeaponModuleComponent(
    var evolution: SubTier = SubTier.A,
    var fireRate: Float = 0.5f,      // Time between individual barrel shots
    var currentCooldown: Float = 0f,
    var barrelIndex: Int = 0,
    val barrelOffsets: MutableList<Vector2> = mutableListOf(Vector2(0f, 0f)),
    var damage: Float = 10f,
    var projectileSpeed: Float = 500f
) : Component<WeaponModuleComponent> {
    override fun type() = WeaponModuleComponent
    companion object : ComponentType<WeaponModuleComponent>()
}

// Tag for projectiles
class ProjectileComponent : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

// Stores info about who fired it to avoid self-collision
data class FactionComponent(
    val type: FactionType
) : Component<FactionComponent> {
    override fun type() = FactionComponent
    companion object : ComponentType<FactionComponent>()
}

enum class FactionType { PLAYER, ENEMY, NEUTRAL }
