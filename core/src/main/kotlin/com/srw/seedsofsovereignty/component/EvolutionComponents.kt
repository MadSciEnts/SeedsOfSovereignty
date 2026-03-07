package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Tracks the ship's evolutionary progress.
 * Collection logic moved to Salvage Pipeline (v0.1.9).
 */
data class EvolutionComponent(
    var currentTier: Int = 1,
    var subTier: Int = 1,
    var essenceCollected: Int = 0,
    var essenceRequired: Int = 5
) : Component<EvolutionComponent> {
    override fun type() = EvolutionComponent
    companion object : ComponentType<EvolutionComponent>()
}
