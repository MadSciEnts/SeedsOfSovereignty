package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class SalvageType { ESSENCE, SCRAP, MODULE_CRATE, DATA_CORE }

/**
 * Unified component for all collectable rewards.
 * Supports Rule #10: The Salvage Pipeline.
 */
data class SalvageComponent(
    var type: SalvageType = SalvageType.ESSENCE,
    var rarity: Rarity = Rarity.COMMON,
    var amount: Int = 1,
    var visualScale: Float = 1.0f,
    var isCollected: Boolean = false,
    var collectTimer: Float = 0.5f // Mandatory delay before collection
) : Component<SalvageComponent> {
    override fun type() = SalvageComponent
    companion object : ComponentType<SalvageComponent>()
}
