package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*

/**
 * ENGINE PROPULSION SYSTEM: Translates Engine Module inputs into parent ship motion.
 */
class EnginePropulsionSystem : IteratingSystem(
    family { all(ModuleComponent, ModuleChannelsComponent, AttachmentComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val channels = entity[ModuleChannelsComponent]
        if (channels.type != ModuleType.ENGINE_IMPULSE_T1) return

        val moveInput = channels.inputs["MOVE"] ?: return
        if (moveInput.vector.len2() < 0.001f) return

        val attach = entity[AttachmentComponent]
        if (attach.parent != Entity.NONE && attach.parent in world) {
            world.run {
                if (attach.parent.has(VelocityComponent)) {
                    val vel = attach.parent[VelocityComponent]
                    tempVec.set(moveInput.vector).scl(vel.acceleration * deltaTime)
                    vel.v.add(tempVec)
                }
            }
        }
    }
}

/**
 * PLAYER INPUT SYSTEM: Updates the player's Engine Module "MOVE" channel.
 */
class PlayerInputSystem(
    private val joystick: Touchpad = inject("joystick")
) : IteratingSystem(
    family { all(PlayerComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        world.family { all(AttachmentComponent, ModuleChannelsComponent) }.forEach { module ->
            if (module[AttachmentComponent].parent == entity && module[ModuleChannelsComponent].type == ModuleType.ENGINE_IMPULSE_T1) {
                val input = module[ModuleChannelsComponent].inputs["MOVE"] ?: return@forEach

                input.vector.set(0f, 0f)
                if (joystick.knobPercentX != 0f || joystick.knobPercentY != 0f) {
                    input.vector.set(joystick.knobPercentX, joystick.knobPercentY)
                }

                if (Gdx.input.isKeyPressed(Keys.W)) input.vector.y += 1f
                if (Gdx.input.isKeyPressed(Keys.S)) input.vector.y -= 1f
                if (Gdx.input.isKeyPressed(Keys.A)) input.vector.x -= 1f
                if (Gdx.input.isKeyPressed(Keys.D)) input.vector.x += 1f

                if (input.vector.len2() > 1f) input.vector.nor()
            }
        }
    }
}

/**
 * MOVE SYSTEM: Handles position, velocity capping, and ACCURATE rotation alignment.
 */
class MoveSystem : IteratingSystem(
    family { all(PositionComponent, VelocityComponent) }
) {
    private val maxSpeed = 15f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        if (vel.v.len2() > maxSpeed * maxSpeed) {
            vel.v.setLength(maxSpeed)
        }

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        // ROTATION ALIGNMENT
        if (vel.v.len2() > 0.1f) {
            val targetAngle = vel.v.angleDeg() - 90f

            // Optimization: Snap rotation for projectiles (zero friction)
            // Interpolate rotation for ships (friction > 0)
            val alpha = if (vel.friction == 0f) 1.0f else 0.40f

            if (entity.has(ShapeComponent)) {
                entity[ShapeComponent].rotation = lerpAngle(entity[ShapeComponent].rotation, targetAngle, alpha)
            }
            if (entity.has(TextureRegionComponent)) {
                entity[TextureRegionComponent].rotation = lerpAngle(entity[TextureRegionComponent].rotation, targetAngle, alpha)
            }
        }

        vel.v.scl(1f - (vel.friction * deltaTime))
    }

    private fun lerpAngle(from: Float, to: Float, alpha: Float): Float {
        if (alpha >= 1.0f) return to
        var diff = (to - from) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        return from + diff * alpha
    }
}
