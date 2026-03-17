package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * EVOLUTION & PROGRESSION
 */

data class EvolutionComponent(
    var tier: Int = 1,
    var subTier: SubTier = SubTier.A
) : Component<EvolutionComponent> {
    override fun type() = EvolutionComponent
    companion object : ComponentType<EvolutionComponent>()
}

// Adjusted to 3 sub-tiers per requirement (A, B, C)
enum class SubTier { A, B, C }
