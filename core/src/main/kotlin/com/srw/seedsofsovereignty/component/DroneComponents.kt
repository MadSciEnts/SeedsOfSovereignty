package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class DroneComponent(
    val owner: Entity,
    var orbitRadius: Float = 50f,
    var orbitSpeed: Float = 2f
) : Component<DroneComponent> {
    override fun type() = DroneComponent
    companion object : ComponentType<DroneComponent>()
}

data class DroneLauncherComponent(
    var droneType: String = "Kinetic",
    var behaviorMode: BehaviorMode = BehaviorMode.DEFENSIVE,
    var maxDrones: Int = 1,
    var activeDrones: Int = 0,
    var regenRate: Float = 5f, // Seconds to rebuild
    var regenTimer: Float = 0f
) : Component<DroneLauncherComponent> {
    enum class BehaviorMode { OFFENSIVE, DEFENSIVE, STRATEGIC }
    override fun type() = DroneLauncherComponent
    companion object : ComponentType<DroneLauncherComponent>()
}

data class FleetCommandComponent(
    var command: Command = Command.DEFEND
) : Component<FleetCommandComponent> {
    enum class Command { ATTACK_NEAREST, DEFEND, ATTACK_STRONGEST }
    override fun type() = FleetCommandComponent
    companion object : ComponentType<FleetCommandComponent>()
}
