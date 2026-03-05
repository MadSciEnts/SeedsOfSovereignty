package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class EvolutionComponent(
    var currentTier: Int = 1,
    var subTier: Int = 1,
    var essenceCollected: Int = 0,
    var essenceRequired: Int = 5
) : Component<EvolutionComponent> {
    override fun type() = EvolutionComponent
    companion object : ComponentType<EvolutionComponent>()
}

data class EssenceComponent(
    var amount: Int = 1,
    var isCollected: Boolean = false
) : Component<EssenceComponent> {
    override fun type() = EssenceComponent
    companion object : ComponentType<EssenceComponent>()
}

data class DroneComponent(
    val owner: Entity,
    var orbitAngle: Float = 0f,
    var orbitRadius: Float = 50f,
    var orbitSpeed: Float = 2f
) : Component<DroneComponent> {
    override fun type() = DroneComponent
    companion object : ComponentType<DroneComponent>()
}
