package com.srw.seedofsov.model

import com.badlogic.gdx.graphics.Color

/**
 * Defines damage types for weapon-shield interaction logic.
 * Truth on Disk: Categorized for hierarchical ship defense interactions.
 */
enum class DamageType {
    KINETIC,   // Balanced
    ION,       // Strong vs Shields, Weak vs Hull
    PLASMA,    // Slow, Splash, Balanced
    LASER      // Strong vs Hull, Weak vs Shields
}

/**
 * Types of visual effects for the EffectComponent.
 */
enum class EffectType {
    MUZZLE_FLASH,
    SHIELD_IMPACT,
    DEBRIS,
    MOTION_TRAIL,
    WPN_TRACE,
    EXPLOSION,
    ATMOSPHERE_VENT,
    ELECTRICAL_ARC,
    RIBBON
}

/**
 * High-level categories for HUD-specific visual effects.
 */
enum class HudFxType {
    SHIELD_MANIFOLD,
    ENGINE_DIAGNOSTIC,
    POWER_CORE,      // Central energy node visualization
    NONE
}

/**
 * Defines how a vector position should be interpreted by systems.
 */
enum class CoordinateSpace {
    OBJECT, // Relative to the parent ship
    SCREEN, // Relative to the device screen
    WORLD   // Absolute world coordinates
}

/**
 * Shared data for visual feedback.
 */
data class VisualMetadata(
    val mainColor: Color = Color.WHITE,
    val secondaryColor: Color = Color.CYAN,
    val pulseIntensity: Float = 1.0f,
    val flickerSpeed: Float = 0.1f
)
