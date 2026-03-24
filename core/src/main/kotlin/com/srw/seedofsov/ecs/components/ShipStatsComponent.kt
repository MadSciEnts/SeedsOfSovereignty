package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.Subtier
import ktx.ashley.mapperFor

/**
 * Advanced structural and energy integrity manifest.
 * Truth on Disk: Supports binary active/disabled shields with cooldown charge buffers.
 */
class ShipStatsComponent : Component, Pool.Poolable {

    // Core Progression
    var tier: Int = 1 // 1-48
    var subTier: Subtier = Subtier.A // A, B, C

    // Directional Layers (0: Front, 1: Port, 2: Aft, 3: Stbd)
    val shieldQuadrants = floatArrayOf(100f, 100f, 100f, 100f)
    val maxShieldQuadrants = floatArrayOf(100f, 100f, 100f, 100f)

    // Binary Shield State
    val shieldActive = booleanArrayOf(true, true, true, true)
    val shieldChargeBuffer = floatArrayOf(0f, 0f, 0f, 0f)

    val hullQuadrants = floatArrayOf(200f, 200f, 200f, 200f)
    val maxHullQuadrants = floatArrayOf(200f, 200f, 200f, 200f)

    // Power Cores act as sequential lives
    val powerCores = mutableListOf<Float>()
    val maxPowerCores = mutableListOf<Float>()

    // Recharge Logic
    var shieldRechargeRate = 10f
    var targetedQuadrant: Int = -1 // -1 means average distribution
    var priorityRechargeMultiplier = 2.0f

    fun isAlive(): Boolean = powerCores.any { it > 0 }

    override fun reset() {
        tier = 1
        subTier = Subtier.A
        for (i in 0..3) {
            shieldQuadrants[i] = 100f
            maxShieldQuadrants[i] = 100f
            shieldActive[i] = true
            shieldChargeBuffer[i] = 0f
            hullQuadrants[i] = 200f
            maxHullQuadrants[i] = 200f
        }
        powerCores.clear()
        maxPowerCores.clear()
        shieldRechargeRate = 10f
        targetedQuadrant = -1
        priorityRechargeMultiplier = 2.0f
    }

    companion object {
        val mapper = mapperFor<ShipStatsComponent>()
    }
}
