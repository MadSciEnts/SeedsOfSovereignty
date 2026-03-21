package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.InputComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.ecs.components.VelocityComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * OBSOLETE: This system has been replaced by ImpulseSystem and PhysicsSystem.
 * Maintained only to resolve build errors until file is deleted.
 */
class MovementSystem : IteratingSystem(
    allOf(TransformComponent::class, VelocityComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val velocityComp = entity[VelocityComponent.mapper] ?: return
        val input = entity[InputComponent.mapper]

        if (input != null) {
            val axes = input.impulseAxes
            velocityComp.velocity.set(
                axes.x * velocityComp.speed,
                axes.y * velocityComp.speed
            )

            if (axes.len() > 0.1f) {
                transform.rotation = axes.angleDeg() - 90f
            }
        }

        transform.position.x += velocityComp.velocity.x * deltaTime
        transform.position.y += velocityComp.velocity.y * deltaTime
    }
}
