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
    var currentRadius: Float = 0f, // The actual radius used by both Ring and Drones
    var currentRotation: Float = 0f // The actual rotation used by both Ring and Drones
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
 * Interface for all ship modules.
 * Truth on Disk: The module controls the tactical projection parameters.
 */
interface ShipModule {
    val category: ModuleCategory
    val name: String
    val tacticalState: TacticalState

    /**
     * Defines the radius of the tactical ring/orbit when fully projected.
     */
    fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 2.5f

    /**
     * Defines the rotation speed/angle of the tactical ring/orbit.
     */
    fun getTacticalRotation(): Float = 0f

    fun getModuleControls(): List<ModuleControl>
    fun getTacticalAttributes(): List<TacticalAttribute> = emptyList()
}
