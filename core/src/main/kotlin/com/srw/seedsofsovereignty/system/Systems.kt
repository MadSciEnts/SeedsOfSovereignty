package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*

// 1. INPUT SYSTEM: Translates keys to intention
class PlayerInputSystem : IteratingSystem(
    family { all(PlayerComponent, VelocityComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        val moveForce = Vector2(0f, 0f)

        if (Gdx.input.isKeyPressed(Keys.W)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D)) moveForce.x += 1f

        if (moveForce.len() > 0) {
            moveForce.nor().scl(vel.acceleration * deltaTime)
            vel.v.add(moveForce)
        }
    }
}

// 2. MOVE SYSTEM: Applies physics
class MoveSystem : IteratingSystem(
    family { all(PositionComponent, VelocityComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        // Apply friction
        vel.v.scl(1f - (vel.friction * deltaTime))
    }
}

// 3. CAMERA SYSTEM: Follows the player
class CameraFollowSystem(
    private val camera: OrthographicCamera = inject()
) : IteratingSystem(
    family { all(PlayerComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        camera.position.x += (pos.x - camera.position.x) * 0.1f
        camera.position.y += (pos.y - camera.position.y) * 0.1f
    }
}

// 4. ATTACHMENT SYSTEM: Syncs modules to parent ship
class ModuleAttachmentSystem : IteratingSystem(
    family { all(AttachmentComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val attach = entity[AttachmentComponent]
        val myPos = entity[PositionComponent]

        // Find parent position
        if (attach.parent != Entity.NONE) {
            if (attach.parent in world) {
                // Correct way to access component of another entity in Fleks 2.x
                // Since IteratingSystem is an EntityComponentContext, we use Entity.get(ComponentType)
                val parentPos = attach.parent.get(PositionComponent).pos
                myPos.pos.set(parentPos).add(attach.offset)
            }
        }
    }
}

// 5. POWER THROTTLING SYSTEM: Applies penalty if core is damaged
class PowerThrottlingSystem : IteratingSystem(
    family { all(PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val core = entity[PowerCoreComponent]
        val availablePower = core.baseOutput * core.integrity
        val requestedPower = core.weaponPower + core.shieldPower + core.enginePower + core.utilityPower

        // If we are overdrawing, everything slows down proportionally
        core.throttle = if (requestedPower > availablePower) availablePower / requestedPower else 1.0f
    }
}

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
