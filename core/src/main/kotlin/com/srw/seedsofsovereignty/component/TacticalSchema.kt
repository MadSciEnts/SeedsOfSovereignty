package com.srw.seedsofsovereignty.component

/**
 * Defines the supported tactile controls for the Command Matrix (v0.3.0).
 */
enum class TacticalInputType {
    MOMENTARY_BUTTON,   // Active while pressed (Fire)
    TOGGLE_BUTTON,      // Flips state on tap (Localization)
    VECTOR_STICK,       // 360-degree direction (Aim)
    FOCUS_STICK,        // Persistent position stick (Drones)
    LINEAR_SLIDER       // Range input (Power distribution)
}

/**
 * Logical anchoring within the 3x3 Tactical Grid.
 */
enum class MatrixAnchor {
    CENTER,
    TOP, BOTTOM, LEFT, RIGHT,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    DIAMOND_NORTH, DIAMOND_SOUTH, DIAMOND_WEST, DIAMOND_EAST
}

/**
 * A specific input requirement defined by hardware.
 */
data class InputRequirement(
    val type: TacticalInputType,
    val anchor: MatrixAnchor,
    val actionKey: String, // e.g., "FIRE", "SHIELD_FRONT", "DRONE_FOCUS"
    val label: String = ""
)

/**
 * The HUD Manifest provided by a module.
 * Replaces hardcoded ControlType logic.
 */
data class TacticalSchema(
    val inputs: List<InputRequirement> = emptyList()
)
