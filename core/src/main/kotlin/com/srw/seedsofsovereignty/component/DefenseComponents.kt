package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ArcedDefenseComponent(
    val shieldFore: Float = 100f,
    val shieldAft: Float = 100f,
    val shieldPort: Float = 100f,
    val shieldStbd: Float = 100f,
    val hullFore: Float = 100f,
    val hullAft: Float = 100f,
    val hullPort: Float = 100f,
    val hullStbd: Float = 100f
) : Component<ArcedDefenseComponent> {
    override fun type() = ArcedDefenseComponent
    companion object : ComponentType<ArcedDefenseComponent>()
}

data class PowerCoreComponent(
    var integrity: Float = 1.0f, // 1.0 = 100% health
    var weaponPower: Float = 0.25f,
    var shieldPower: Float = 0.25f,
    var enginePower: Float = 0.25f,
    var utilityPower: Float = 0.25f,
    var baseOutput: Float = 100f,
    var throttle: Float = 1.0f // Calculated penalty [0.0 to 1.0]
) : Component<PowerCoreComponent> {
    override fun type() = PowerCoreComponent
    companion object : ComponentType<PowerCoreComponent>()
}
