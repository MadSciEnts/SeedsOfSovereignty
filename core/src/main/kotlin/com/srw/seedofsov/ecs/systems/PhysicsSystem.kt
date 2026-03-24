package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.PhysicsComponent
import com.srw.seedofsov.ecs.components.ProjectileComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Low-level Newtonian integration system.
 * Truth on Disk: Captures previous positions for projectiles to support accurate raycasting.
 */
class PhysicsSystem : IteratingSystem(
    allOf(TransformComponent::class, PhysicsComponent::class).get(),
    20
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return
        val proj = entity[ProjectileComponent.mapper]

        // 1. Snapshot Position for Raycasting (Projectiles only)
        proj?.let {
            if (!it.isInitialized) {
                it.previousPosition.set(transform.position.x, transform.position.y)
                it.isInitialized = true
            } else {
                it.previousPosition.set(transform.position.x, transform.position.y)
            }
        }

        // 2. Linear Integration
        transform.position.x += physics.velocity.x * deltaTime
        transform.position.y += physics.velocity.y * deltaTime

        // 3. Linear Damping
        physics.velocity.scl(1f - (physics.friction * deltaTime))
        if (physics.velocity.len() < 0.01f) {
            physics.velocity.set(0f, 0f)
        }

        // 4. Angular Integration
        transform.rotation += physics.angularVelocity * deltaTime

        // 5. Angular Damping
        physics.angularVelocity *= (1f - (physics.angularDamping * deltaTime))
        if (Math.abs(physics.angularVelocity) < 0.1f) {
            physics.angularVelocity = 0f
        }
    }
}
