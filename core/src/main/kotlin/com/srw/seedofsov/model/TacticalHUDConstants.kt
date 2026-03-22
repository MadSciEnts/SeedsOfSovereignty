package com.srw.seedofsov.model

import com.badlogic.gdx.math.Vector2

/**
 * Shared constants for the Tactical Overlay system.
 * Truth on Disk: Ensures alignment between visual rings and physical drone orbits.
 */
object TacticalHUDConstants {
    // Reduced by 30% for a cleaner, more refined look (0.85 -> 0.6)
    const val RING_RADIAL_THICKNESS = 0.6f
    const val EXTRA_RING_SPACING = 0.15f

    // Visual depth for blocky segments
    const val RING_VISUAL_DEPTH = 0.15f

    const val INNER_START_OFFSET = 1.25f

    // Scale for touchpad input projection
    const val TACTICAL_RANGE_MULTIPLIER = 6.0f

    /**
     * Calculates the radius of a specific ring based on its orbit index.
     */
    fun getRingRadius(shipRadius: Float, orbitIndex: Int): Float {
        return shipRadius + INNER_START_OFFSET +
               (orbitIndex * (RING_RADIAL_THICKNESS + EXTRA_RING_SPACING)) +
               (RING_RADIAL_THICKNESS / 2f)
    }

    /**
     * Standard ship radius calculation for HUD and Orbits.
     */
    fun calculateShipRadius(size: Vector2): Float {
        return (size.x + size.y) / 4f
    }
}
