package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*

/**
 * ENGINE PROPULSION SYSTEM: Translates Module inputs into physical motion.
 */
class EnginePropulsionSystem : IteratingSystem(
    family { all(ModuleComponent, ModuleChannelsComponent, AttachmentComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val channels = entity[ModuleChannelsComponent]

        // Standard TARGET channel handles movement
        val moveInput = channels.inputs[StandardChannels.TARGET] ?: return
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
 * PLAYER INPUT SYSTEM: Drives the player's unit via StandardChannels.
 */
class PlayerInputSystem(
    private val joystick: Touchpad = inject("joystick")
) : IteratingSystem(
    family { all(PlayerComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        world.family { all(AttachmentComponent, ModuleChannelsComponent) }.forEach { moduleEntity ->
            if (moduleEntity[AttachmentComponent].parent == entity) {
                val channels = moduleEntity[ModuleChannelsComponent]

                // Movement Joystick ONLY drives Impulse Engines in Epoch 1
                if (channels.type == ModuleType.ENGINE_IMPULSE) {
                    val input = channels.inputs[StandardChannels.TARGET] ?: return@forEach

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
}

/**
 * MOVE SYSTEM: Handles position and rotation alignment.
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

            // OPTIMIZATION: Snap rotation for projectiles (zero friction)
            // Interpolate rotation for ships (friction > 0)
            val alpha = if (vel.friction == 0f) 1.0f else 0.40f

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
