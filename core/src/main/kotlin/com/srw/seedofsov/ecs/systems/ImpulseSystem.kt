package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.ImpulseEngineComponent
import com.srw.seedofsov.ecs.components.PhysicsComponent
import com.srw.seedofsov.ecs.components.ShipComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.model.ModuleCategory
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Translates slot-based navigation data into engine thrust.
 * Agnostic of input source (Player/AI).
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
        // FIX: Added null safety and used the now-available getSlot()
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

            // 3. Update Orientation
            transform.rotation = axes.angleDeg() - 90f
            engine.isActive = true
        } else {
            engine.isActive = false
        }
    }
}
