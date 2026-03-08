package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ParticleComponent(
    val type: Type,
    var color: Color = Color.WHITE,
    var size: Float = 10f,
    var alpha: Float = 1.0f,
    var fadeRate: Float = 1.0f
) : Component<ParticleComponent> {
    enum class Type { SPARK, GLOW, SHIELD_RIPPLE }
    override fun type() = ParticleComponent
    companion object : ComponentType<ParticleComponent>()
}

/**
 * Orchestrates the seamless tier-up celebration sequence (v0.1.7).
 */
data class EvolutionCelebrationComponent(
    var phase: Phase = Phase.TRIGGERED,
    var newTier: Int = 1,
    var newSubTier: Int = 1,
    var timer: Float = 0f,
    var newWeaponDNA: WeaponModuleComponent? = null
) : Component<EvolutionCelebrationComponent> {
    enum class Phase { TRIGGERED, FLASH, FLYING_ICON, COMPLETED }
    override fun type() = EvolutionCelebrationComponent
    companion object : ComponentType<EvolutionCelebrationComponent>()
}
