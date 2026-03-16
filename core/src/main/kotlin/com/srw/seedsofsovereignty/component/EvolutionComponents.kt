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

// Moving SubTier here so it's globally accessible for all components
enum class SubTier { A, B, C, D }
