package com.srw.seedofsov.model

import com.badlogic.gdx.graphics.Color

/**
 * Defines damage types for weapon-shield interaction logic.
 */
enum class DamageType {
    KINETIC,   // Strong vs Hull, Weak vs Shields
    ENERGY,    // Strong vs Shields, Weak vs Hull
    PLASMA,    // Balanced, Splash Damage
    EXPLOSIVE  // High AOE, Component Damage
}

/**
 * Types of visual effects for the EffectComponent.
 */
enum class EffectType {
    MUZZLE_FLASH,
    SHIELD_IMPACT,
    DEBRIS,
    MOTION_TRAIL,
    WPN_TRACE
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
