package com.srw.seedofsov.model

/**
 * Defines a specific UI control required by a module.
 */
data class ModuleControl(
    val type: String, // e.g., "JOYSTICK", "SLIDER", "BUTTON"
    val channelName: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * Interface for all ship modules.
 */
interface ShipModule {
    val category: ModuleCategory
    val name: String

    /**
     * Returns the list of UI controls required for this module.
     * This defines the Command Matrix layout for the module.
     */
    fun getModuleControls(): List<ModuleControl>
}
