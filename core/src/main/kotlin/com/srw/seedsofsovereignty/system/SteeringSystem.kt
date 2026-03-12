package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Autonomous Steering System (v2.7.5).
 * ROLE: Processes homing logic for torpedoes and autonomous entities.
 */
class SteeringSystem : IteratingSystem(family { all(SteeringComponent, PositionComponent, VelocityComponent) }) {
    private val targetFamily by lazy { world.family { all(PositionComponent).any(EnemyComponent, PlayerComponent) } }
    private val steeringForce = Vector2()
    private val desiredVelocity = Vector2()

    override fun onTickEntity(entity: Entity) {
        val steering = entity[SteeringComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val projectile = if (entity.has(ProjectileComponent)) entity[ProjectileComponent] else null

        // Find Target if missing or invalid
        var target = steering.targetEntity

        if (target == null || target !in world || !target.has(PositionComponent)) {
            steering.targetEntity = findNearestTarget(pos, projectile?.ownerIsEnemy ?: false)
            target = steering.targetEntity
        }

        if (target == null) return

        val targetPos = target[PositionComponent].pos

        // Basic Seek Behavior
        desiredVelocity.set(targetPos).sub(pos).nor().scl(steering.maxSpeed)
        steeringForce.set(desiredVelocity).sub(vel).limit(steering.maxForce)

        vel.add(steeringForce.scl(deltaTime))

        // Update Orientation to match velocity
        if (entity.has(OrientationComponent) && !vel.isZero) {
            entity[OrientationComponent].angleRad = vel.angleRad()
        }
    }

    private fun findNearestTarget(pos: Vector2, ownerIsEnemy: Boolean): Entity? {
        var nearest: Entity? = null
        var nearestDistSq = Float.MAX_VALUE
        targetFamily.forEach { target ->
            val isTargetEnemy = target.has(EnemyComponent)
            if (isTargetEnemy != ownerIsEnemy) {
                val distSq = pos.dst2(target[PositionComponent].pos)
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq
                    nearest = target
                }
            }
        }
        return nearest
    }
}
