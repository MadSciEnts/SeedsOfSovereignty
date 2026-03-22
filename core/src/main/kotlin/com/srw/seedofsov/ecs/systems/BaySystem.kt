package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.EntityFactory
import com.srw.seedofsov.ecs.components.*
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages the deployment and deterministic orbital formation of Drones.
 * Truth on Disk: Drones follow fixed angular slots relative to the bay's logical swarmRotation.
 * This ensures they maintain their orbit logic even when the visual ring is flat and static.
 */
class BaySystem(private val factory: EntityFactory) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class).get(),
    35 // Priority after TacticalSystem (45)
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

    private fun updateBay(owner: Entity, shipTransform: TransformComponent, bay: BayModuleComponent, deltaTime: Float) {
        // 1. Cleanup & Smart Launch
        bay.activeUnits.removeAll { it.isScheduledForRemoval }

        if (bay.activeUnits.size < bay.capacity) {
            bay.launchTimer -= deltaTime
            if (bay.launchTimer <= 0f && bay.isLaunchRequested) {
                val droneEntity = factory.createDrone(shipTransform.position.x, shipTransform.position.y, owner)
                bay.activeUnits.add(droneEntity)
                bay.launchTimer = bay.launchRate
            }
        }

        // 2. Swarm Logic (Physical Spin)
        // This is the logical rotation state that never stops.
        val signedControl = (bay.orbitSpeed - 0.5f) * 2f
        val currentBaseSpeed = 120f * signedControl
        bay.swarmRotation += currentBaseSpeed * deltaTime

        // 3. Synchronized Formation Physics
        val state = bay.tacticalState
        val droneCount = bay.activeUnits.size
        if (droneCount == 0) return

        val idealGap = 360f / droneCount

        bay.activeUnits.forEachIndexed { index, unit ->
            val seeking = unit[SeekingComponent.mapper] ?: return@forEachIndexed
            val drone = unit[DroneComponent.mapper] ?: return@forEachIndexed

            seeking.ownerEntity = owner

            // TRUTH ON DISK: Drones use the module's logical swarmRotation.
            // This ensures they keep orbiting even when the visual HUD ring (state.currentRotation)
            // is hard-locked to 0f for flat-state stability.
            val targetAngle = bay.swarmRotation + (index * idealGap)

            // Matches TacticalRenderer geometry logic:
            // 1. Position on the circle
            orbitVec.set(0f, state.currentRadius).rotateDeg(targetAngle)

            // 2. Apply squish (perspective)
            orbitVec.scl(1.0f, state.heightSquish)

            // 3. Orient to the projection axis
            // Note: state.lastStableAwayAngle is world-locked (90f) when state is flat.
            orbitVec.rotateDeg(state.lastStableAwayAngle - 90f)

            // Result: Drone targets the logic-driven position in world space.
            seeking.targetPos.set(state.worldTargetPos).add(orbitVec)

            drone.orbitPhase = targetAngle
        }
    }
}
