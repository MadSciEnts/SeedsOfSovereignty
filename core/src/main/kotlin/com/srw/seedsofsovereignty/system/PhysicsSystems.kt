package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.VelocityComponent

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
