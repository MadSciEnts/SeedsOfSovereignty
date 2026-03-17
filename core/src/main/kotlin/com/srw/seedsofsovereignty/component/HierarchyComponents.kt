package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2

/**
 * HIERARCHY & MODULES
 */

data class AttachmentComponent(
    var parent: Entity = Entity.NONE,
    val offset: Vector2 = Vector2()
) : Component<AttachmentComponent> {
    override fun type() = AttachmentComponent
    companion object : ComponentType<AttachmentComponent>()
}

// Defines a physical slot on a ship chassis
data class SlotComponent(
    val slotType: SlotType,
    val offset: Vector2 = Vector2(),
    var occupiedBy: Entity = Entity.NONE
) : Component<SlotComponent> {
    override fun type() = SlotComponent
    companion object : ComponentType<SlotComponent>()
}

enum class SlotType { SMALL_ARM, LARGE_ARM, UTILITY, CORE, ENGINE }

data class ModuleComponent(
    val category: ModuleCategory,
    var cooldown: Float = 0f,
    var powerRequirement: Float = 10f
) : Component<ModuleComponent> {
    override fun type() = ModuleComponent
    companion object : ComponentType<ModuleComponent>()
}

enum class ModuleCategory {
    PRIMARY, SECONDARY, TERTIARY, QUADRARY, SHIELDS, POWER, DRONES, FIGHTERS
}

/**
 * GROUP MASTER: A virtual entity used to control multiple modules of the same category simultaneously.
 */
data class GroupMasterComponent(
    val category: ModuleCategory
) : Component<GroupMasterComponent> {
    override fun type() = GroupMasterComponent
    companion object : ComponentType<GroupMasterComponent>()
}

/**
 * CONTROL GROUPS: Allows multiple modules to be controlled as a single unit.
 * Examples: All PRIMARY weapons fire together. TERTIARY grouped by Port/Starboard.
 */
data class ControlGroupComponent(
    val name: String
) : Component<ControlGroupComponent> {
    override fun type() = ControlGroupComponent
    companion object : ComponentType<ControlGroupComponent>()
}

// Tag for the module (or group of modules) currently active in the Command Matrix
class SelectedModuleComponent : Component<SelectedModuleComponent> {
    override fun type() = SelectedModuleComponent
    companion object : ComponentType<SelectedModuleComponent>()
}
