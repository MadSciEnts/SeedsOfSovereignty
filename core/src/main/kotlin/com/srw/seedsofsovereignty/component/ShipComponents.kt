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
    var equippedTier: Int = 0,
    var slotEntity: Entity? = null // Reference to the entity holding the module stats
)

enum class SlotType { FIXED_ENGINE, FIXED_REACTOR, FIXED_HULL, WEAPON, DRONE, UNIVERSAL }

/**
 * Manages a pending decision for the player.
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

data class ShipChassisComponent(
    val maxShieldLayers: Int = 1,
    val maxHullLayers: Int = 1,
    val wireframeAsset: String = "blueprint_default"
) : Component<ShipChassisComponent> {
    override fun type() = ShipChassisComponent
    companion object : ComponentType<ShipChassisComponent>()
}
