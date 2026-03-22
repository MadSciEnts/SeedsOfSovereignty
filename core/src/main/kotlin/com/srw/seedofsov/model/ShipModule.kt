package com.srw.seedofsov.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * Common state for any module participating in the tactical overlay.
 */
data class TacticalState(
    val worldTargetPos: Vector2 = Vector2(),
    var lastStableAwayAngle: Float = 90f,
    var localAwayAngle: Float = 0f,
    var heightSquish: Float = 1f,
    var aimTransition: Float = 0f,
    var selectionAnimation: Float = 0f,
    var isSelected: Boolean = false,
    var layoutOrbitIndex: Int = 0,
    val lerpedOffset: Vector2 = Vector2(),
    var currentRadius: Float = 0f,
    var currentRotation: Float = 0f,
    var firePulse: Float = 0f // Ender's Game kinetic feedback
) {
    fun reset() {
        worldTargetPos.set(0f, 0f)
        lastStableAwayAngle = 90f
        localAwayAngle = 0f
        heightSquish = 1f
        aimTransition = 0f
        selectionAnimation = 0f
        isSelected = false
        layoutOrbitIndex = 0
        lerpedOffset.set(0f, 0f)
        currentRadius = 0f
        currentRotation = 0f
        firePulse = 0f
    }
}

data class ModuleControl(
    val type: String,
    val channelName: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

enum class TacticalProjection {
    FLAT,
    PROJECTED
}

enum class TacticalAttributeType {
    CONTINUOUS,
    DISCRETE,
    DIRECTIONAL,
    ICON
}

data class TacticalAttribute(
    val label: String,
    val current: Float,
    val max: Float,
    val type: TacticalAttributeType,
    val preferredQuadrant: Int = 0,
    val colorOverride: Color? = null,
    val projection: TacticalProjection = TacticalProjection.FLAT,
    val iconName: String? = null
)

/**
 * Defines how the tactical ring's orientation is derived.
 */
enum class TacticalOrientationReality {
    SHIP_RELATIVE,   // Follows hull rotation
    WORLD_FIXED,     // Independent of hull (e.g. Joystick)
    AWAY_FROM_SHIP   // Always faces away from ship center (Spherical Projection)
}

/**
 * Interface for all ship modules.
 * Truth on Disk: Supports direct hardware-reporting of absolute world orientation.
 */
interface ShipModule {
    val category: ModuleCategory
    val name: String
    val tacticalState: TacticalState

    // Intent Projection Properties
    val commandPosition: Vector2
    var commandSpace: CoordinateSpace
    var orientationReality: TacticalOrientationReality

    /**
     * Authority: The Absolute World Orientation of this module's functional axis.
     * Updated by specialized systems (e.g. WeaponSystem) to ensure HUD alignment.
     */
    var worldAimAngle: Float

    fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 2.5f
    fun getTacticalRotation(): Float = 0f

    fun getModuleControls(): List<ModuleControl>
    fun getTacticalAttributes(): List<TacticalAttribute> = emptyList()
}
