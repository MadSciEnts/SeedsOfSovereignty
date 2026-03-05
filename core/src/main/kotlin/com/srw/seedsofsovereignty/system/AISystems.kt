package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import kotlin.math.sqrt

class EnemyAISystem : IteratingSystem(family { all(EnemyComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, VelocityComponent) } }
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }
    private val steerForce = Vector2()
    private val predictedPlayerPos = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]
        val enemy = entity[EnemyComponent]
        val orientation = entity[OrientationComponent]

        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val playerVel = player[VelocityComponent].v
        val docking = if (player.has(DockingComponent)) player[DockingComponent] else null

        var nearStation = docking?.isDocked == true
        if (!nearStation) {
            stationFamily.forEach { station ->
                if (pos.dst2(station[PositionComponent].pos) < 250000f) nearStation = true
            }
        }

        if (nearStation) {
            enemy.state = EnemyComponent.State.RETREATING
            steerForce.set(pos).sub(playerPos).nor().scl(vel.acceleration * 2f * deltaTime)
            vel.v.add(steerForce)
            vel.v.scl(0.95f)
            if (pos.dst2(playerPos) > 2250000f) world -= entity
        } else {
            enemy.state = EnemyComponent.State.HUNTING
            val distSq = pos.dst2(playerPos)

            // CALIBRATED PREDICTIVE AIMING
            val bulletSpeed = 600f
            val timeToReach = sqrt(distSq) / bulletSpeed

            predictedPlayerPos.set(playerPos).add(playerVel.x * timeToReach, playerVel.y * timeToReach)
            orientation.angleRad = Vector2(predictedPlayerPos).sub(pos).angleRad()

            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = distSq < 400000f
            }

            if (distSq < 90000f) { // Orbiting
                val toPlayer = Vector2(playerPos).sub(pos).nor()
                val tangent = Vector2(-toPlayer.y, toPlayer.x).scl(vel.acceleration * deltaTime)
                vel.v.add(tangent)
                vel.v.add(toPlayer.scl(vel.acceleration * 0.2f * deltaTime))
            } else {
                steerForce.set(playerPos).sub(pos).nor().scl(vel.acceleration * deltaTime)
                vel.v.add(steerForce)
            }
        }
    }
}

class DroneSystem : IteratingSystem(family { all(DroneComponent, PositionComponent, OrientationComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val orientation = entity[OrientationComponent]

        drone.orbitAngle += drone.orbitSpeed * deltaTime
        val ownerPos = drone.owner[PositionComponent].pos
        pos.set(
            ownerPos.x + MathUtils.cos(drone.orbitAngle) * drone.orbitRadius,
            ownerPos.y + MathUtils.sin(drone.orbitAngle) * drone.orbitRadius
        )

        var nearestEnemy: Entity? = null
        var minDistSq = Float.MAX_VALUE

        enemyFamily.forEach { enemy ->
            val enemyComp = enemy[EnemyComponent]
            if (enemyComp.state == EnemyComponent.State.RETREATING) return@forEach

            val ePos = enemy[PositionComponent].pos
            val d2 = pos.dst2(ePos)
            if (d2 < minDistSq) {
                minDistSq = d2
                nearestEnemy = enemy
            }
        }

        nearestEnemy?.let { enemy ->
            val ePos = enemy[PositionComponent].pos
            orientation.angleRad = Vector2(ePos).sub(pos).angleRad()

            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = true
            }
        } ?: run {
            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = false
            }
        }
    }
}
