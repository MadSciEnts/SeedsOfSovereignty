package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.srw.seedofsov.ecs.components.ImpulseEngineComponent
import com.srw.seedofsov.ecs.components.PhysicsComponent
import com.srw.seedofsov.ecs.components.ShipComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.model.ModuleCategory
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Translates slot-based navigation data into engine thrust and rotational torque.
 * Truth on Disk: Respects maxRotationSpeed from ImpulseEngineComponent for gradual orientation.
 * Priority 10: Runs before physics integration.
 */
class ImpulseSystem : IteratingSystem(
    allOf(
        ShipComponent::class,
        ImpulseEngineComponent::class,
        PhysicsComponent::class,
        TransformComponent::class
    ).get(),
    10
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val engine = entity[ImpulseEngineComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return

        // 1. Resolve navigation channel from the engine slot
        val navChannel = ship.getSlot(ModuleCategory.ENGINE)?.getChannel("navigation") ?: return
        val axes = navChannel.valueVector2
        val intensity = axes.len()

        if (intensity > 0.1f) {
            // 2. Apply thrust force to physics
            val force = engine.thrustPower * intensity
            physics.velocity.add(
                axes.x * force * deltaTime,
                axes.y * force * deltaTime
            )

            // 3. Update Orientation (Gradual)
            // Truth on Disk: Uses lerpAngleDeg to ensure smooth shortest-path rotation.
            // Responsiveness is governed by maxRotationSpeed (deg/sec).
            // We use a normalized responsiveness factor based on the engine's capability.
            val targetRotation = axes.angleDeg() - 90f

            // Calculate a responsiveness factor.
            // 180 deg/sec = ~0.5 rotation/sec.
            // We multiply by intensity so gentle stick movements result in gentler turns.
            val responsiveness = (engine.maxRotationSpeed / 180f) * 10f * intensity

            transform.rotation = MathUtils.lerpAngleDeg(
                transform.rotation,
                targetRotation,
                responsiveness * deltaTime
            )

            engine.isActive = true
        } else {
            engine.isActive = false
        }
    }
}
