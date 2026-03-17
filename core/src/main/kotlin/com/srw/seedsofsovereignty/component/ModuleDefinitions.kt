package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.math.Vector2

/**
 * MODULE TYPES: Defines the unique classes of modules.
 */
enum class ModuleType {
    KINETIC_CANNON,
    PULSE_LASER,
    SHIELD_GEN,
    POWER_CORE_T1,
    DRONE_HIVE,
    FIGHTER_BAY
}

/**
 * CONTROL WIDGET TYPES
 */
enum class ControlWidgetType {
    JOYSTICK_2D,
    TOUCHPAD_2D,
    BUTTON_DISCRETE,
    SLIDER_1D
}

data class ControlWidgetDefinition(
    val widgetType: ControlWidgetType,
    val channelKey: String,
    val relativePosition: Vector2,
    val orientation: Float = 0f,
    val label: String = ""
)

data class OutputWidgetDefinition(
    val channelKey: String,
    val relativePosition: Vector2,
    val label: String = ""
)

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
        // Tier 1 Kinetic Cannon
        definitions[ModuleType.KINETIC_CANNON] = ModuleTypeDefinition(
            type = ModuleType.KINETIC_CANNON,
            controls = listOf(
                ControlWidgetDefinition(ControlWidgetType.JOYSTICK_2D, "AIM", Vector2(0.5f, 0.4f), label = "AIM")
            ),
            outputs = listOf(
                OutputWidgetDefinition("HEAT", Vector2(0.5f, 0.9f), "TEMP")
            )
        )

        // Shield Generator
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

        // Power Core
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

        // Drone Hive: Manages autonomous defense drones
        definitions[ModuleType.DRONE_HIVE] = ModuleTypeDefinition(
            type = ModuleType.DRONE_HIVE,
            controls = listOf(
                ControlWidgetDefinition(ControlWidgetType.BUTTON_DISCRETE, "LAUNCH", Vector2(0.5f, 0.5f), label = "DEPLOY")
            ),
            outputs = listOf(
                OutputWidgetDefinition("COUNT", Vector2(0.5f, 0.1f), "ACTIVE")
            )
        )
    }

    fun get(type: ModuleType) = definitions[type] ?: throw IllegalArgumentException("No definition for $type")
}
