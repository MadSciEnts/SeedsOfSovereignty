package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.ShieldModuleComponent
import com.srw.seedofsov.ecs.components.ShipStatsComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages shield integrity and dynamic power diversion.
 * Truth on Disk: Diverts recharge/cooldown energy and calculates priority quadrant based on touchpad.
 */
class ShieldSystem : IteratingSystem(allOf(ShipStatsComponent::class, ShieldModuleComponent::class).get()) {

    private val weights = floatArrayOf(0f, 0f, 0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val stats = entity[ShipStatsComponent.mapper] ?: return
        val module = entity[ShieldModuleComponent.mapper] ?: return

        // 1. Resolve State Transitions
        for (i in 0..3) {
            if (stats.shieldActive[i] && stats.shieldQuadrants[i] <= 0f) {
                stats.shieldActive[i] = false
                stats.shieldChargeBuffer[i] = 0f
            }
        }

        // 2. Calculate Directional Power Ratios (Touchpad Diversion)
        val cp = module.commandPosition
        weights[0] = Math.max(0f, cp.y)  // Front
        weights[1] = Math.max(0f, -cp.x) // Left
        weights[2] = Math.max(0f, -cp.y) // Rear
        weights[3] = Math.max(0f, cp.x)  // Right

        val totalBias = weights[0] + weights[1] + weights[2] + weights[3]
        val totalEnergy = module.rechargeRate * deltaTime

        // Update Priority Quadrant based on dominant bias (threshold 0.2)
        if (totalBias > 0.2f) {
            var maxIdx = -1
            var maxWeight = 0f
            for (i in 0..3) {
                if (weights[i] > maxWeight) {
                    maxWeight = weights[i]
                    maxIdx = i
                }
            }
            module.priorityQuadrant = maxIdx
        } else {
            module.priorityQuadrant = -1
        }

        if (totalBias > 0.05f) {
            for (i in 0..3) {
                val ratio = weights[i] / totalBias
                processQuadrantEnergy(stats, i, totalEnergy * ratio)
            }
        } else {
            val energyPerQuadrant = totalEnergy / 4f
            for (i in 0..3) {
                processQuadrantEnergy(stats, i, energyPerQuadrant)
            }
        }

        // 3. Sync State to Module for Tactical UI
        var currentTotal = 0f
        for (i in 0..3) {
            currentTotal += stats.shieldQuadrants[i]
            module.quadrantHealth[i] = stats.shieldQuadrants[i]
            module.quadrantMax[i] = stats.maxShieldQuadrants[i]
            module.quadrantActive[i] = stats.shieldActive[i]
            module.quadrantBuffer[i] = stats.shieldChargeBuffer[i]
        }
        module.shieldStrength = currentTotal
    }

    private fun processQuadrantEnergy(stats: ShipStatsComponent, index: Int, amount: Float) {
        if (stats.shieldActive[index]) {
            if (stats.shieldQuadrants[index] < stats.maxShieldQuadrants[index]) {
                stats.shieldQuadrants[index] = (stats.shieldQuadrants[index] + amount).coerceAtMost(stats.maxShieldQuadrants[index])
            }
        } else {
            stats.shieldChargeBuffer[index] += amount
            if (stats.shieldChargeBuffer[index] >= stats.maxShieldQuadrants[index]) {
                stats.shieldQuadrants[index] = stats.maxShieldQuadrants[index]
                stats.shieldActive[index] = true
                stats.shieldChargeBuffer[index] = 0f
            }
        }
    }
}
