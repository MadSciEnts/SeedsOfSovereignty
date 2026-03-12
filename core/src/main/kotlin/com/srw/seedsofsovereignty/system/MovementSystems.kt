package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer

/**
 * Handles player control and input translation (v2.9.1).
 */
class PlayerInputSystem(
    private val gameViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent, VelocityComponent) }) {
    private val moveForce = Vector2()
    val joystickVector = Vector2()
    val aimVector = Vector2()
    var isFiringRequested = false

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        val orientation = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null
        val docking = if (entity.has(DockingComponent)) entity[DockingComponent].isDocked else false

        if (!entity.has(FleetCommandComponent)) {
            entity.configure { it += FleetCommandComponent() }
        }
        val fleet = entity[FleetCommandComponent]

        if (docking) return

        moveForce.set(joystickVector)

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveForce.x += 1f

        if (!moveForce.isZero) {
            moveForce.nor().scl((vel.acceleration * 0.1f) * deltaTime)
            vel.v.add(moveForce)
            orientation.angleRad = vel.v.angleRad()
        }

        if (weapon != null) {
            if (!aimVector.isZero) {
                val angle = aimVector.angleRad()
                if (fleet.controlMode == FleetCommandComponent.ControlMode.ATTACK_DIRECTION) {
                    weapon.aimAngleRad = angle
                } else {
                    val idx = fleet.selectedSlotIndex.coerceIn(0, 3)
                    fleet.slotFocusAngles[idx] = angle
                    fleet.slotFocusPoints[idx].set(com.badlogic.gdx.math.MathUtils.cos(angle) * 15f, com.badlogic.gdx.math.MathUtils.sin(angle) * 15f)
                }
            } else if (!vel.v.isZero(0.1f)) {
                if (fleet.controlMode == FleetCommandComponent.ControlMode.ATTACK_DIRECTION) {
                    weapon.aimAngleRad = vel.v.angleRad()
                }
            }
            weapon.isFiring = isFiringRequested || Gdx.input.isKeyPressed(Keys.SPACE)
        }
    }

    fun getTouchWorld(screenX: Int, screenY: Int): Vector2 {
        val vec = com.badlogic.gdx.math.Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
        gameViewport.unproject(vec)
        return Vector2(vec.x, vec.y)
    }
}

class MoveSystem : IteratingSystem(family { all(PositionComponent, VelocityComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        // 1. PHYSICAL TRAIL RECORDING (v3.0.5)
        // If entity has a TrailComponent, physically record current position before moving.
        if (entity.has(TrailComponent)) {
            val trail = entity[TrailComponent]
            if (trail.history.size >= trail.maxLength) trail.history.removeAt(0)
            trail.history.add(Vector2(pos.pos))
        }

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        var frictionFactor = vel.friction
        if (entity.has(SalvageComponent)) {
            frictionFactor = 0.5f
        } else if (entity.has(PlayerComponent) && entity.has(EvolutionComponent)) {
            val evo = entity[EvolutionComponent]
            val input = world.system<PlayerInputSystem>()
            if (evo.currentTier >= 9 && input.joystickVector.isZero) {
                frictionFactor *= 5.0f
                if (vel.v.len2() > 10f) spawnBrakeFlare(pos.pos.x, pos.pos.y, vel.v.angleRad())
            }
        }
        if (!entity.has(ProjectileComponent)) {
            vel.v.scl(1f - (frictionFactor * deltaTime))
        }
    }

    private fun spawnBrakeFlare(x: Float, y: Float, angleRad: Float) {
        world.entity {
            it += PositionComponent().apply { pos.set(x + com.badlogic.gdx.math.MathUtils.cos(angleRad)*2f, y + com.badlogic.gdx.math.MathUtils.sin(angleRad)*2f) }
            it += VelocityComponent().apply {
                v.set(10f, 0f).setAngleRad(angleRad + com.badlogic.gdx.math.MathUtils.PI)
                friction = 2f
            }
            it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = Color.ORANGE, size = 0.4f)
            it += LifeSpanComponent(remaining = 0.15f)
        }
    }
}

/**
 * Tactical Camera Orchestrator (v2.9.1).
 * ROLE: Links camera zoom to ship engagement intent.
 */
class CameraSystem(private val camera: OrthographicCamera) : IteratingSystem(family { all(PlayerComponent, PositionComponent, VelocityComponent, EvolutionComponent, TacticalStatsComponent) }) {
    private val targetPos = Vector2()
    private val lerpSpeed = 2.5f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val stats = entity[TacticalStatsComponent]
        val module = if (entity.has(WeaponModuleComponent)) entity[WeaponModuleComponent] else null

        // 1. BASE ZOOM: Linked to modular attackDistance
        val finalAttackRange = stats.attackDistance * (module?.passiveDistanceMod ?: 1.0f)
        val targetBaseZoom = 0.5f + (finalAttackRange / 100f)

        targetPos.set(pos.x + (vel.x * 0.2f), pos.y + (vel.y * 0.2f))
        camera.position.x += (targetPos.x - camera.position.x) * lerpSpeed * deltaTime
        camera.position.y += (targetPos.y - camera.position.y) * lerpSpeed * deltaTime

        // 2. SPEED ZOOM: Dynamic FOV expansion during flight
        val speedPercent = (vel.len() / 50f).coerceIn(0f, 1f)
        val targetZoom = targetBaseZoom * (1.0f + (speedPercent * 0.2f))

        camera.zoom += (targetZoom - camera.zoom) * deltaTime
        camera.update()
    }
}

class OriginSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent) }) {
    private val allPosFamily by lazy { world.family { all(PositionComponent) } }
    private val threshold = 1000f
    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        if (Math.abs(playerPos.x) > threshold || Math.abs(playerPos.y) > threshold) {
            val shift = Vector2(playerPos)
            allPosFamily.forEach { target -> target[PositionComponent].pos.sub(shift) }
        }
    }
}
