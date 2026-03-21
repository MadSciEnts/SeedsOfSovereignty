package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.PhysicsComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Low-level Newtonian integration system.
 * Handles velocity-to-position and angular integration.
 * Priority 20: Ensures integration happens after impulse logic.
 */
class PhysicsSystem : IteratingSystem(
    allOf(TransformComponent::class, PhysicsComponent::class).get(),
    20
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return

        // 1. Linear Integration
        transform.position.x += physics.velocity.x * deltaTime
        transform.position.y += physics.velocity.y * deltaTime

        // 2. Linear Damping
        physics.velocity.scl(1f - (physics.friction * deltaTime))
        if (physics.velocity.len() < 0.01f) {
            physics.velocity.set(0f, 0f)
        }

        // 3. Angular Integration
        transform.rotation += physics.angularVelocity * deltaTime

        // 4. Angular Damping
        physics.angularVelocity *= (1f - (physics.angularDamping * deltaTime))
        if (Math.abs(physics.angularVelocity) < 0.1f) {
            physics.angularVelocity = 0f
        }
    }
}
