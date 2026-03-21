package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.DamageType
import ktx.ashley.mapperFor

/**
 * Stores projectile-specific data.
 * Truth on Disk: Includes damage type for interaction logic.
 */
class ProjectileComponent : Component, Pool.Poolable {
    var damage = 0f
    var speed = 0f
    var lifetime = 2f // Seconds before self-destruct
    var damageType = DamageType.KINETIC

    // Prevents the projectile from hitting the ship that fired it immediately
    var sourceEntityId: Int? = null

    override fun reset() {
        damage = 0f
        speed = 0f
        lifetime = 2f
        damageType = DamageType.KINETIC
        sourceEntityId = null
    }

    companion object {
        val mapper = mapperFor<ProjectileComponent>()
    }
}
