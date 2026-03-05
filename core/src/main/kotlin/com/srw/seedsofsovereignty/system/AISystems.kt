package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import kotlin.math.sqrt

class EnemyAISystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(EnemyComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, VelocityComponent) } }
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }
    private val steerForce = Vector2()
    private val predictedPlayerPos = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]
        val enemy = entity[EnemyComponent]
        val orientation = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else return // Enemies must have weapons

        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val playerVel = player[VelocityComponent].v
        val docking = if (player.has(DockingComponent)) player[DockingComponent] else null

        var isSafeHavenActive = docking?.isDocked == true
        if (!isSafeHavenActive) {
            stationFamily.forEach { station ->
                if (pos.dst2(station[PositionComponent].pos) < 250000f) isSafeHavenActive = true
            }
        }

        if (isSafeHavenActive) {
            enemy.state = EnemyComponent.State.RETREATING
            weapon.isFiring = false

            steerForce.set(pos).sub(playerPos).nor().scl(vel.acceleration * 2f * deltaTime)
            vel.v.add(steerForce)
            vel.v.scl(0.95f)

            if (pos.dst2(playerPos) > 2250000f) world -= entity
        } else {
            enemy.state = EnemyComponent.State.HUNTING
            val distSq = pos.dst2(playerPos)
            val isVisibleToPlayer = camera.frustum.pointInFrustum(pos.x, pos.y, 0f)

            // Predictive Aiming Logic
            val bulletSpeed = 600f
            val timeToReach = sqrt(distSq) / bulletSpeed
            predictedPlayerPos.set(playerPos).add(playerVel.x * timeToReach, playerVel.y * timeToReach)

            // SET BOTH HULL AND AIM ANGLE
            val aimAngle = Vector2(predictedPlayerPos).sub(pos).angleRad()
            orientation.angleRad = aimAngle
            weapon.aimAngleRad = aimAngle

            weapon.isFiring = distSq < 400000f && isVisibleToPlayer

            if (distSq < 90000f) { // Orbiting
                val toPlayer = Vector2(playerPos).sub(pos).nor()
                val tangent = Vector2(-toPlayer.y, toPlayer.x).scl(vel.acceleration * deltaTime)
                vel.v.add(tangent)
                vel.v.add(toPlayer.scl(vel.acceleration * 0.2f * deltaTime))
            } else { // Hunting
                steerForce.set(playerPos).sub(pos).nor().scl(vel.acceleration * deltaTime)
                vel.v.add(steerForce)
            }
        }
    }
}

class DroneSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(DroneComponent, PositionComponent, OrientationComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }
    private val droneFamily by lazy { world.family { all(DroneComponent) } }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val orientation = entity[OrientationComponent]
        val weapon = if(entity.has(WeaponComponent)) entity[WeaponComponent] else null

        val owner = drone.owner
        if (owner !in world) {
            world -= entity
            return
        }

        // Dynamic Spacing Logic
        var total = 0; var myIndex = -1
        droneFamily.forEach { if (it[DroneComponent].owner == owner) { if (it == entity) myIndex = total; total++ } }

        val targetOrbitAngle = drone.orbitAngle + (myIndex * MathUtils.PI2 / total)
        val ownerPos = owner[PositionComponent].pos
        pos.set(ownerPos.x + MathUtils.cos(targetOrbitAngle) * drone.orbitRadius, ownerPos.y + MathUtils.sin(targetOrbitAngle) * drone.orbitRadius)
        drone.orbitAngle += drone.orbitSpeed * deltaTime

        var nearestEnemy: Entity? = null
        var minDistSq = Float.MAX_VALUE

        enemyFamily.forEach { enemy ->
            if (enemy[EnemyComponent].state == EnemyComponent.State.RETREATING) return@forEach
            val ePos = enemy[PositionComponent].pos
            val d2 = pos.dst2(ePos)
            if (d2 < minDistSq && camera.frustum.pointInFrustum(ePos.x, ePos.y, 0f)) {
                minDistSq = d2
                nearestEnemy = enemy
            }
        }

        nearestEnemy?.let { enemy ->
            val ePos = enemy[PositionComponent].pos
            val aimAngle = Vector2(ePos).sub(pos).angleRad()
            orientation.angleRad = aimAngle
            weapon?.aimAngleRad = aimAngle // SET AIM ANGLE
            weapon?.isFiring = true
        } ?: run {
            weapon?.isFiring = false
        }
    }
}
