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
import com.srw.seedsofsovereignty.manager.SectorManager
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

class SectorPopulationSystem(
    private val camera: OrthographicCamera,
    private val sectorManager: SectorManager
) : IntervalSystem(interval = Fixed(5f)) {
    override fun onTick() {
        val sector = sectorManager.currentSector
        val player = world.family { all(PlayerComponent, EvolutionComponent) }.firstOrNull() ?: return
        val currentTier = player[EvolutionComponent].currentTier

        // 1. Density Control
        val enemyCount = world.family { all(EnemyComponent) }.numEntities
        val stationCount = world.family { all(StationComponent) }.numEntities

        // 2. Faction-Based Spawning logic
        when(sector.faction) {
            Faction.SWARM -> {
                if (enemyCount < 8) spawnEnemy(currentTier, "Swarm")
                if (stationCount < 1) spawnStation(currentTier, "Hive")
            }
            Faction.VANGUARD -> {
                if (enemyCount < 4) spawnEnemy(currentTier, "Vanguard Guard")
                if (stationCount < 2) spawnStation(currentTier, "Outpost")
            }
            Faction.ANCIENTS -> {
                if (enemyCount < 2) spawnEnemy(currentTier, "Ancient Sentry")
                if (stationCount < 1) spawnStation(currentTier, "Monolith")
            }
            Faction.NEUTRAL -> {
                if (enemyCount < 3) spawnEnemy(currentTier, "Scavenger")
                if (stationCount < 3) spawnStation(currentTier, "Trading Hub")
            }
        }
    }

    private fun spawnEnemy(tier: Int, type: String) {
        val spawnRadius = 800f * camera.zoom
        val angle = MathUtils.random(0f, MathUtils.PI2)
        val spawnX = camera.position.x + MathUtils.cos(angle) * spawnRadius
        val spawnY = camera.position.y + MathUtils.sin(angle) * spawnRadius

        world.entity {
            it += PositionComponent().apply { pos.set(spawnX, spawnY) }
            it += VelocityComponent().apply { acceleration = 200f + (tier * 20f); friction = 1.0f }
            it += OrientationComponent()
            it += HealthComponent(current = 20f * tier, max = 20f * tier)
            it += EnemyComponent(type = type)
            it += WeaponComponent(cooldown = 1.5f, barrels = 1)
            it += WeaponModuleComponent(name = "$type Cannon", projectileTier = tier)
            it += VisibleComponent()
        }
    }

    private fun spawnStation(tier: Int, name: String) {
        val playerPos = world.family { all(PlayerComponent) }.first()[PositionComponent].pos
        val angle = MathUtils.random(0f, MathUtils.PI2)
        val dist = 1500f
        world.entity {
            it += PositionComponent().apply { pos.set(playerPos.x + MathUtils.cos(angle) * dist, playerPos.y + MathUtils.sin(angle) * dist) }
            it += StationComponent(name = "$name ${MathUtils.random(100, 999)}", radius = 200f)
            it += MarketComponent().apply {
                for (i in 0 until 3) weaponOffers.add(ModuleGenerator.generateWeapon(tier))
            }
        }
    }
}
