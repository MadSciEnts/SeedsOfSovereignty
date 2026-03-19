package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2

/**
 * ATTACHMENT: Child-parent relationship.
 */
data class AttachmentComponent(
    var parent: Entity = Entity.NONE,
    val offset: Vector2 = Vector2()
) : Component<AttachmentComponent> {
    override fun type() = AttachmentComponent
    companion object : ComponentType<AttachmentComponent>()
}

/**
 * SLOT: Physical mounting point.
 */
data class SlotComponent(
    val slotType: SlotType,
    val offset: Vector2 = Vector2()
) : Component<SlotComponent> {
    override fun type() = SlotComponent
    companion object : ComponentType<SlotComponent>()
}

enum class SlotType { CORE, ENGINE, WING, TURRET }

/**
 * MODULE: Logic and Resource data for hardware components.
 */
data class ModuleComponent(
    val category: ModuleCategory,
    var powerCategory: PowerCategory = PowerCategory.NONE,
    var isActive: Boolean = true
) : Component<ModuleComponent> {
    override fun type() = ModuleComponent
    companion object : ComponentType<ModuleComponent>()
}

enum class ModuleCategory {
    PRIMARY, SECONDARY, TERTIARY, QUADRARY, SHIELDS, POWER, DRONES, FIGHTERS, NAVIGATION, ENGINE
}

/**
 * MASTER: Logic bridge for grouped module control.
 */
data class GroupMasterComponent(
    val category: ModuleCategory
) : Component<GroupMasterComponent> {
    override fun type() = GroupMasterComponent
    companion object : ComponentType<GroupMasterComponent>()
}

/**
 * SELECTION: UI focus tag.
 */
class SelectedModuleComponent : Component<SelectedModuleComponent> {
    override fun type() = SelectedModuleComponent
    companion object : ComponentType<SelectedModuleComponent>()
}
