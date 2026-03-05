package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class PlayerInputSystem : IteratingSystem(family { all(PlayerComponent, VelocityComponent) }) {
    private val moveForce = Vector2()
    val joystickVector = Vector2()
    val aimVector = Vector2()
    var isFiringRequested = false

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        val orientation = entity[OrientationComponent]
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val docking = if (entity.has(DockingComponent)) entity[DockingComponent] else null

        if (docking?.isDocked == true) return

        moveForce.set(joystickVector)

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveForce.x += 1f

        if (!moveForce.isZero) {
            moveForce.nor().scl(vel.acceleration * deltaTime)
            vel.v.add(moveForce)
            if ((evo?.currentTier ?: 1) < 4) orientation.angleRad = moveForce.angleRad()
        }

        if ((evo?.currentTier ?: 1) >= 4) {
            if (!aimVector.isZero) orientation.angleRad = aimVector.angleRad()
            else if (!moveForce.isZero) orientation.angleRad = moveForce.angleRad()
        }

        if (entity.has(WeaponComponent)) {
            entity[WeaponComponent].isFiring = isFiringRequested || Gdx.input.isKeyPressed(Keys.SPACE)
        }
    }
}

class MoveSystem : IteratingSystem(family { all(PositionComponent, VelocityComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        val frictionFactor = if (entity.has(EssenceComponent)) 0.5f else vel.friction
        if (!entity.has(ProjectileComponent)) {
            vel.v.scl(1f - (frictionFactor * deltaTime))
        }
    }
}

class CameraSystem(private val camera: OrthographicCamera) : IteratingSystem(family { all(PlayerComponent, PositionComponent, VelocityComponent) }) {
    private val targetPos = Vector2()
    private val lerpSpeed = 2.5f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        targetPos.set(pos.x + (vel.x * 0.5f), pos.y + (vel.y * 0.5f))
        camera.position.x += (targetPos.x - camera.position.x) * lerpSpeed * deltaTime
        camera.position.y += (targetPos.y - camera.position.y) * lerpSpeed * deltaTime
        val speedPercent = (vel.len() / 500f).coerceIn(0f, 1f)
        camera.zoom += (1.0f + (speedPercent * 0.4f) - camera.zoom) * deltaTime
        camera.update()
    }
}

class OriginSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent) }) {
    private val allPosFamily by lazy { world.family { all(PositionComponent) } }
    private val threshold = 10000f

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        if (Math.abs(playerPos.x) > threshold || Math.abs(playerPos.y) > threshold) {
            val shift = Vector2(playerPos)
            allPosFamily.forEach { target -> target[PositionComponent].pos.sub(shift) }
        }
    }
}
