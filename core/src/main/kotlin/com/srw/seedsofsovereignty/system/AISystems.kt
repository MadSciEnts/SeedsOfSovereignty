package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import kotlin.math.sqrt

/**
 * Autonomous Intelligence Systems (v2.9.1).
 * ROLE: Specialized behavior logic driven by modular ship stats.
 *
 * FIXES:
 * 1. [MODULAR] - AI movement (speed/turn) is now driven by TacticalStatsComponent.
 * 2. [PASSIVE] - Equipped modules apply passive modifiers to maneuverability.
 */
class EnemyAISystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(EnemyComponent, TacticalStatsComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, VelocityComponent) } }
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }
    private val steerForce = Vector2()
    private val predictedPlayerPos = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]
        val enemy = entity[EnemyComponent]
        val stats = entity[TacticalStatsComponent]
        val orientation = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else return

        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val playerVel = player[VelocityComponent].v
        val docking = if (player.has(DockingComponent)) player[DockingComponent].isDocked else false

        // 1. DYNAMIC STAT REFRESH (1-2s Interval)
        enemy.aiTimer -= deltaTime
        if (enemy.aiTimer <= 0f) {
            enemy.aiTimer = MathUtils.random(1.0f, 2.0f)
            stats.orbitDrift = MathUtils.random(-0.5f, 0.5f)

            // TIERED DEPTH: Base distance based on Evolution Tier
            val tier = if (entity.has(EvolutionComponent)) entity[EvolutionComponent].currentTier else 1
            val minP = 0.10f + (tier - 1) * 0.022f
            val maxP = 0.15f + (tier - 1) * 0.022f
            stats.attackDistance = MathUtils.random(minP, maxP) * 128f
        }

        // 2. MODULAR PASSIVE MULTIPLIERS
        val module = if (entity.has(WeaponModuleComponent)) entity[WeaponModuleComponent] else null
        val finalAttackDist = stats.attackDistance * (module?.passiveDistanceMod ?: 1.0f)
        val finalSpeed = stats.maxSpeed * (module?.passiveSpeedMod ?: 1.0f)
        val finalAccel = vel.acceleration * (module?.passiveSpeedMod ?: 1.0f) // Accel scales with speed mod

        var isSafeHavenActive = docking
        if (!isSafeHavenActive) {
            stationFamily.forEach { station ->
                if (pos.dst2(station[PositionComponent].pos) < 2500f) isSafeHavenActive = true
            }
        }

        if (isSafeHavenActive) {
            enemy.state = EnemyComponent.State.RETREATING
            weapon.isFiring = false
            steerForce.set(pos).sub(playerPos).nor().scl(finalAccel * 2f * deltaTime)
            vel.v.add(steerForce); vel.v.scl(0.95f)
            if (pos.dst2(playerPos) > 22500f) world -= entity
        } else {
            enemy.state = EnemyComponent.State.HUNTING
            val distSq = pos.dst2(playerPos)
            val dist = sqrt(distSq)
            val isVisibleToPlayer = camera.frustum.pointInFrustum(pos.x, pos.y, 0f)

            // Bullet Speed Compensation
            val bulletSpeed = 60f
            val timeToReach = dist / bulletSpeed
            predictedPlayerPos.set(playerPos).add(playerVel.x * timeToReach, playerVel.y * timeToReach)

            val aimAngle = Vector2(predictedPlayerPos).sub(pos).angleRad()
            orientation.angleRad = aimAngle
            weapon.aimAngleRad = aimAngle

            weapon.isFiring = distSq < 4000f && isVisibleToPlayer

            // 3. REACTIVE STEERING
            val toPlayer = Vector2(playerPos).sub(pos).nor()
            val tangent = Vector2(-toPlayer.y, toPlayer.x)

            if (dist < finalAttackDist) {
                // BACK-PEDAL: Repulsive force if too close
                steerForce.set(toPlayer).scl(-finalAccel * 0.8f * deltaTime)
                vel.v.add(steerForce)
            } else if (dist > finalAttackDist + 10f) {
                // APPROACH: Attractive force if too far
                steerForce.set(toPlayer).scl(finalAccel * 0.6f * deltaTime)
                vel.v.add(steerForce)
            }

            // LAZY ORBIT: Dynamic horizontal drift
            steerForce.set(tangent).scl(finalAccel * stats.orbitDrift * deltaTime)
            vel.v.add(steerForce)

            // PROXIMITY TENSION: Maintain general engagement
            vel.v.add(toPlayer.scl(finalAccel * 0.05f * deltaTime))

            // TOP-SPEED CLAMP: Using finalSpeed stat
            if (vel.v.len2() > finalSpeed * finalSpeed) {
                vel.v.setLength(finalSpeed)
            }
        }
    }
}
