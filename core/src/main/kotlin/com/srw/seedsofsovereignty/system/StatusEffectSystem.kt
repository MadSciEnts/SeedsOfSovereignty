package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Processes temporary strategic debuffs and buffs on ships.
 * Implements Rule #3: Multi-Faceted Impacts.
 */
class StatusEffectSystem : IteratingSystem(family { all(StatusEffectComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val status = entity[StatusEffectComponent]
        val effects = status.activeEffects

        if (effects.isEmpty()) return

        // 1. Process Duration and Cleanup
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.remainingDuration -= deltaTime
            if (effect.remainingDuration <= 0f) {
                iterator.remove()
            }
        }

        // 2. Apply Ongoing Attribute Modifiers
        // Note: These act as multipliers on top of base stats
        var speedMod = 1.0f
        var cooldownMod = 1.0f

        effects.forEach { effect ->
            when (effect.type) {
                EffectType.SPEED_MULTIPLIER -> speedMod *= effect.potency
                EffectType.COOLDOWN_MULTIPLIER -> cooldownMod *= effect.potency
                else -> {}
            }
        }

        // Apply Speed Snare
        if (entity.has(VelocityComponent)) {
            // Acceleration is affected by the snare potency
            entity[VelocityComponent].acceleration *= speedMod
        }

        // Apply System Jammer
        if (entity.has(WeaponComponent)) {
            // Note: Jammer increases wait time, so potency > 1.0
            entity[WeaponComponent].cooldown += (deltaTime * (cooldownMod - 1.0f)).coerceAtLeast(0f)
        }
    }
}
