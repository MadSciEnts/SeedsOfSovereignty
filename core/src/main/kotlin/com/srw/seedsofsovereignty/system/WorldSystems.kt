package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ScreenUtils
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
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
                it += PositionComponent().apply { pos.set(1000f, 1000f) }
                it += StationComponent(name = "Nexus Prime", radius = 200f)
                it += VisibleComponent()
            }
        }
    }
}
