package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2

data class ParticleComponent(
    val type: Type = Type.SPARK,
    var color: Color = Color.WHITE,
    var size: Float = 2f,
    var alpha: Float = 1f,
    var fadeRate: Float = 0f
) : Component<ParticleComponent> {
    enum class Type { SPARK, GLOW, TRACE, SMOKE, SHIELD_RIPPLE }
    override fun type() = ParticleComponent
    companion object : ComponentType<ParticleComponent>()
}

/**
 * Tracks a pending evolution celebration event.
 */
data class EvolutionCelebrationComponent(
    var newTier: Int = 1,
    var newSubTier: Int = 1,
    var newWeaponDNA: WeaponModuleComponent? = null,
    var phase: Phase = Phase.TRIGGERED,
    var timer: Float = 0f,
    val spawnPos: Vector2 = Vector2()
) : Component<EvolutionCelebrationComponent> {
    enum class Phase { TRIGGERED, FLASH, FLYING_ICON, COMPLETED }
    override fun type() = EvolutionCelebrationComponent
    companion object : ComponentType<EvolutionCelebrationComponent>()
}
