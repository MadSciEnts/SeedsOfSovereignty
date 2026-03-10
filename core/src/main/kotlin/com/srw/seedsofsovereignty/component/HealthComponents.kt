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

data class ShieldModuleComponent(
    val tier: Int = 1,
    var currentHP: Float = 100f,
    var maxHP: Float = 100f,
    var rechargeRate: Float = 5f
) : Component<ShieldModuleComponent> {
    override fun type() = ShieldModuleComponent
    companion object : ComponentType<ShieldModuleComponent>()
}

data class ArmorModuleComponent(
    val tier: Int = 1,
    var currentHP: Float = 100f,
    var maxHP: Float = 100f,
    val flatReduction: Float = 1f
) : Component<ArmorModuleComponent> {
    override fun type() = ArmorModuleComponent
    companion object : ComponentType<ArmorModuleComponent>()
}

data class HullModuleComponent(
    val tier: Int = 1,
    var currentHP: Float = 200f,
    var maxHP: Float = 200f,
    val integrityBonus: Float = 0.1f // % bonus to systemsIntegrity stability
) : Component<HullModuleComponent> {
    override fun type() = HullModuleComponent
    companion object : ComponentType<HullModuleComponent>()
}

data class QuadrantHealth(
    val shieldLayers: MutableList<Float> = mutableListOf(),
    val hullLayers: MutableList<Float> = mutableListOf()
)

data class QuadrantHealthComponent(
    val front: QuadrantHealth = QuadrantHealth(),
    val rear: QuadrantHealth = QuadrantHealth(),
    val left: QuadrantHealth = QuadrantHealth(),
    val right: QuadrantHealth = QuadrantHealth(),
    var systemsIntegrity: Float = 100f
) : Component<QuadrantHealthComponent> {
    override fun type() = QuadrantHealthComponent
    companion object : ComponentType<QuadrantHealthComponent>()
}
