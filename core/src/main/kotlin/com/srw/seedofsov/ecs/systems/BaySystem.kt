package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.EntityFactory
import com.srw.seedofsov.ecs.components.*
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Manages the deployment and advanced orbital flocking of Drones.
 * Truth on Disk: The module defines the orbit via TacticalState.
 * This system follows the lerped currentRadius and currentRotation from TacticalSystem.
 */
class BaySystem(private val factory: EntityFactory) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class).get(),
    35 // Priority after TacticalSystem (15)
) {
    private val orbitVec = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return

        ship.slots.forEach { slot ->
            val module = slot.equippedModule
            if (module is BayModuleComponent) {
                updateBay(entity, transform, module, deltaTime)
            }
        }
    }

    private fun updateBay(owner: Entity, transform: TransformComponent, bay: BayModuleComponent, deltaTime: Float) {
        // 1. Cleanup & Smart Launch
        bay.activeUnits.removeAll { it.isScheduledForRemoval }

        if (bay.activeUnits.size < bay.capacity) {
            bay.launchTimer -= deltaTime
            if (bay.launchTimer <= 0f && bay.isLaunchRequested) {
                val droneEntity = factory.createDrone(transform.position.x, transform.position.y, owner)
                bay.activeUnits.add(droneEntity)
                bay.launchTimer = bay.launchRate
            }
        }

        // 2. Swarm Logic (Physics Driven)
        // The Module calculates its own swarmRotation, which is then mirrored by the Tactical HUD.
        val signedControl = (bay.orbitSpeed - 0.5f) * 2f
        val currentBaseSpeed = 90f * signedControl
        bay.swarmRotation += currentBaseSpeed * deltaTime

        // 3. Synchronized Orbit Physics
        val state = bay.tacticalState
        val droneCount = bay.activeUnits.size
        if (droneCount == 0) return

        val idealGap = 360f / droneCount.coerceAtLeast(1)
        val drones = bay.activeUnits.mapNotNull { it[DroneComponent.mapper] }

        bay.activeUnits.forEach { unit ->
            val seeking = unit[SeekingComponent.mapper] ?: return@forEach
            val drone = unit[DroneComponent.mapper] ?: return@forEach

            seeking.ownerEntity = owner
            drone.orbitPhase += currentBaseSpeed * deltaTime

            // Flocking / Spacing logic
            drones.forEach { other ->
                if (other === drone) return@forEach
                var diff = drone.orbitPhase - other.orbitPhase
                while (diff <= -180f) diff += 360f
                while (diff > 180f) diff -= 360f

                if (diff.absoluteValue < idealGap) {
                    val overlap = 1f - (diff.absoluteValue / idealGap)
                    val pushStrength = overlap * (80f + currentBaseSpeed.absoluteValue)
                    drone.orbitPhase += diff.sign * pushStrength * deltaTime
                }
            }

            // --- TRUTH ON DISK: PERFECT ALIGNMENT ---
            // The drone follows the EXACT lerped radius calculated in TacticalSystem.
            // This lerp starts at the Flat Ring radius (at origin) and expands to the
            // module's defined combat orbit (when projected).

            orbitVec.set(0f, state.currentRadius).rotateDeg(drone.orbitPhase)

            // Apply procedural squish (proportional to projection distance)
            orbitVec.scl(1.0f, state.heightSquish)

            // Orient the orbit towards the projection target
            orbitVec.rotateDeg(state.lastStableAwayAngle - 90f)

            // Result: Drone is world-locked to the Projected Hub + its specific orbit offset.
            seeking.targetPos.set(state.worldTargetPos).add(orbitVec)
        }
    }
}
