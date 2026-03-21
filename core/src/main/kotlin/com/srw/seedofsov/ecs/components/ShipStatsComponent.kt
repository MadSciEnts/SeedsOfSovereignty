package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Advanced structural and energy integrity manifest.
 * Supports directional quadrants and sequential power core 'lives'.
 */
class ShipStatsComponent : Component, Pool.Poolable {
    // Directional Layers (0: Top-Right, 1: Top-Left, 2: Bottom-Left, 3: Bottom-Right)
    val shieldQuadrants = floatArrayOf(100f, 100f, 100f, 100f)
    val maxShieldQuadrants = floatArrayOf(100f, 100f, 100f, 100f)

    val hullQuadrants = floatArrayOf(200f, 200f, 200f, 200f)
    val maxHullQuadrants = floatArrayOf(200f, 200f, 200f, 200f)

    // Power Cores act as sequential lives
    val powerCores = mutableListOf<Float>()
    val maxPowerCores = mutableListOf<Float>()

    fun isAlive(): Boolean = powerCores.any { it > 0 }

    override fun reset() {
        for (i in 0..3) {
            shieldQuadrants[i] = 100f
            maxShieldQuadrants[i] = 100f
            hullQuadrants[i] = 200f
            maxHullQuadrants[i] = 200f
        }
        powerCores.clear()
        maxPowerCores.clear()
    }

    companion object {
        val mapper = mapperFor<ShipStatsComponent>()
    }
}
