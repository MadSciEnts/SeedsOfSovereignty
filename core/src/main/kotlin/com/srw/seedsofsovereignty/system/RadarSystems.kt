package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

// 6. RADAR SENSOR SYSTEM: Detects nearby entities
class RadarSensorSystem : IteratingSystem(
    family { all(RadarComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val radar = entity[RadarComponent]
        val myPos = entity[PositionComponent].pos

        // Update sweep angle based on utility power
        val core = entity.getOrNull(PowerCoreComponent)
        val sweepSpeed = 180f * (core?.utilityPower ?: 0.25f) / 0.25f
        radar.sweepAngle = (radar.sweepAngle + sweepSpeed * deltaTime) % 360f

        // Clear old blips
        radar.blips.clear()

        // Find visible entities
        world.forEach { other ->
            if (other != entity && other has VisibleOnRadarComponent && other has PositionComponent) {
                val otherPos = other.get(PositionComponent).pos
                val dist = myPos.dst(otherPos)

                if (dist <= radar.range) {
                    val visible = other.get(VisibleOnRadarComponent)
                    radar.blips.add(RadarBlip(
                        entityId = other.id,
                        relativePos = Vector2(otherPos).sub(myPos),
                        color = visible.color
                    ))
                }
            }
        }
    }
}
