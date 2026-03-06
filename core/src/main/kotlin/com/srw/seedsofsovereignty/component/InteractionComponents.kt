package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class SensorComponent(
    var radius: Float = 1000f // Matches SYSTEM_MINIMAP_SENSORS.md spec
) : Component<SensorComponent> {
    override fun type() = SensorComponent
    companion object : ComponentType<SensorComponent>()
}

class VisibleComponent : Component<VisibleComponent> {
    override fun type() = VisibleComponent
    companion object : ComponentType<VisibleComponent>()
}

data class StationComponent(
    val name: String = "Outpost",
    val radius: Float = 150f
) : Component<StationComponent> {
    override fun type() = StationComponent
    companion object : ComponentType<StationComponent>()
}

/**
 * Holds modules offered for trade or sale.
 * Players can choose to Swap (Replace current) or Scrap (Destroy for Essence).
 */
data class MarketComponent(
    val weaponOffers: MutableList<WeaponModuleComponent> = mutableListOf(),
    var essencePrice: Int = 0, // Base price multiplier or flat fee
    var refreshTimer: Float = 60f // Seconds until stock changes
) : Component<MarketComponent> {
    override fun type() = MarketComponent
    companion object : ComponentType<MarketComponent>()
}

data class DockingComponent(
    var isDocked: Boolean = false,
    var nearestStation: Entity? = null,
    var proximity: Float = 0f,
    var undockCooldown: Float = 0f
) : Component<DockingComponent> {
    override fun type() = DockingComponent
    companion object : ComponentType<DockingComponent>()
}

data class ApertureComponent(
    var openProgress: Float = 0f,
    var state: State = State.CLOSED
) : Component<ApertureComponent> {
    enum class State { CLOSED, OPENING, OPEN, CLOSING }
    override fun type() = ApertureComponent
    companion object : ComponentType<ApertureComponent>()
}
