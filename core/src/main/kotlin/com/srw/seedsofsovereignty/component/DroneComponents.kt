package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2

enum class DroneCategory { ATTACK, DEFEND, REPAIR }

/**
 * The swappable hardware module for drones.
 */
data class DroneModuleComponent(
    var name: String = "Guardian D1",
    var category: DroneCategory = DroneCategory.DEFEND,
    var maxHealth: Float = 50f,
    var maxDrones: Int = 1,
    var rarity: Rarity = Rarity.COMMON,
    val weaponDNA: WeaponModuleComponent = WeaponModuleComponent()
) : Component<DroneModuleComponent> {
    override fun type() = DroneModuleComponent
    companion object : ComponentType<DroneModuleComponent>()
}

/**
 * Live state tracking for a specific drone launcher slot.
 */
data class DroneLauncherComponent(
    var activeDrones: Int = 0,
    var regenRate: Float = 5f,
    var regenTimer: Float = 0f,
    var slotIndex: Int = 0
) : Component<DroneLauncherComponent> {
    override fun type() = DroneLauncherComponent
    companion object : ComponentType<DroneLauncherComponent>()
}

/**
 * Attached to the Player ship to manage fleet directives and UI state.
 */
data class FleetCommandComponent(
    var controlMode: ControlMode = ControlMode.ATTACK_DIRECTION,
    var selectedSlotIndex: Int = 0,
    val slotFocusPoints: Array<Vector2> = Array(4) { Vector2(0f, 150f) },
    val slotFocusAngles: FloatArray = FloatArray(4) { 1.57f },
    var command: Command = Command.DEFEND,

    // Persistence for dual-mode joystick
    val lastWeaponAim: Vector2 = Vector2(0f, 0.1f), // Small non-zero default
    val lastDroneAim: Vector2 = Vector2(0f, 0f),

    // Localization Mode
    var localization: LocalizationMode = LocalizationMode.CAMERA
) : Component<FleetCommandComponent> {
    enum class ControlMode { ATTACK_DIRECTION, DRONE_FOCUS }
    enum class Command { ATTACK, DEFEND, REPAIR }
    enum class LocalizationMode { CAMERA, SHIP }
    override fun type() = FleetCommandComponent
    companion object : ComponentType<FleetCommandComponent>()
}

/**
 * The actual drone entity component.
 */
data class DroneComponent(
    val owner: Entity,
    var slotIndex: Int = 0,
    var orbitRadius: Float = 80f,
    var currentOrbitAngle: Float = 0f
) : Component<DroneComponent> {
    override fun type() = DroneComponent
    companion object : ComponentType<DroneComponent>()
}
