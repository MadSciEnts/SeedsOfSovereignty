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

/**
 * Manages a pending decision for the player (e.g., keeping old gear vs swapping to new default).
 */
data class ChoiceComponent(
    var pendingModule: WeaponModuleComponent? = null,
    var isAwaitingChoice: Boolean = false,
    var choiceType: ChoiceType = ChoiceType.EVOLUTION_UPGRADE
) : Component<ChoiceComponent> {
    enum class ChoiceType { EVOLUTION_UPGRADE, LOOT_FOUND }
    override fun type() = ChoiceComponent
    companion object : ComponentType<ChoiceComponent>()
}
