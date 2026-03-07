package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ModuleGenerator
import ktx.graphics.use

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
    private val parallaxFactors = listOf(0.05f, 0.12f, 0.25f)
    private val starCamera = Matrix4()
    private val starScale = 0.45f
    private val zoomInfluence = 0.1f

    override fun onTick() {
        if (layers.isEmpty()) return

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        starCamera.setToOrtho2D(0f, 0f, w, h)
        val originalMatrix = batch.projectionMatrix.cpy()
        batch.projectionMatrix = starCamera

        batch.use { b ->
            layers.forEachIndexed { index, texture ->
                val factor = parallaxFactors.getOrElse(index) { 0.1f }

                val zoomFactor = 1f + (camera.zoom - 1f) * zoomInfluence
                val effectiveScale = starScale * zoomFactor

                val uWidth = (w * effectiveScale) / texture.width
                val vHeight = (h * effectiveScale) / texture.height

                val uCenter = (camera.position.x * factor) / texture.width
                val vCenter = (camera.position.y * factor) / texture.height

                val u = uCenter - uWidth / 2f
                val v = vCenter - vHeight / 2f
                val u2 = uCenter + uWidth / 2f
                val v2 = vCenter + vHeight / 2f

                b.draw(texture, 0f, 0f, w, h, u, v, u2, v2)
            }
        }

        batch.projectionMatrix = originalMatrix
    }
}

class EnemySpawnSystem(
    private val camera: OrthographicCamera
) : IntervalSystem(interval = Fixed(2f)) {
    override fun onTick() {
        val playerFamily = world.family { all(PlayerComponent, EvolutionComponent) }
        val player = playerFamily.firstOrNull() ?: return
        val currentTier = player[EvolutionComponent].currentTier
        val playerPos = player[PositionComponent].pos
        val stationFamily = world.family { all(StationComponent, PositionComponent) }

        val nearStation = stationFamily.any { it[PositionComponent].pos.dst2(playerPos) < 490000f }
        if (nearStation) return

        val enemyCount = world.family { all(EnemyComponent) }.numEntities
        if (enemyCount < 5) {
            val spawnRadius = 800f * camera.zoom
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
                it += OrientationComponent()

                // Unified Vitals: Every enemy MUST have a HealthComponent to take damage and drop loot
                val hp = when(currentTier) {
                    1 -> 20f
                    2 -> 40f
                    3 -> 60f
                    4 -> 100f
                    else -> 20f
                }
                it += HealthComponent(current = hp, max = hp)

                it += EnemyComponent(
                    type = when(currentTier) {
                        1 -> "Swarm"
                        2 -> "Reaper"
                        3 -> "Phantom"
                        else -> "Apex"
                    }
                )

                val baseFireRate = when(currentTier) {
                    1 -> 2.0f
                    2 -> 1.5f
                    else -> 1.0f
                }

                it += WeaponComponent(
                    cooldown = MathUtils.random(0f, baseFireRate),
                    barrels = 1
                )

                val enemyTier = if (it[EnemyComponent].type == "Reaper") 2 else 1
                it += WeaponModuleComponent(
                    name = "Enemy Pulse",
                    projectileTier = enemyTier,
                    projectileSpeed = 600f,
                    effects = mutableListOf(
                        ProjectileEffect(EffectType.HULL_DAMAGE, 5f, vfx = ImpactVfxType.SPARK_RED),
                        ProjectileEffect(EffectType.SHIELD_DAMAGE, 5f, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE)
                    )
                )

                it += VisibleComponent()
            }
        }
    }
}

class StationSpawnSystem : IntervalSystem(interval = Fixed(10f)) {
    override fun onTick() {
        val playerFamily = world.family { all(PlayerComponent, PositionComponent, SensorComponent) }
        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val sensorRadius = player[SensorComponent].radius
        val tier = player[EvolutionComponent].currentTier

        if (world.family { all(StationComponent) }.numEntities < 3) {
            val spawnDist = sensorRadius * MathUtils.random(1.0f, 2.5f)
            val angle = MathUtils.random(0f, MathUtils.PI2)
            val spawnX = playerPos.x + MathUtils.cos(angle) * spawnDist
            val spawnY = playerPos.y + MathUtils.sin(angle) * spawnDist

            world.entity {
                it += PositionComponent().apply { pos.set(spawnX, spawnY) }
                it += StationComponent(name = "Outpost ${MathUtils.random(100, 999)}", radius = 200f)

                it += MarketComponent().apply {
                    for (i in 0 until 3) {
                        weaponOffers.add(ModuleGenerator.generateWeapon(tier))
                    }
                    if (tier >= 5) {
                        for (i in 0 until 2) {
                            droneOffers.add(ModuleGenerator.generateDroneModule(tier))
                        }
                    }
                }
            }
        }
    }
}
