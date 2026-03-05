package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class HealthComponent(
    var current: Float = 100f,
    var max: Float = 100f
) : Component<HealthComponent> {
    override fun type() = HealthComponent
    companion object : ComponentType<HealthComponent>()
}

data class ShieldComponent(
    var current: Float = 50f,
    var max: Float = 50f,
    var rechargeRate: Float = 5f, // Per second
    var active: Boolean = true
) : Component<ShieldComponent> {
    override fun type() = ShieldComponent
    companion object : ComponentType<ShieldComponent>()
}
