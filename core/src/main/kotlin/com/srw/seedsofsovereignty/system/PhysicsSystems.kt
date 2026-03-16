package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.VelocityComponent

/**
 * PLAYER INPUT SYSTEM: Reads from the on-screen joystick and keyboard to apply movement force.
 */
class PlayerInputSystem(
    private val joystick: Touchpad = inject("joystick")
) : IteratingSystem(
    family { all(PlayerComponent, VelocityComponent) }
) {
    private val moveForce = Vector2()

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        moveForce.set(0f, 0f)

        // 1. Read Joystick Input
        if (joystick.knobPercentX != 0f || joystick.knobPercentY != 0f) {
            moveForce.set(joystick.knobPercentX, joystick.knobPercentY)
        }

        // 2. Keyboard Fallback (Additive)
        if (Gdx.input.isKeyPressed(Keys.W)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D)) moveForce.x += 1f

        // 3. Apply Movement
        if (moveForce.len() > 0) {
            // Clamp diagonal movement
            if (moveForce.len() > 1f) moveForce.nor()

            moveForce.scl(vel.acceleration * deltaTime)
            vel.v.add(moveForce)
        }
    }
}

/**
 * MOVE SYSTEM: Applies velocity to position and applies friction.
 */
class MoveSystem : IteratingSystem(
    family { all(PositionComponent, VelocityComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        // Apply friction/drag
        vel.v.scl(1f - (vel.friction * deltaTime))
    }
}
