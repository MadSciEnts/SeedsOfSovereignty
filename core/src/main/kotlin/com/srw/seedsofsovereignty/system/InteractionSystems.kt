package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class SensorSystem : IteratingSystem(family { all(SensorComponent, PositionComponent, PlayerComponent) }) {
    private val targets by lazy { world.family { all(PositionComponent).none(PlayerComponent, ProjectileComponent) } }

    override fun onTickEntity(entity: Entity) {
        val sensor = entity[SensorComponent]
        val sensorPos = entity[PositionComponent].pos

        targets.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val distSq = sensorPos.dst2(targetPos)

            if (distSq <= sensor.radius * sensor.radius) {
                if (!target.has(VisibleComponent)) {
                    target.configure { it += VisibleComponent() }
                }
            } else {
                if (target.has(VisibleComponent)) {
                    target.configure { it -= VisibleComponent }
                }
            }
        }
    }
}

class StationSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, DockingComponent) }) {
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val docking = entity[DockingComponent]

        if (docking.undockCooldown > 0f) {
            docking.undockCooldown -= deltaTime
            return
        }

        var nearest: Entity? = null
        var minDistSq = Float.MAX_VALUE

        stationFamily.forEach { station ->
            val sPos = station[PositionComponent].pos
            val d2 = playerPos.dst2(sPos)
            if (d2 < minDistSq) {
                minDistSq = d2
                nearest = station
            }
        }

        docking.nearestStation = nearest
        docking.proximity = if (nearest != null) Math.sqrt(minDistSq.toDouble()).toFloat() else Float.MAX_VALUE

        if (docking.proximity < 100f) {
            docking.isDocked = true
            entity[VelocityComponent].v.set(0f, 0f)
        } else {
            docking.isDocked = false
        }
    }
}

/**
 * Unified system for Vacuum collection and Tractor beams.
 * Implements Rule #10: The Salvage Pipeline.
 */
class SalvageSystem : IteratingSystem(family { all(SalvageComponent, PositionComponent, VelocityComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, EvolutionComponent) } }
    private val pullRadiusSq = 62500f
    private val collectRadiusSq = 1600f // 40u - slightly larger for better collection feel

    override fun onTickEntity(entity: Entity) {
        val salvage = entity[SalvageComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v

        if (salvage.isCollected) return

        // 1. Task Fix: Process Grace Period (Ensures item is rendered before collection)
        if (salvage.collectTimer > 0f) {
            salvage.collectTimer -= deltaTime
            return
        }

        // 2. Locate Player for Vacuum Pull
        playerFamily.firstOrNull()?.let { player ->
            val playerPos = player[PositionComponent].pos
            val distSq = pos.dst2(playerPos)

            if (distSq < pullRadiusSq) {
                val pull = Vector2(playerPos).sub(pos).nor().scl(1200f * deltaTime)
                vel.add(pull)

                if (distSq < collectRadiusSq) {
                    salvage.isCollected = true
                    resolveReward(player, salvage)
                    world -= entity
                }
            }
        }
    }

    private fun resolveReward(player: Entity, salvage: SalvageComponent) {
        with(world) {
            when (salvage.type) {
                SalvageType.ESSENCE -> {
                    val evo = player[EvolutionComponent]
                    evo.essenceCollected += salvage.amount
                }
                SalvageType.SCRAP -> {
                    // Resource management will be implemented in v0.2.0
                }
                else -> {}
            }
        }
    }
}
