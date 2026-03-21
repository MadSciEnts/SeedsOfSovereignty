package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.pow

/**
 * Steering and pursuit logic for Drones and Seeking Projectiles.
 * Truth on Disk: Implements Ease-Out acceleration and cubic blending for fluid "gliding" movement.
 */
class SeekingSystem : IteratingSystem(
    allOf(TransformComponent::class, PhysicsComponent::class, SeekingComponent::class).get()
) {
    private val currentPos = Vector2()
    private val steeringForce = Vector2()
    private val parentVelocity = Vector2()
    private val relativeVelocity = Vector2()
    private val desiredRelativeVel = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return
        val seeking = entity[SeekingComponent.mapper] ?: return
        val drone = entity[DroneComponent.mapper]

        // 1. Establish the Stable Frame of Reference (Ship Velocity)
        val targetEntity = seeking.ownerEntity
        if (targetEntity != null) {
            val targetPhysics = targetEntity[PhysicsComponent.mapper]
            if (targetPhysics != null) {
                parentVelocity.set(targetPhysics.velocity).scl(seeking.velocityInheritance)
            } else {
                parentVelocity.set(0f, 0f)
            }
        } else {
            parentVelocity.set(0f, 0f)
        }

        // 2. Calculate Distance and Relative State
        currentPos.set(transform.position.x, transform.position.y)
        steeringForce.set(seeking.targetPos).sub(currentPos)
        val distance = steeringForce.len()

        // current relative velocity vs the parent frame
        relativeVelocity.set(physics.velocity).sub(parentVelocity)

        // 3. Fluid "Ease-In-Out" Arrival Logic
        val maxSpeed = 30f
        val arrivalRadius = seeking.arrivalRadius.coerceAtLeast(2.0f)

        // Normalized distance within arrival zone (0..1)
        val nDist = MathUtils.clamp(distance / arrivalRadius, 0f, 1f)

        // Cubic ease-out for speed: starts fast, slows down very gracefully
        // Formula: speed = max * (3n^2 - 2n^3) [Cubic Hermite]
        val smoothStep = 3f * nDist.pow(2) - 2f * nDist.pow(3)
        val targetSpeed = maxSpeed * smoothStep

        // 4. Resolve Desired Relative Velocity
        if (distance > 0.01f) {
            desiredRelativeVel.set(steeringForce).nor().scl(targetSpeed)
        } else {
            desiredRelativeVel.set(0f, 0f)
        }

        // 5. Blended Acceleration (The "Ease-Out")
        // We want a very soft acceleration when far, and precise locking when near.
        // nDist is 1.0 when far, 0.0 when at target.
        // acceleration factor ramps up as we get closer to ensure we 'stick' the landing.
        val accelFactor = MathUtils.lerp(10f, 3f, nDist)

        // Blend the relative velocity toward the desired relative velocity
        relativeVelocity.lerp(desiredRelativeVel, accelFactor * deltaTime)

        // Re-combine with parent frame for final world velocity
        physics.velocity.set(parentVelocity).add(relativeVelocity)

        // 6. Rotation Logic (Fluid Alignment)
        if (drone != null) {
            val combatTarget = drone.combatTarget
            if (combatTarget != null) {
                val eTransform = combatTarget[TransformComponent.mapper]
                if (eTransform != null) {
                    val aimVec = Vector2(eTransform.position.x - transform.position.x,
                                         eTransform.position.y - transform.position.y).nor()
                    val targetAngle = aimVec.angleDeg() - 90f
                    transform.rotation = MathUtils.lerpAngleDeg(transform.rotation, targetAngle, 8f * deltaTime)
                }
            } else {
                // Blend between facing travel and facing outward
                val lookAngle = if (distance > 1.0f) {
                    steeringForce.angleDeg() - 90f
                } else {
                    steeringForce.angleDeg() + 90f
                }
                transform.rotation = MathUtils.lerpAngleDeg(transform.rotation, lookAngle, 4f * deltaTime)
            }
        }
    }
}
