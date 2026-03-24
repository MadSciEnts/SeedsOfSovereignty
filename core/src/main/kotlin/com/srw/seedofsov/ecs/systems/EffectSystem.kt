package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.EffectComponent
import com.srw.seedofsov.ecs.components.SpriteComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages the lifetime and procedural animation of transient visual effects.
 * Truth on Disk: Supports relative positioning, scale drift, and alpha auto-fading.
 */
class EffectSystem : IteratingSystem(allOf(EffectComponent::class, TransformComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val effect = entity[EffectComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val spriteComp = entity[SpriteComponent.mapper]

        // 1. Lifetime Management
        effect.timer -= deltaTime
        if (effect.timer <= 0f) {
            engine.removeEntity(entity)
            return
        }

        // 2. Procedural Animation
        val lifePct = effect.timer / effect.maxLifetime

        // Scale Drift
        if (effect.scaleVelocity != 0f) {
            transform.size.add(effect.scaleVelocity * deltaTime, effect.scaleVelocity * deltaTime)
        }

        // Auto-Fade
        if (effect.autoFade) {
            spriteComp?.sprite?.setAlpha(lifePct)
        }

        // 3. Relative Parenting
        effect.parentTransform?.let { parent ->
            transform.position.set(
                parent.position.x + effect.relativeOffset.x,
                parent.position.y + effect.relativeOffset.y,
                transform.position.z
            )
        }
    }
}
