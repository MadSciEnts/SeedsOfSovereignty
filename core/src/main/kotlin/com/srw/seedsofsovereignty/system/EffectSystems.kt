package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.ParticleComponent

/**
 * Handles per-frame updates for visual effects, such as fading and scaling.
 */
class EffectSystem : IteratingSystem(family { all(ParticleComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val particle = entity[ParticleComponent]

        // Auto-fade logic: reduce alpha over time
        if (particle.fadeRate > 0f) {
            particle.alpha -= particle.fadeRate * deltaTime
            if (particle.alpha <= 0f) {
                world -= entity
            }
        }
    }
}
