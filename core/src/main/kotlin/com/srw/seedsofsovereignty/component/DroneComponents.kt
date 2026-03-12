package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2

enum class DroneCategory { ATTACK, DEFEND, REPAIR }

/**
 * The swappable hardware module for drones (v2.7.5).
 * FIXES:
 * 1. [GSF 0.1] - Scaled orbit logic to 0.1 factor.
 */
data class DroneModuleComponent(
    var name: String = "Guardian D1",
    var category: DroneCategory = DroneCategory.DEFEND,
    var maxHealth: Float = 50f,
    var maxDrones: Int = 1,
    var rarity: Rarity = Rarity.COMMON,
    var tacticalSchema: TacticalSchema = TacticalSchema(listOf(
        InputChannel(ControlClassification.TACTICAL_STICK, "DRONE_MOVE", relX = 0f, relY = 0f),
        InputChannel(ControlClassification.ACTION_BUTTON, "DRONE_DEPLOY", label = "DEPLOY", relX = 260f, relY = 0f)
    )),
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
    val slotFocusPoints: Array<Vector2> = Array(4) { Vector2(0f, 15f) }, // GSF 0.1: 150 -> 15
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
    var orbitRadius: Float = 8f, // GSF 0.1: 80 -> 8
    var currentOrbitAngle: Float = 0f
) : Component<DroneComponent> {
    override fun type() = DroneComponent
    companion object : ComponentType<DroneComponent>()
}
