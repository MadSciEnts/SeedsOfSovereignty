package com.srw.seedsofsovereignty.component

/**
 * Descriptive hardware classification for bidirectional instruments.
 */
enum class ControlClassification {
    ACTION_BUTTON, MOVEMENT_STICK, TACTICAL_STICK, TOGGLE_SWITCH, DIAMOND_GRID, MODIFIER_SLIDER, STATUS_LIGHT
}

/**
 * A bidirectional input/output channel defined by a hardware module.
 * v0.4.0: Transitioned to chained relative positioning and signal feedback.
 */
data class InputChannel(
    val classification: ControlClassification,
    val actionKey: String, // Key for pushing control data to the module
    val signalKey: String? = null, // Optional key for pulling feedback data from the module
    val relX: Float = 0f, // X offset relative to the previous channel in the list
    val relY: Float = 0f, // Y offset relative to the previous channel in the list
    val width: Float = 250f, // Physical footprint width
    val height: Float = 250f, // Physical footprint height
    val label: String? = null,
    val color: String? = null, // Skin color identifier
    val icon: String? = null, // Asset identifier
    val styleId: String? = null // Texture collection reference, e.g., "INDUSTRIAL"
)

/**
 * The HUD Manifest provided by a module.
 * v0.4.0: Now holds a list of InputChannels for bidirectional instrumentation.
 */
data class TacticalSchema(
    val inputs: List<InputChannel> = emptyList()
)
