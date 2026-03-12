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
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.util.ModuleGenerator
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import ktx.graphics.use

class ClearScreenSystem : IntervalSystem() {
    override fun onTick() { ScreenUtils.clear(0.02f, 0.02f, 0.05f, 1f) }
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
        val w = Gdx.graphics.width.toFloat(); val h = Gdx.graphics.height.toFloat()
        starCamera.setToOrtho2D(0f, 0f, w, h)
        val originalMatrix = batch.projectionMatrix.cpy()
        batch.projectionMatrix = starCamera
        batch.use { b ->
            layers.forEachIndexed { index, texture ->
                val factor = parallaxFactors.getOrElse(index) { 0.1f }
                val zoomFactor = 1f + (camera.zoom - 1f) * zoomInfluence
                val uWidth = (w * starScale * zoomFactor) / texture.width
                val vHeight = (h * starScale * zoomFactor) / texture.height
                val uCenter = (camera.position.x * factor * 10f) / texture.width
                val vCenter = (camera.position.y * factor * 10f) / texture.height
                b.draw(texture, 0f, 0f, w, h, uCenter - uWidth/2f, vCenter - vHeight/2f, uCenter + uWidth/2f, vCenter + vHeight/2f)
            }
        }
        batch.projectionMatrix = originalMatrix
    }
}

class StationSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, DockingComponent) }) {
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }
    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val docking = entity[DockingComponent]
        if (docking.undockCooldown > 0f) { docking.undockCooldown -= deltaTime; return }
        var nearest: Entity? = null; var minDistSq = Float.MAX_VALUE
        stationFamily.forEach { station ->
            val d2 = playerPos.dst2(station[PositionComponent].pos)
            if (d2 < minDistSq) { minDistSq = d2; nearest = station }
        }
        docking.nearestStation = nearest
        docking.proximity = if (nearest != null) Math.sqrt(minDistSq.toDouble()).toFloat() else Float.MAX_VALUE
        if (docking.proximity < 10f) { docking.isDocked = true; entity[VelocityComponent].v.set(0f, 0f) }
        else { docking.isDocked = false }
    }
}

class SectorPopulationSystem(
    private val camera: OrthographicCamera,
    private val sectorManager: SectorManager
) : IntervalSystem(interval = Fixed(5f)) {
    override fun onTick() {
        val player = world.family { all(PlayerComponent, EvolutionComponent) }.firstOrNull() ?: return
        val tier = player[EvolutionComponent].currentTier
        val enemyCount = world.family { all(EnemyComponent) }.numEntities
        val stationCount = world.family { all(StationComponent) }.numEntities
        when(sectorManager.currentSector.faction) {
            Faction.SWARM -> { if (enemyCount < 8) spawnEnemy(tier, "Swarm"); if (stationCount < 1) spawnStation(tier, "Hive") }
            Faction.VANGUARD -> { if (enemyCount < 4) spawnEnemy(tier, "Vanguard Guard"); if (stationCount < 2) spawnStation(tier, "Outpost") }
            else -> { if (enemyCount < 3) spawnEnemy(tier, "Scavenger") }
        }
    }

    private fun spawnEnemy(tier: Int, type: String) {
        val spawnRadius = MathUtils.random(341f, 512f); val angle = MathUtils.random(0f, MathUtils.PI2)
        world.entity {
            it += PositionComponent().apply { pos.set(camera.position.x + MathUtils.cos(angle) * spawnRadius, camera.position.y + MathUtils.sin(angle) * spawnRadius) }
            it += VelocityComponent().apply { acceleration = 20f + (tier * 2f); friction = 1.0f }
            it += OrientationComponent(); it += HealthComponent(current = 20f * tier, max = 20f * tier)
            it += EnemyComponent(type = type); it += WeaponComponent(cooldown = 1.5f, barrels = 1)

            // VAST ENEMY WAKE (v3.1.5): 128 segments, width = hullSize * 0.8
            val hullSize = ProceduralShipDrawer.getShipSize(tier, 1)
            it += TrailComponent(color = Color.RED.cpy(), maxLength = 32, width = hullSize * 0.4f)

            it += TacticalStatsComponent(attackDistance = 50f, maxSpeed = 40f + (tier * 5f), fireRateMult = if (tier == 1) 0.3f else 0.6f + (tier * 0.05f))
            it += WeaponModuleComponent(name = "$type Cannon", projectileTier = tier)
            it += VisibleComponent()
        }
    }

    private fun spawnStation(tier: Int, name: String) {
        val player = world.family { all(PlayerComponent) }.firstOrNull() ?: return
        val angle = MathUtils.random(0f, MathUtils.PI2); val dist = MathUtils.random(341f, 512f)
        world.entity {
            it += PositionComponent().apply { pos.set(player[PositionComponent].pos.x + MathUtils.cos(angle) * dist, player[PositionComponent].pos.y + MathUtils.sin(angle) * dist) }
            it += StationComponent(name = "$name ${MathUtils.random(100, 999)}", radius = 20f)
            it += TacticalStatsComponent(attackDistance = 0f, engagementAlpha = 0.7f)
            it += MarketComponent().apply { for (i in 0 until 3) weaponOffers.add(ModuleGenerator.generateWeapon(tier)) }
        }
    }
}
