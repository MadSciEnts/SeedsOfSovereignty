package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.EntityFactory
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.CoordinateSpace
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Manages the deployment and advanced orbital flocking of Drones.
 * Truth on Disk: Implements "Nearest Point" logic with enhanced organic spacing.
 * Orbit speed reduced to 3.5f/s for a very slow graceful feel. Extreme depth squish (0.05 min).
 */
class BaySystem(private val factory: EntityFactory) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class).get()
) {
    private val tempVec = Vector2()
    private val orbitVec = Vector2()
    private val currentIntentOffset = Vector2()
    private val droneRelPos = Vector2()

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
        // 1. Cleanup & Launch
        bay.activeUnits.removeAll { it.isScheduledForRemoval }
        if (bay.activeUnits.size < bay.capacity) {
            bay.launchTimer -= deltaTime
            if (bay.launchTimer <= 0f && bay.isLaunchRequested) {
                val drone = factory.createDrone(transform.position.x, transform.position.y, owner)
                bay.activeUnits.add(drone)
                bay.launchTimer = bay.launchRate
            }
        }

        val droneCount = bay.activeUnits.size
        if (droneCount == 0) return

        val shipRadius = (transform.size.x + transform.size.y) / 4f
        val tacticalRange = shipRadius * 5f

        // 2. Resolve Target Hub Position (Local -> World conversion based on Mode)
        bay.lerpedOffset.lerp(bay.commandPosition, 3f * deltaTime)
        tempVec.set(bay.lerpedOffset).scl(tacticalRange)

        if (bay.commandSpace == CoordinateSpace.OBJECT) {
            currentIntentOffset.set(tempVec).rotateDeg(transform.rotation)
        } else {
            currentIntentOffset.set(tempVec)
        }

        bay.worldTargetPos.set(transform.position.x + currentIntentOffset.x, transform.position.y + currentIntentOffset.y)

        // 3. Resolve STABLE Orbit Orientation (Anti-Flip Logic)
        val intentMag = bay.lerpedOffset.len()
        if (intentMag > 0.1f) {
            val targetAwayAngle = currentIntentOffset.angleDeg()
            bay.lastStableAwayAngle = MathUtils.lerpAngleDeg(bay.lastStableAwayAngle, targetAwayAngle, 4f * deltaTime)
        }

        // 4. Resolve depth squish (Extremely substantial squish: 1.0 down to 0.05)
        val pushMag = MathUtils.clamp(intentMag, 0f, 1f)
        val radiusScale = MathUtils.lerp(1.6f, 1.0f, pushMag)
        val heightSquish = MathUtils.lerp(1.0f, 0.05f, pushMag)

        // 5. Update Drones (Nearest Point Logic + Proportional Spacing)
        val idealGap = 360f / droneCount.coerceAtLeast(1)
        val repulsionThreshold = idealGap * 1.5f // Increased threshold for smoother distribution

        bay.activeUnits.forEach { unit ->
            val seeking = unit[SeekingComponent.mapper] ?: return@forEach
            val drone = unit[DroneComponent.mapper] ?: return@forEach
            val uTransform = unit[TransformComponent.mapper] ?: return@forEach

            seeking.ownerEntity = owner

            // A. Find Current Dynamic Phase
            droneRelPos.set(uTransform.position.x, uTransform.position.y).sub(bay.worldTargetPos)
            droneRelPos.rotateDeg(-(bay.lastStableAwayAngle - 90f))
            droneRelPos.y /= heightSquish
            val nearestPhase = droneRelPos.angleDeg()

            // B. Advance the Phase (Reduced speed: 3.5f/s)
            drone.orbitPhase = nearestPhase + (3.5f * deltaTime)

            // C. Proportional Spacing Logic (Stronger repulsion for better spacing)
            bay.activeUnits.forEach { otherUnit ->
                if (otherUnit === unit) return@forEach
                val otherDrone = otherUnit[DroneComponent.mapper] ?: return@forEach

                var diff = drone.orbitPhase - otherDrone.orbitPhase
                while (diff <= -180f) diff += 360f
                while (diff > 180f) diff -= 360f

                if (diff.absoluteValue < repulsionThreshold) {
                    val overlap = 1f - (diff.absoluteValue / repulsionThreshold)
                    val pushStrength = overlap * 120f // Doubled strength to force distribution

                    val pushDir = if (diff.absoluteValue < 0.1f) {
                        if (unit.hashCode() > otherUnit.hashCode()) 1f else -1f
                    } else {
                        diff.sign
                    }
                    drone.orbitPhase += pushDir * pushStrength * deltaTime
                }
            }

            // D. Calculate Target Position on the Plane
            val radiusVariance = (unit.hashCode().absoluteValue % 3) * 0.4f
            val baseRadius = shipRadius * (2.5f + radiusVariance) * radiusScale

            orbitVec.set(0f, baseRadius).rotateDeg(drone.orbitPhase)
            orbitVec.scl(1.0f, heightSquish)
            orbitVec.rotateDeg(bay.lastStableAwayAngle - 90f)

            seeking.targetPos.set(bay.worldTargetPos).add(orbitVec)
        }
    }
}
