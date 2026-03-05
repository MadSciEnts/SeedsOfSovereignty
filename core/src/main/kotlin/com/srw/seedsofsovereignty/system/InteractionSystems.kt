package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
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
