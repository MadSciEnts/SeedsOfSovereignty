package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.EffectComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages the lifetime of transient visual effects.
 */
class EffectSystem : IteratingSystem(allOf(EffectComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val effect = entity[EffectComponent.mapper] ?: return

        effect.timer -= deltaTime
        if (effect.timer <= 0f) {
            engine.removeEntity(entity)
        }
    }
}
