package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class ClearScreenSystem : IntervalSystem() {
    override fun onTick() {
        ScreenUtils.clear(0.02f, 0.02f, 0.05f, 1f)
    }
}

class StarfieldSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera,
    private val layers: List<Texture>
) : IntervalSystem() {
    private val parallaxFactors = listOf(0.1f, 0.3f, 0.6f)

    override fun onTick() {
        if (layers.isEmpty()) return

        batch.use(camera.combined) { b ->
            layers.forEachIndexed { index, texture ->
                val factor = parallaxFactors.getOrElse(index) { 0.1f }
                val w = camera.viewportWidth * camera.zoom
                val h = camera.viewportHeight * camera.zoom
                val x = camera.position.x - w / 2f
                val y = camera.position.y - h / 2f

                val uWidth = w / texture.width
                val vHeight = h / texture.height
                val uCenter = (camera.position.x * factor) / texture.width
                val vCenter = (camera.position.y * factor) / texture.height

                val u = uCenter - uWidth / 2f
                val v = vCenter - vHeight / 2f
                val u2 = uCenter + uWidth / 2f
                val v2 = vCenter + vHeight / 2f

                b.draw(texture, x, y, w, h, u, v, u2, v2)
            }
        }
    }
}

class PlayerInputSystem : IteratingSystem(family { all(PlayerComponent, VelocityComponent) }) {
    private val moveForce = Vector2()
    val joystickVector = Vector2()
    val aimVector = Vector2()
    var isFiringRequested = false

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        val orientation = entity[OrientationComponent]
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null

        moveForce.set(joystickVector)

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveForce.x += 1f

        if (!moveForce.isZero) {
            moveForce.nor().scl(vel.acceleration * deltaTime)
            vel.v.add(moveForce)

            if ((evo?.currentTier ?: 1) < 4) {
                orientation.angleRad = moveForce.angleRad()
            }
        }

        if ((evo?.currentTier ?: 1) >= 4) {
            if (!aimVector.isZero) {
                orientation.angleRad = aimVector.angleRad()
            } else if (!moveForce.isZero) {
                orientation.angleRad = moveForce.angleRad()
            }
        }

        if (entity.has(WeaponComponent)) {
            entity[WeaponComponent].isFiring = isFiringRequested || Gdx.input.isKeyPressed(Keys.SPACE)
        }
    }
}

class WeaponSystem : IteratingSystem(family { all(WeaponComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val weapon = entity[WeaponComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val angleRad = entity[OrientationComponent].angleRad

        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = evo?.currentTier ?: 1

        if (weapon.cooldown > 0f) {
            weapon.cooldown -= deltaTime
        }

        if (weapon.isFiring && weapon.cooldown <= 0f) {
            weapon.cooldown = weapon.fireRate

            for (i in 0 until weapon.barrels) {
                if (weapon.barrels == 2 && i != weapon.currentBarrel) continue

                world.entity { e ->
                    e += PositionComponent().apply {
                        this.pos.set(pos)
                        if (weapon.barrels >= 2) {
                            val offset = if (i == 0) -10f else if (i == 1) 10f else 0f
                            val ox = MathUtils.cos(angleRad + MathUtils.PI/2f) * offset
                            val oy = MathUtils.sin(angleRad + MathUtils.PI/2f) * offset
                            this.pos.add(ox, oy)
                        }
                    }
                    e += VelocityComponent().apply {
                        this.v.set(1000f, 0f).setAngleRad(angleRad).add(vel)
                    }
                    e += ProjectileComponent(
                        tier = tier,
                        damage = when(tier) {
                            1 -> 10f
                            2 -> 25f
                            3 -> 15f
                            4 -> 40f
                            else -> 10f
                        }
                    )
                    e += LifeSpanComponent(remaining = 1.5f)
                }
            }

            if (weapon.barrels > 1) {
                weapon.currentBarrel = (weapon.currentBarrel + 1) % weapon.barrels
            }
        }
    }
}

class DroneSystem : IteratingSystem(family { all(DroneComponent, PositionComponent, OrientationComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val orientation = entity[OrientationComponent]

        drone.orbitAngle += drone.orbitSpeed * deltaTime
        val ownerPos = drone.owner[PositionComponent].pos
        pos.set(
            ownerPos.x + MathUtils.cos(drone.orbitAngle) * drone.orbitRadius,
            ownerPos.y + MathUtils.sin(drone.orbitAngle) * drone.orbitRadius
        )

        var nearestEnemy: Entity? = null
        var minDistSq = Float.MAX_VALUE

        enemyFamily.forEach { enemy ->
            val ePos = enemy[PositionComponent].pos
            val d2 = pos.dst2(ePos)
            if (d2 < minDistSq) {
                minDistSq = d2
                nearestEnemy = enemy
            }
        }

        nearestEnemy?.let { enemy ->
            val ePos = enemy[PositionComponent].pos
            orientation.angleRad = Vector2(ePos).sub(pos).angleRad()

            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = true
            }
        } ?: run {
            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = false
            }
        }
    }
}

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

class EnemySpawnSystem(
    private val camera: OrthographicCamera
) : IntervalSystem(interval = Fixed(2f)) {
    override fun onTick() {
        val player = world.family { all(PlayerComponent, EvolutionComponent) }.firstOrNull() ?: return
        val currentTier = player[EvolutionComponent].currentTier
        val enemyCount = world.family { all(EnemyComponent) }.numEntities

        if (enemyCount < 5) {
            val spawnRadius = 600f
            val angle = MathUtils.random(0f, MathUtils.PI2)
            val spawnX = camera.position.x + MathUtils.cos(angle) * spawnRadius
            val spawnY = camera.position.y + MathUtils.sin(angle) * spawnRadius

            world.entity {
                it += PositionComponent().apply { pos.set(spawnX, spawnY) }
                it += VelocityComponent().apply {
                    acceleration = when(currentTier) {
                        1 -> 150f
                        2 -> 250f
                        3 -> 200f
                        else -> 300f
                    }
                    friction = 1.0f
                }
                it += EnemyComponent(
                    health = when(currentTier) {
                        1 -> 20f
                        2 -> 40f
                        3 -> 60f
                        4 -> 100f
                        else -> 20f
                    },
                    type = when(currentTier) {
                        1 -> "Swarm"
                        2 -> "Reaper"
                        3 -> "Phantom"
                        else -> "Apex"
                    }
                )
            }
        }
    }
}

class StationSpawnSystem : IntervalSystem(interval = Fixed(10f)) {
    override fun onTick() {
        if (world.family { all(StationComponent) }.numEntities == 0) {
            world.entity {
                it += PositionComponent().apply { pos.set(1500f, 1500f) }
                it += StationComponent(name = "Nexus Prime", radius = 200f)
                it += VisibleComponent() // Always visible on radar for now
            }
        }
    }
}

class EnemyAISystem : IteratingSystem(family { all(EnemyComponent, PositionComponent, VelocityComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent) } }
    private val steerForce = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]

        playerFamily.firstOrNull()?.let { player ->
            val playerPos = player[PositionComponent].pos
            steerForce.set(playerPos).sub(pos).nor().scl(vel.acceleration * deltaTime)
            vel.v.add(steerForce)
        }
    }
}

class CombatSystem : IteratingSystem(family { all(ProjectileComponent, PositionComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projComp = entity[ProjectileComponent]

        enemyFamily.forEach { enemy ->
            val enemyPos = enemy[PositionComponent].pos
            if (projPos.dst2(enemyPos) < 1600f) {
                val enemyComp = enemy[EnemyComponent]
                enemyComp.health -= projComp.damage
                world -= entity

                if (enemyComp.health <= 0f) {
                    val ePos = Vector2(enemyPos)
                    world.entity { e ->
                        e += PositionComponent().apply { pos.set(ePos) }
                        e += VelocityComponent().apply { friction = 0.5f }
                        e += EssenceComponent()
                        e += LifeSpanComponent(remaining = 15f)
                    }
                    world -= enemy
                }
                return@forEach
            }
        }
    }
}

class CollectionSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, EvolutionComponent) }) {
    private val essenceFamily by lazy { world.family { all(EssenceComponent, PositionComponent, VelocityComponent) } }
    private val pullRadiusSq = 40000f
    private val collectRadiusSq = 900f

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val evolution = entity[EvolutionComponent]

        essenceFamily.forEach { essence ->
            val essencePos = essence[PositionComponent].pos
            val vel = essence[VelocityComponent].v
            val distSq = playerPos.dst2(essencePos)

            if (distSq < pullRadiusSq) {
                val pullForce = Vector2(playerPos).sub(essencePos).nor().scl(800f * deltaTime)
                vel.add(pullForce)

                if (distSq < collectRadiusSq) {
                    val essComp = essence[EssenceComponent]
                    if (!essComp.isCollected) {
                        essComp.isCollected = true
                        evolution.essenceCollected += essComp.amount
                        world -= essence
                    }
                }
            }
        }
    }
}

class EvolutionSystem : IteratingSystem(family { all(PlayerComponent, EvolutionComponent, WeaponComponent, VelocityComponent) }) {
    private val droneFamily by lazy { world.family { all(DroneComponent) } }

    override fun onTickEntity(entity: Entity) {
        val evo = entity[EvolutionComponent]
        val weapon = entity[WeaponComponent]
        val vel = entity[VelocityComponent]

        if (evo.essenceCollected >= evo.essenceRequired) {
            evo.subTier++
            evo.essenceCollected = 0
            evo.essenceRequired = (evo.essenceRequired * 1.5f).toInt()

            when (evo.currentTier) {
                1 -> {
                    when (evo.subTier) {
                        2 -> weapon.fireRate = 0.5f
                        3 -> { weapon.fireRate = 0.33f; weapon.barrels = 2 }
                        4 -> { weapon.fireRate = 0.25f; weapon.barrels = 3 }
                        5 -> { evo.currentTier = 2; evo.subTier = 1; weapon.fireRate = 0.2f; weapon.barrels = 1; vel.acceleration += 150f }
                    }
                }
                2 -> {
                    when (evo.subTier) {
                        2 -> weapon.fireRate = 0.15f
                        3 -> { weapon.fireRate = 0.12f; weapon.barrels = 2 }
                        4 -> { weapon.fireRate = 0.1f; weapon.barrels = 4 }
                        5 -> { evo.currentTier = 3; evo.subTier = 1; weapon.barrels = 1; vel.acceleration += 100f }
                    }
                }
                3 -> {
                    val droneCount = droneFamily.numEntities
                    if (droneCount < evo.subTier) {
                        for (i in 0 until (evo.subTier - droneCount)) {
                            world.entity { d ->
                                d += PositionComponent()
                                d += OrientationComponent()
                                d += DroneComponent(owner = entity, orbitAngle = i * MathUtils.PI2 / 4f, orbitRadius = 60f + i * 10f)
                                d += WeaponComponent(fireRate = 0.5f)
                                d += VelocityComponent()
                            }
                        }
                    }

                    if (evo.subTier >= 5) {
                        evo.currentTier = 4
                        evo.subTier = 1
                        weapon.fireRate = 0.1f
                        weapon.barrels = 1
                    }
                }
            }
        }
    }
}

class LifeSpanSystem : IteratingSystem(family { all(LifeSpanComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val life = entity[LifeSpanComponent]
        life.remaining -= deltaTime
        if (life.remaining <= 0f) {
            world -= entity
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

class CameraSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(PlayerComponent, PositionComponent, VelocityComponent) }) {
    private val targetPos = Vector2()
    private val lerpSpeed = 2.5f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v

        targetPos.set(pos.x + (vel.x * 0.5f), pos.y + (vel.y * 0.5f))

        camera.position.x += (targetPos.x - camera.position.x) * lerpSpeed * deltaTime
        camera.position.y += (targetPos.y - camera.position.y) * lerpSpeed * deltaTime

        val speedPercent = (vel.len() / 500f).coerceIn(0f, 1f)
        val targetZoom = 1.0f + (speedPercent * 0.4f)
        camera.zoom += (targetZoom - camera.zoom) * deltaTime

        camera.update()
    }
}

class RenderSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera,
    private val drawer: ShapeDrawer
) : IteratingSystem(family { all(PositionComponent) }) {

    override fun onTick() {
        batch.use(camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos

        if (entity.has(VisibleComponent) || entity.has(PlayerComponent) || entity.has(ProjectileComponent) || entity.has(EssenceComponent)) {
            if (entity.has(PlayerComponent)) {
                val angleRad = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else MathUtils.PI / 2f
                val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
                drawPlayerShip(pos.x, pos.y, angleRad, evo?.currentTier ?: 1, evo?.subTier ?: 1)
            } else if (entity.has(ProjectileComponent)) {
                val proj = entity[ProjectileComponent]
                val vel = entity[VelocityComponent].v
                drawProjectile(pos.x, pos.y, vel.angleRad(), proj.tier)
            } else if (entity.has(EnemyComponent)) {
                val enemy = entity[EnemyComponent]
                drawEnemy(pos.x, pos.y, enemy.type)
            } else if (entity.has(EssenceComponent)) {
                drawEssence(pos.x, pos.y)
            } else if (entity.has(DroneComponent)) {
                val angleRad = entity[OrientationComponent].angleRad
                drawDrone(pos.x, pos.y, angleRad)
            } else if (entity.has(StationComponent)) {
                drawStation(pos.x, pos.y)
            } else {
                drawDetectedObject(pos.x, pos.y)
            }
        }
    }

    private fun drawPlayerShip(x: Float, y: Float, rotation: Float, tier: Int, subTier: Int) {
        val size = when(tier) {
            1 -> 16f
            2 -> 20f
            3 -> 24f
            4 -> 30f
            else -> 16f
        }
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        val x1 = x + size * cos
        val y1 = y + size * sin

        val angleOffset = if (tier >= 2) 2.2f else 2.4f
        val x2 = x + size * MathUtils.cos(rotation + angleOffset)
        val y2 = y + size * MathUtils.sin(rotation + angleOffset)
        val x3 = x + size * MathUtils.cos(rotation - angleOffset)
        val y3 = y + size * MathUtils.sin(rotation - angleOffset)

        val color = when(tier) {
            1 -> Color.CYAN
            2 -> Color.VIOLET
            3 -> Color.GOLD
            4 -> Color.ORANGE
            else -> Color.CYAN
        }
        drawer.setColor(color)
        drawer.line(x1, y1, x2, y2, 2f)
        drawer.line(x2, y2, x3, y3, 2f)
        drawer.line(x3, y3, x1, y1, 2f)

        drawer.setColor(Color.WHITE)
        if (tier == 1) {
            when (subTier) {
                2 -> drawWeaponPoint(x + 10f * cos, y + 10f * sin, rotation, 4f)
                3 -> {
                    val s1 = rotation + MathUtils.PI / 2f
                    val s2 = rotation - MathUtils.PI / 2f
                    drawWeaponPoint(x + 8f * MathUtils.cos(s1), y + 8f * MathUtils.sin(s1), rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s2), y + 8f * MathUtils.sin(s2), rotation, 4f)
                }
                4 -> {
                    val s1 = rotation + MathUtils.PI / 2f
                    val s2 = rotation - MathUtils.PI / 2f
                    drawWeaponPoint(x + 10f * cos, y + 10f * sin, rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s1), y + 8f * MathUtils.sin(s1), rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s2), y + 8f * MathUtils.sin(s2), rotation, 4f)
                }
            }
        } else if (tier == 4) {
            drawer.setColor(Color.GOLD)
            drawer.filledCircle(x, y, 6f)
            drawer.setColor(Color.WHITE)
            drawer.line(x, y, x + 12f * cos, y + 12f * sin, 3f)
        }

        if (tier >= 3) {
            drawer.setColor(color)
            drawer.circle(x, y, size * 0.8f, 1.5f)
        } else if (tier >= 2) {
            drawer.line(x, y, x - size * 0.5f * cos, y - size * 0.5f * sin, 2f)
        }

        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, 3f)
    }

    private fun drawWeaponPoint(x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)
        val x1 = x + size * cos
        val y1 = y + size * sin
        val x2 = x + size * MathUtils.cos(rotation + 2.4f)
        val y2 = y + size * MathUtils.sin(rotation + 2.4f)
        val x3 = x + size * MathUtils.cos(rotation - 2.4f)
        val y3 = y + size * MathUtils.sin(rotation - 2.4f)
        drawer.line(x1, y1, x2, y2, 1f)
        drawer.line(x2, y2, x3, y3, 1f)
        drawer.line(x3, y3, x1, y1, 1f)
    }

    private fun drawDrone(x: Float, y: Float, rotation: Float) {
        drawer.setColor(Color.GOLD)
        drawer.circle(x, y, 6f, 1.5f)
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)
        drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1f)
    }

    private fun drawProjectile(x: Float, y: Float, rotation: Float, tier: Int) {
        when (tier) {
            1 -> {
                drawer.setColor(Color.YELLOW)
                val length = 8f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 1.5f)
            }
            2 -> {
                drawer.setColor(Color.VIOLET)
                val length = 12f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 3f)
            }
            3 -> {
                drawer.setColor(Color.GOLD)
                val length = 10f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.filledCircle(x, y, 3f)
                drawer.line(x, y, x + length * cos, y + length * sin, 2f)
            }
            4 -> {
                drawer.setColor(Color.ORANGE)
                drawer.filledCircle(x, y, 4f)
                drawer.setColor(Color.WHITE)
                drawer.circle(x, y, 6f, 1f)
            }
            else -> {
                drawer.setColor(Color.WHITE)
                val length = 8f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 1.5f)
            }
        }
    }

    private fun drawEnemy(x: Float, y: Float, type: String) {
        when (type) {
            "Reaper" -> {
                drawer.setColor(Color.PURPLE)
                drawer.circle(x, y, 18f, 3f)
                drawer.setColor(Color.RED)
                drawer.circle(x, y, 10f, 2f)
            }
            "Phantom" -> {
                drawer.setColor(Color.TEAL)
                drawer.rectangle(x-10f, y-10f, 20f, 20f, 2f)
                drawer.setColor(Color.RED)
                drawer.filledCircle(x, y, 4f)
            }
            else -> {
                drawer.setColor(Color.RED)
                drawer.circle(x, y, 15f, 2f)
                drawer.setColor(Color.ORANGE)
                drawer.line(x - 10f, y, x + 10f, y, 1f)
                drawer.line(x, y - 10f, x, y + 10f, 1f)
            }
        }
    }

    private fun drawEssence(x: Float, y: Float) {
        drawer.setColor(Color.CYAN)
        val time = Gdx.graphics.frameId.toFloat() * 0.1f
        val pulse = 4f + MathUtils.sin(time) * 2f
        drawer.filledCircle(x, y, pulse)
        drawer.setColor(Color.WHITE)
        drawer.circle(x, y, pulse, 1f)
    }

    private fun drawStation(x: Float, y: Float) {
        drawer.setColor(Color.CYAN)
        drawer.circle(x, y, 150f, 4f)
        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x - 60f, y - 60f, 120f, 120f)
        drawer.setColor(Color.WHITE)
        drawer.rectangle(x - 60f, y - 60f, 120f, 120f, 2f)

        drawer.setColor(Color.CYAN)
        drawer.line(x - 60f, y, x + 60f, y, 1f)
        drawer.line(x, y - 60f, x, y + 60f, 1f)
    }

    private fun drawDetectedObject(x: Float, y: Float) {
        drawer.setColor(Color.GRAY)
        drawer.rectangle(x - 8f, y - 8f, 16f, 16f, 1f)
        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x - 6f, y - 6f, 12f, 12f)
    }
}

class RadarRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PositionComponent, PlayerComponent, SensorComponent) }) {

    private val targetsFamily by lazy { world.family { all(PositionComponent, VisibleComponent).none(PlayerComponent, ProjectileComponent) } }
    private var timeCount = 0f

    override fun onTick() {
        timeCount += deltaTime
        uiViewport.camera.update()
        batch.use(uiViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val sensor = entity[SensorComponent]

        val radarRadius = 120f
        val padding = 40f
        val centerX = uiViewport.worldWidth - radarRadius - padding
        val centerY = uiViewport.worldHeight - radarRadius - padding

        drawer.setColor(0f, 0.6f, 0f, 0.3f)
        drawer.filledCircle(centerX, centerY, radarRadius)
        drawer.setColor(Color.GREEN)
        drawer.circle(centerX, centerY, radarRadius, 3f)

        val scanAngle = (timeCount * 3f) % MathUtils.PI2
        drawer.setColor(Color.LIME)
        drawer.line(centerX, centerY, centerX + MathUtils.cos(scanAngle) * radarRadius, centerY + MathUtils.sin(scanAngle) * radarRadius, 2f)

        targetsFamily.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val relativePos = Vector2(targetPos).sub(playerPos)

            val scale = radarRadius / sensor.radius
            val rx = centerX + relativePos.x * scale
            val ry = centerY + relativePos.y * scale

            if (Vector2.dst(centerX, centerY, rx, ry) <= radarRadius) {
                if (target.has(EnemyComponent)) {
                    drawer.setColor(Color.RED)
                } else if (target.has(EssenceComponent)) {
                    drawer.setColor(Color.CYAN)
                } else if (target.has(StationComponent)) {
                    drawer.setColor(Color.CYAN)
                } else {
                    drawer.setColor(Color.WHITE)
                }
                drawer.filledCircle(rx, ry, 4f)
            }
        }

        drawer.setColor(Color.WHITE)
        drawer.filledCircle(centerX, centerY, 5f)
    }
}

class HUDSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent, EvolutionComponent) }) {

    override fun onTick() {
        batch.use(uiViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val evo = entity[EvolutionComponent]

        val barWidth = 400f
        val barHeight = 10f
        val x = (uiViewport.worldWidth - barWidth) / 2f
        val y = 30f

        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x, y, barWidth, barHeight)

        val progress = (evo.essenceCollected.toFloat() / evo.essenceRequired.toFloat()).coerceIn(0f, 1f)
        drawer.setColor(Color.CYAN)
        drawer.filledRectangle(x, y, barWidth * progress, barHeight)

        drawer.setColor(Color.WHITE)
        drawer.rectangle(x, y, barWidth, barHeight, 1f)
    }
}

class StationSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, DockingComponent) }) {
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val docking = entity[DockingComponent]

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

class ApertureSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent, DockingComponent, ApertureComponent) }) {

    override fun onTick() {
        batch.use(uiViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val docking = entity[DockingComponent]
        val aperture = entity[ApertureComponent]

        if (docking.isDocked) {
            if (aperture.state == ApertureComponent.State.CLOSED) aperture.state = ApertureComponent.State.OPENING
        } else {
            if (aperture.state == ApertureComponent.State.OPEN) aperture.state = ApertureComponent.State.CLOSING
        }

        val speed = 2f
        when (aperture.state) {
            ApertureComponent.State.OPENING -> {
                aperture.openProgress += speed * deltaTime
                if (aperture.openProgress >= 1f) {
                    aperture.openProgress = 1f
                    aperture.state = ApertureComponent.State.OPEN
                }
            }
            ApertureComponent.State.CLOSING -> {
                aperture.openProgress -= speed * deltaTime
                if (aperture.openProgress <= 0f) {
                    aperture.openProgress = 0f
                    aperture.state = ApertureComponent.State.CLOSED
                }
            }
            else -> {}
        }

        if (aperture.openProgress > 0f) {
            val barHeight = (uiViewport.worldHeight / 2f) * (1f - aperture.openProgress)
            drawer.setColor(0f, 0f, 0f, 0.8f)

            drawer.filledRectangle(0f, uiViewport.worldHeight - barHeight, uiViewport.worldWidth, barHeight)
            drawer.filledRectangle(0f, 0f, uiViewport.worldWidth, barHeight)

            drawer.setColor(Color.CYAN)
            drawer.line(0f, uiViewport.worldHeight - barHeight, uiViewport.worldWidth, uiViewport.worldHeight - barHeight, 2f)
            drawer.line(0f, barHeight, uiViewport.worldWidth, barHeight, 2f)
        }
    }
}

class OriginSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent) }) {
    private val allPosFamily by lazy { world.family { all(PositionComponent) } }
    private val threshold = 10000f

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        if (Math.abs(playerPos.x) > threshold || Math.abs(playerPos.y) > threshold) {
            val shift = Vector2(playerPos)
            allPosFamily.forEach { target ->
                target[PositionComponent].pos.sub(shift)
            }
        }
    }
}
