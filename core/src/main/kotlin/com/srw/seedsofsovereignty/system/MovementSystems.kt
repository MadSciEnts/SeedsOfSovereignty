package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer

class PlayerInputSystem : IteratingSystem(family { all(PlayerComponent, VelocityComponent) }) {
    private val moveForce = Vector2()
    val joystickVector = Vector2()
    val aimVector = Vector2()
    var isFiringRequested = false

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        val orientation = entity[OrientationComponent]
        val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null
        val docking = if (entity.has(DockingComponent)) entity[DockingComponent] else null

        if (!entity.has(FleetCommandComponent)) {
            entity.configure { it += FleetCommandComponent() }
        }
        val fleet = entity[FleetCommandComponent]

        if (docking?.isDocked == true) return

        moveForce.set(joystickVector)

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveForce.x += 1f

        if (!moveForce.isZero) {
            moveForce.nor().scl(vel.acceleration * deltaTime)
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
                    fleet.slotFocusPoints[idx].set(com.badlogic.gdx.math.MathUtils.cos(angle) * 150f, com.badlogic.gdx.math.MathUtils.sin(angle) * 150f)
                }
            } else if (!vel.v.isZero(0.1f)) {
                if (fleet.controlMode == FleetCommandComponent.ControlMode.ATTACK_DIRECTION) {
                    weapon.aimAngleRad = vel.v.angleRad()
                }
            }
            weapon.isFiring = isFiringRequested || Gdx.input.isKeyPressed(Keys.SPACE)
        }
    }
}

class MoveSystem : IteratingSystem(family { all(PositionComponent, VelocityComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        var frictionFactor = vel.friction

        // Task Fix: Replace legacy EssenceComponent with Salvage Pipeline (v0.1.9)
        if (entity.has(SalvageComponent)) {
            frictionFactor = 0.5f
        } else if (entity.has(PlayerComponent) && entity.has(EvolutionComponent)) {
            val evo = entity[EvolutionComponent]
            val input = world.system<PlayerInputSystem>()

            if (evo.currentTier >= 9 && input.joystickVector.isZero && !Gdx.input.isKeyPressed(Keys.W) && !Gdx.input.isKeyPressed(Keys.S) && !Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {
                frictionFactor *= 5.0f
                if (vel.v.len2() > 100f) spawnBrakeFlare(pos.pos.x, pos.pos.y, vel.v.angleRad())
            }
        }

        if (!entity.has(ProjectileComponent)) {
            vel.v.scl(1f - (frictionFactor * deltaTime))
        }
    }

    private fun spawnBrakeFlare(x: Float, y: Float, angleRad: Float) {
        world.entity {
            it += PositionComponent().apply { pos.set(x + com.badlogic.gdx.math.MathUtils.cos(angleRad)*20f, y + com.badlogic.gdx.math.MathUtils.sin(angleRad)*20f) }
            it += VelocityComponent().apply {
                v.set(100f, 0f).setAngleRad(angleRad + com.badlogic.gdx.math.MathUtils.PI)
                friction = 2f
            }
            it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = com.badlogic.gdx.graphics.Color.ORANGE, size = 4f)
            it += LifeSpanComponent(remaining = 0.15f)
        }
    }
}

class CameraSystem(private val camera: OrthographicCamera) : IteratingSystem(family { all(PlayerComponent, PositionComponent, VelocityComponent, EvolutionComponent) }) {
    private val targetPos = Vector2()
    private val lerpSpeed = 2.5f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val evo = entity[EvolutionComponent]

        val currentSize = ProceduralShipDrawer.getShipSize(evo.currentTier, evo.subTier)
        val baseZoom = 1.0f + (currentSize - 8f) / 85f

        targetPos.set(pos.x + (vel.x * 0.5f), pos.y + (vel.y * 0.5f))

        camera.position.x += (targetPos.x - camera.position.x) * lerpSpeed * deltaTime
        camera.position.y += (targetPos.y - camera.position.y) * lerpSpeed * deltaTime

        val speedPercent = (vel.len() / 500f).coerceIn(0f, 1f)
        val targetZoom = baseZoom * (1.0f + (speedPercent * 0.2f))

        camera.zoom += (targetZoom - camera.zoom) * deltaTime
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
