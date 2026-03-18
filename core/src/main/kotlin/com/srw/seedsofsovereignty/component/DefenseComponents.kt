package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ArcedDefenseComponent(
    var shieldFore: Float = 100f,
    var shieldAft: Float = 100f,
    var shieldPort: Float = 100f,
    var shieldStbd: Float = 100f,
    var hullFore: Float = 100f,
    var hullAft: Float = 100f,
    var hullPort: Float = 100f,
    var hullStbd: Float = 100f
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
