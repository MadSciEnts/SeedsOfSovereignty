package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class DroneRegenSystem : IteratingSystem(family { all(DroneLauncherComponent, DroneModuleComponent) }) {
    private val droneFamily by lazy { world.family { all(DroneComponent) } }

    override fun onTickEntity(entity: Entity) {
        val launcher = entity[DroneLauncherComponent]
        val module = entity[DroneModuleComponent]

        var activeCount = 0
        droneFamily.forEach { if (it[DroneComponent].slotIndex == launcher.slotIndex) activeCount++ }
        launcher.activeDrones = activeCount

        val player = world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()
        player?.get(TacticalHubComponent)?.let { hub ->
            val slotIdx = launcher.slotIndex
            val modeValue = hub.scalars["DRONE_MODE_$slotIdx"] ?: 0f
            hub.signals["DRONE_MODE_SIGNAL_$slotIdx"] = StatusSignal(value = modeValue, label = "MODE")
            hub.signals["DRONES_ACTIVE_$slotIdx"] = StatusSignal(value = launcher.activeDrones.toFloat(), label = "ACTIVE")
            hub.signals["DRONES_REMAINING_$slotIdx"] = StatusSignal(value = (module.maxDrones - launcher.activeDrones).toFloat(), label = "RESERVE")

            if (hub.scalars["DRONE_DEPLOY_$slotIdx"] == 1f) {
                if (launcher.activeDrones < module.maxDrones) spawnDrone(entity, launcher, module)
                hub.scalars["DRONE_DEPLOY_$slotIdx"] = 0f
            }
        }

        if (launcher.activeDrones < module.maxDrones) {
            launcher.regenTimer += deltaTime
            if (launcher.regenTimer >= launcher.regenRate) {
                launcher.regenTimer = 0f
                spawnDrone(entity, launcher, module)
            }
        }
    }

    private fun spawnDrone(slotEntity: Entity, launcher: DroneLauncherComponent, module: DroneModuleComponent) {
        val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull() ?: return
        world.entity { d ->
            d += PositionComponent().apply { pos.set(player[PositionComponent].pos) }
            d += OrientationComponent()
            d += DroneComponent(owner = player, slotIndex = launcher.slotIndex, orbitRadius = 70f + (launcher.slotIndex * 30f))
            d += WeaponComponent()
            val dna = module.weaponDNA
            d += WeaponModuleComponent(
                name = dna.name, projectileTier = dna.projectileTier, rarity = dna.rarity,
                projectileSpeed = dna.projectileSpeed, fireRate = dna.fireRate,
                projectileColor = dna.projectileColor, effects = dna.effects.toMutableList()
            )
            d += VelocityComponent().apply { friction = 2.5f }
            d += HealthComponent(max = module.maxHealth, current = module.maxHealth)
            d += VisibleComponent()
        }
    }
}

/**
 * Technical Specification: DroneFormationSystem (v0.7.3)
 * ROLE: Advanced Organic Fleet Movement.
 *
 * DESIGN PRINCIPLES:
 * 1. [THE ELLIPTICAL PHALANX] - Drones transition from circular to elliptical orbits
 *    based on tactical distance (pushMag).
 * 2. [SQUISH LOGIC] - High-distance orbits favor angular coverage over radial depth.
 * 3. [DIRECTIONAL BIAS] - Squish is always perpendicular to the vector from mother ship.
 */
class DroneFormationSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family {
    all(DroneComponent, PositionComponent, VelocityComponent, OrientationComponent)
}) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }
    private val targetPos = Vector2()
    private val steerForce = Vector2()
    private val rotatedOffset = Vector2()
    private val orbitComponent = Vector2()
    private var globalOrbitBase = 0f

    override fun onTick() {
        globalOrbitBase += 1.5f * deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val orient = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null

        val owner = drone.owner
        if (!world.contains(owner)) {
            world -= entity
            return
        }

        var localizationMode = FleetCommandComponent.LocalizationMode.CAMERA
        rotatedOffset.set(0f, 0f)

        if (owner.has(TacticalHubComponent)) {
            val hub = owner[TacticalHubComponent]
            val slotIdx = drone.slotIndex
            hub.vectors["DRONE_MOVE_$slotIdx"]?.let { rotatedOffset.set(it).scl(150f) }
            val modeSignal = hub.signals["DRONE_MODE_SIGNAL_$slotIdx"]?.value ?: 0f
            localizationMode = if (modeSignal == 1f) FleetCommandComponent.LocalizationMode.SHIP else FleetCommandComponent.LocalizationMode.CAMERA
        }

        val shipAngle = if (owner.has(OrientationComponent)) owner[OrientationComponent].angleRad else 0f
        if (localizationMode == FleetCommandComponent.LocalizationMode.SHIP) {
            rotatedOffset.rotateRad(shipAngle - MathUtils.PI / 2f)
        }

        val pushMag = rotatedOffset.len() / 150f
        val ownerPos = owner[PositionComponent].pos
        val droneIndex = entity.id % 8
        val orbitAngle = globalOrbitBase + (droneIndex * MathUtils.PI2 / 8f)

        // [ORGANIC ELLIPSE LOGIC] (v0.7.3)
        // 1. Calculate base circular components
        orbitComponent.set(MathUtils.cos(orbitAngle), MathUtils.sin(orbitAngle))

        // 2. Apply "Squish" scaling based on pushMag
        // As pushMag -> 1.0, the X-axis (radial) is squished, while Y-axis (angular) is maintained.
        // We rotate the orbit relative to the directive vector so the "flat" side faces the ship.
        val radialSquish = (1f - pushMag * 0.7f).coerceIn(0.3f, 1.0f)
        val angularSpread = (1f + pushMag * 0.3f).coerceIn(1.0f, 1.3f)

        orbitComponent.x *= radialSquish
        orbitComponent.y *= angularSpread

        // 3. Align the ellipse to the direction of the command vector
        if (rotatedOffset.len2() > 0.1f) {
            orbitComponent.rotateRad(rotatedOffset.angleRad())
        }

        targetPos.set(ownerPos)
            .add(rotatedOffset)
            .add(orbitComponent.scl(drone.orbitRadius))

        steerForce.set(targetPos).sub(pos).scl(5f)
        vel.add(steerForce.x * deltaTime, steerForce.y * deltaTime)
        pos.add(vel.x * deltaTime, vel.y * deltaTime)

        var targetEntity: Entity? = null
        var minDistSq = 160000f
        enemyFamily.forEach { enemy ->
            val d2 = pos.dst2(enemy[PositionComponent].pos)
            if (d2 < minDistSq) {
                minDistSq = d2
                targetEntity = enemy
            }
        }

        targetEntity?.let { enemy ->
            val aimAngle = Vector2(enemy[PositionComponent].pos).sub(pos).angleRad()
            orient.angleRad = aimAngle
            weapon?.aimAngleRad = aimAngle
            weapon?.isFiring = true
        } ?: run {
            orient.angleRad = orbitAngle
            weapon?.isFiring = false
        }
    }
}
