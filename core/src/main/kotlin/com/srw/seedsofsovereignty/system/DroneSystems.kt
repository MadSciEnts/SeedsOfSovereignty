package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class DroneRegenSystem : IteratingSystem(family { all(DroneLauncherComponent, PositionComponent) }) {
    private val droneFamily by lazy { world.family { all(DroneComponent) } }

    override fun onTickEntity(entity: Entity) {
        val launcher = entity[DroneLauncherComponent]

        var activeCount = 0
        droneFamily.forEach { if (it[DroneComponent].owner == entity) activeCount++ }
        launcher.activeDrones = activeCount

        if (launcher.activeDrones < launcher.maxDrones) {
            launcher.regenTimer += deltaTime
            if (launcher.regenTimer >= launcher.regenRate) {
                launcher.regenTimer = 0f
                spawnDrone(entity, launcher)
            }
        }
    }

    private fun spawnDrone(owner: Entity, launcher: DroneLauncherComponent) {
        world.entity { d ->
            d += PositionComponent().apply { pos.set(owner[PositionComponent].pos) }
            d += OrientationComponent()
            d += DroneComponent(owner = owner, orbitRadius = 60f)
            d += WeaponComponent()
            // Drones use a specific "Drone Kinetic" module
            d += WeaponModuleComponent(
                name = "Drone Kinetic",
                projectileTier = 3,
                damage = 15f,
                projectileSpeed = 800f,
                fireRate = 0.5f
            )
            d += VelocityComponent()
            d += HealthComponent(max = 30f, current = 30f)
            d += VisibleComponent()
        }
    }
}

class DroneFormationSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(DroneComponent, PositionComponent, OrientationComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }
    private val droneFamily by lazy { world.family { all(DroneComponent) } }
    private val droneListBuffer = mutableListOf<Entity>()
    private var globalOrbitBase = 0f

    override fun onTick() {
        globalOrbitBase += 2f * deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val orientation = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null

        val owner = drone.owner
        if (!world.contains(owner)) {
            world -= entity
            return
        }

        val fleetCommand = if (owner.has(FleetCommandComponent)) owner[FleetCommandComponent].command else FleetCommandComponent.Command.DEFEND

        var targetEntity: Entity? = null
        var minDistSq = Float.MAX_VALUE

        enemyFamily.forEach { enemy ->
            if (enemy[EnemyComponent].state == EnemyComponent.State.RETREATING) return@forEach
            val ePos = enemy[PositionComponent].pos
            val d2 = owner[PositionComponent].pos.dst2(ePos)
            if (d2 < minDistSq && camera.frustum.pointInFrustum(ePos.x, ePos.y, 0f)) {
                minDistSq = d2
                targetEntity = enemy
            }
        }

        droneListBuffer.clear()
        droneFamily.forEach { if (it[DroneComponent].owner == owner) droneListBuffer.add(it) }
        val index = droneListBuffer.indexOf(entity)
        val total = droneListBuffer.size
        val finalAngle = globalOrbitBase + (index * MathUtils.PI2 / total)

        val centerPoint = if (fleetCommand != FleetCommandComponent.Command.DEFEND && targetEntity != null) {
            targetEntity!![PositionComponent].pos
        } else {
            owner[PositionComponent].pos
        }

        pos.set(
            centerPoint.x + MathUtils.cos(finalAngle) * drone.orbitRadius,
            centerPoint.y + MathUtils.sin(finalAngle) * drone.orbitRadius
        )

        targetEntity?.let { enemy ->
            val aimAngle = Vector2(enemy[PositionComponent].pos).sub(pos).angleRad()
            orientation.angleRad = aimAngle
            weapon?.aimAngleRad = aimAngle
            weapon?.isFiring = true
        } ?: run {
            weapon?.isFiring = false
        }
    }
}
