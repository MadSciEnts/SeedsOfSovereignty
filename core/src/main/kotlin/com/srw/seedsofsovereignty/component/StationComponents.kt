package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * SPACE STATIONS & COMMERCE
 */

data class StationComponent(
    val name: String = "Outpost",
    val dockingRange: Float = 5f,
    var isPlayerDocked: Boolean = false,
    var maxDefenders: Int = 6,
    var spawnInterval: Float = 5f,
    var spawnTimer: Float = 0f
) : Component<StationComponent> {
    override fun type() = StationComponent
    companion object : ComponentType<StationComponent>()
}

data class ShopComponent(
    val categories: MutableList<ModuleCategory> = mutableListOf(),
    val inventory: MutableList<Entity> = mutableListOf()
) : Component<ShopComponent> {
    override fun type() = ShopComponent
    companion object : ComponentType<ShopComponent>()
}

data class InventoryComponent(
    var credits: Long = 0,
    val stashedModules: MutableList<Entity> = mutableListOf()
) : Component<InventoryComponent> {
    override fun type() = InventoryComponent
    companion object : ComponentType<InventoryComponent>()
}
