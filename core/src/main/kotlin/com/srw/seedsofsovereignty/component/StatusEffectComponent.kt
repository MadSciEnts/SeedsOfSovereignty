package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Tracks active temporary modifications to a ship's performance.
 */
data class StatusEffectComponent(
    val activeEffects: MutableList<ActiveEffect> = mutableListOf()
) : Component<StatusEffectComponent> {
    override fun type() = StatusEffectComponent
    companion object : ComponentType<StatusEffectComponent>()
}

data class ActiveEffect(
    val type: EffectType,
    val potency: Float,
    var remainingDuration: Float,
    val vfx: ImpactVfxType
)
