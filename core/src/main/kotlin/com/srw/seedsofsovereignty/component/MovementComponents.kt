package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * Steering and Autonomous Movement Components (v2.7.5).
 */
data class SteeringComponent(
    var maxForce: Float = 5f, // GSF 0.1: 50 -> 5
    var maxSpeed: Float = 12f, // GSF 0.1: 120 -> 12
    var targetEntity: Entity? = null,
    var steeringMode: SteeringMode = SteeringMode.SEEK
) : Component<SteeringComponent> {
    enum class SteeringMode { SEEK, FLEE, ARRIVE, WANDER }
    override fun type() = SteeringComponent
    companion object : ComponentType<SteeringComponent>()
}
