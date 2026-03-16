package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.math.Vector2

/**
 * MODULE TYPES: Defines the unique classes of modules.
 * Multiple instances of the same type can exist with different properties.
 */
enum class ModuleType {
    KINETIC_CANNON,
    PULSE_LASER,
    SHIELD_GEN,
    POWER_CORE_T1,
    DRONE_HIVE
}

/**
 * CONTROL WIDGET TYPES: The physical UI elements in the Command Matrix.
 */
enum class ControlWidgetType {
    JOYSTICK_2D,
    TOUCHPAD_2D,
    BUTTON_DISCRETE,
    SLIDER_1D
}

/**
 * CONTROL DEFINITION: Metadata for how a specific control is rendered and mapped.
 */
data class ControlWidgetDefinition(
    val widgetType: ControlWidgetType,
    val channelKey: String,          // Maps to Input channel map keys
    val relativePosition: Vector2,   // 0.0 to 1.0 within the Control Area
    val orientation: Float = 0f,     // Rotation of the widget (e.g. for sliders)
    val label: String = ""
)

/**
 * OUTPUT DEFINITION: Metadata for rendering status bars/labels from module outputs.
 */
data class OutputWidgetDefinition(
    val channelKey: String,          // Maps to Output channel map keys
    val relativePosition: Vector2,
    val label: String = ""
)

/**
 * MODULE TYPE DEFINITION: The static "blueprint" for a module's control scheme.
 */
data class ModuleTypeDefinition(
    val type: ModuleType,
    val controls: List<ControlWidgetDefinition>,
    val outputs: List<OutputWidgetDefinition> = emptyList()
)

/**
 * MODULE REGISTRY: Centralized definitions for all module types.
 */
object ModuleRegistry {
    private val definitions = mutableMapOf<ModuleType, ModuleTypeDefinition>()

    init {
        // Define Tier 1 Kinetic Cannon: Joystick for Aiming + Heat Output
        definitions[ModuleType.KINETIC_CANNON] = ModuleTypeDefinition(
            type = ModuleType.KINETIC_CANNON,
            controls = listOf(
                ControlWidgetDefinition(
                    ControlWidgetType.JOYSTICK_2D,
                    "AIM",
                    Vector2(0.5f, 0.4f),
                    label = "AIM"
                )
            ),
            outputs = listOf(
                OutputWidgetDefinition("HEAT", Vector2(0.5f, 0.9f), "TEMP")
            )
        )

        // Define Shield Generator: 5-Way Buttons for Directional Bias + Shield Strength Output
        definitions[ModuleType.SHIELD_GEN] = ModuleTypeDefinition(
            type = ModuleType.SHIELD_GEN,
            controls = listOf(
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "FORE", Vector2(0.5f, 0.8f), label = "FORE"),
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "AFT", Vector2(0.5f, 0.2f), label = "AFT"),
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "PORT", Vector2(0.2f, 0.5f), label = "PORT"),
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "STBD", Vector2(0.8f, 0.5f), label = "STBD"),
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "CENTER", Vector2(0.5f, 0.5f), label = "BAL")
            ),
            outputs = listOf(
                OutputWidgetDefinition("CHARGE", Vector2(0.5f, 0.05f), "SHIELD")
            )
        )

        // Define Power Core: 4x Sliders for Resource Allocation
        definitions[ModuleType.POWER_CORE_T1] = ModuleTypeDefinition(
            type = ModuleType.POWER_CORE_T1,
            controls = listOf(
                ControlWidgetDefinition(ControlWidgetType.SLIDER_1D, "WEAPON", Vector2(0.2f, 0.5f), orientation = 90f, label = "WPN"),
                ControlWidgetDefinition(ControlWidgetType.SLIDER_1D, "SHIELD", Vector2(0.4f, 0.5f), orientation = 90f, label = "SHD"),
                ControlWidgetDefinition(ControlWidgetType.SLIDER_1D, "ENGINE", Vector2(0.6f, 0.5f), orientation = 90f, label = "ENG"),
                ControlWidgetDefinition(ControlWidgetType.SLIDER_1D, "UTILITY", Vector2(0.8f, 0.5f), orientation = 90f, label = "UTL")
            ),
            outputs = listOf(
                OutputWidgetDefinition("TOTAL_LOAD", Vector2(0.5f, 0.95f), "LOAD")
            )
        )
    }

    fun get(type: ModuleType) = definitions[type] ?: throw IllegalArgumentException("No definition for $type")
}
