package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.DamageType
import ktx.ashley.mapperFor

/**
 * Stores projectile-specific data.
 * Truth on Disk: Supports trajectory tracking for continuous collision detection (Raycasting).
 */
class ProjectileComponent : Component, Pool.Poolable {
    var damage = 10f
    var speed = 15f
    var lifetime = 2f
    var damageType = DamageType.KINETIC

    // Advanced Damage Hierarchy
    var shieldMultiplier = 1.0f
    var hullMultiplier = 1.0f
    var coreMultiplier = 1.0f

    // Special Flags
    var bypassShields = false
    var isAcid = false
    var isShieldOnly = false

    // Raycasting Support
    val previousPosition = Vector2()
    var isInitialized = false

    var sourceEntityId: Int? = null

    override fun reset() {
        damage = 10f
        speed = 15f
        lifetime = 2f
        damageType = DamageType.KINETIC
        shieldMultiplier = 1.0f
        hullMultiplier = 1.0f
        coreMultiplier = 1.0f
        bypassShields = false
        isAcid = false
        isShieldOnly = false
        previousPosition.set(0f, 0f)
        isInitialized = false
        sourceEntityId = null
    }

    companion object {
        val mapper = mapperFor<ProjectileComponent>()
    }
}
