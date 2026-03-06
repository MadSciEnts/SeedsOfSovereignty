package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * Tracks the ship's capacity for swappable hardware modules.
 */
data class ModuleSlotComponent(
    val slots: MutableList<ModuleSlot> = mutableListOf()
) : Component<ModuleSlotComponent> {
    override fun type() = ModuleSlotComponent
    companion object : ComponentType<ModuleSlotComponent>()
}

data class ModuleSlot(
    val type: SlotType,
    val tier: Int,
    var equippedName: String = "EMPTY",
    var equippedTier: Int = 0
)

enum class SlotType { WEAPON, DRONE, UTILITY }
