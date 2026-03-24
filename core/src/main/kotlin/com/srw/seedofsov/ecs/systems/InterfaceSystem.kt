package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.*
import com.srw.seedofsov.ui.hud.Hud
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Synchronizes ECS ship state to the HUD display and bridges Command Matrix inputs to Modules.
 * Truth on Disk: Pushes binary shield states and cooldown buffers to the Nautilus display.
 */
class InterfaceSystem(private val hud: Hud) : IteratingSystem(
    allOf(PlayerComponent::class, ShipStatsComponent::class, ShipComponent::class).get(),
    100
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val stats = entity[ShipStatsComponent.mapper] ?: return
        val ship = entity[ShipComponent.mapper] ?: return
        val display = hud.nautilus

        // 1. Sync Ship State -> HUD (Nautilus)
        for (i in 0..3) {
            display.shieldActive[i] = stats.shieldActive[i]
            display.shieldPcts[i] = stats.shieldQuadrants[i] / stats.maxShieldQuadrants[i]
            display.shieldBufferPcts[i] = stats.shieldChargeBuffer[i] / stats.maxShieldQuadrants[i]
            display.hullPcts[i] = stats.hullQuadrants[i] / stats.maxHullQuadrants[i]
        }

        display.corePcts.clear()
        stats.powerCores.forEachIndexed { index, current ->
            display.corePcts.add(current / stats.maxPowerCores[index])
        }

        // 2. Sync HUD -> Module Input Propagation
        ship.slots.forEach { slot ->
            val module = slot.equippedModule ?: return@forEach

            when (module) {
                is BayModuleComponent -> {
                    module.commandPosition.set(slot.getChannel("target_position").valueVector2)
                    val spaceInput = slot.getChannel("cycle_space").valueEnum
                    if (spaceInput is CoordinateSpace) module.commandSpace = spaceInput
                    module.orbitSpeed = slot.getChannel("orbit_speed").valueFloat
                    module.isLaunchRequested = slot.getChannel("launch_unit").valueBoolean
                }
                is WeaponModuleComponent -> {
                    // Inputs handled in WeaponSystem
                }
            }
        }
    }
}
