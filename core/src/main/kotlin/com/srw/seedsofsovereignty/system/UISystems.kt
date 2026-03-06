package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class RadarRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PositionComponent, PlayerComponent, SensorComponent) }) {

    private val targetsFamily by lazy { world.family { all(PositionComponent, VisibleComponent).none(PlayerComponent, ProjectileComponent) } }
    private var timeCount = 0f
    private val tempVec = Vector2()

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
        val centerX = uiViewport.worldWidth - radarRadius - 40f
        val centerY = uiViewport.worldHeight - radarRadius - 40f

        drawer.setColor(0f, 0.15f, 0f, 0.5f)
        drawer.filledCircle(centerX, centerY, radarRadius)
        drawer.setColor(Color.GREEN)
        drawer.circle(centerX, centerY, radarRadius, 2f)

        val scanAngle = (timeCount * 2.5f) % MathUtils.PI2
        drawer.setColor(Color.LIME.r, Color.LIME.g, Color.LIME.b, 0.4f)
        drawer.line(centerX, centerY, centerX + MathUtils.cos(scanAngle) * radarRadius, centerY + MathUtils.sin(scanAngle) * radarRadius, 2f)

        targetsFamily.forEach { target ->
            val targetPos = target[PositionComponent].pos
            tempVec.set(targetPos).sub(playerPos)

            val scale = radarRadius / sensor.radius
            val rx = centerX + tempVec.x * scale
            val ry = centerY + tempVec.y * scale

            val distFromCenter = Vector2.dst(centerX, centerY, rx, ry)

            if (distFromCenter <= radarRadius) {
                when {
                    target.has(StationComponent) -> {
                        drawer.setColor(Color.CYAN); drawer.rectangle(rx - 4f, ry - 4f, 8f, 8f, 2f)
                    }
                    target.has(EnemyComponent) -> {
                        val flicker = 0.5f + 0.5f * MathUtils.sin(timeCount * 15f)
                        drawer.setColor(1f, 0f, 0f, flicker); drawer.filledCircle(rx, ry, 3f)
                    }
                    target.has(EssenceComponent) -> {
                        drawer.setColor(Color.GOLD); drawer.filledCircle(rx, ry, 2f)
                    }
                    else -> {
                        drawer.setColor(Color.WHITE); drawer.filledCircle(rx, ry, 2f)
                    }
                }
            }
        }

        val playerAngle = entity[OrientationComponent].angleRad
        drawer.setColor(Color.WHITE)
        val pSize = 6f
        drawer.line(centerX, centerY, centerX + MathUtils.cos(playerAngle) * pSize, centerY + MathUtils.sin(playerAngle) * pSize, 2f)
        drawer.filledCircle(centerX, centerY, 3f)
    }
}

class HUDSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent).any(EvolutionComponent, HealthComponent, ShieldComponent) }) {

    private val font = BitmapFont()

    override fun onTick() {
        batch.use(uiViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        if (entity.has(EvolutionComponent)) {
            val evo = entity[EvolutionComponent]
            val barWidth = 400f
            val x = (uiViewport.worldWidth - barWidth) / 2f
            val y = 30f

            drawer.setColor(Color.DARK_GRAY); drawer.filledRectangle(x, y, barWidth, 10f)
            val progress = (evo.essenceCollected.toFloat() / evo.essenceRequired.toFloat()).coerceIn(0f, 1f)
            drawer.setColor(Color.CYAN); drawer.filledRectangle(x, y, barWidth * progress, 10f)
            drawer.setColor(Color.WHITE); drawer.rectangle(x, y, barWidth, 10f, 1f)

            val tierName = when(evo.currentTier) {
                1 -> "PILOT"; 2 -> "INTERCEPTOR"; 3 -> "CARRIER"; 4 -> "SENTINEL"
                5 -> "BULWARK"; 6 -> "SENTINEL+"; 7 -> "WRAITH"; 8 -> "DREADNOUGHT"
                9 -> "VANGUARD"
                else -> "CAPITAL"
            }
            font.color = Color.CYAN
            font.draw(batch, "TIER ${evo.currentTier}: $tierName [Sub-${evo.subTier}]", x, y + 35f)

            // WEAPON INDICATOR
            if (entity.has(WeaponModuleComponent)) {
                val module = entity[WeaponModuleComponent]
                font.color = Color.WHITE
                font.draw(batch, "EQP: ${module.name} [T${module.projectileTier}]", x, y + 55f)
            }
        }

        val barWidth = 250f
        val startX = 30f
        var currentY = uiViewport.worldHeight - 40f

        if (entity.has(HealthComponent)) {
            val health = entity[HealthComponent]
            drawer.setColor(0.3f, 0f, 0f, 0.6f); drawer.filledRectangle(startX, currentY, barWidth, 14f)
            drawer.setColor(Color.RED); drawer.filledRectangle(startX, currentY, barWidth * (health.current / health.max).coerceIn(0f, 1f), 14f)
            drawer.setColor(Color.WHITE); drawer.rectangle(startX, currentY, barWidth, 14f, 1.5f)
            currentY -= 24f
        }

        if (entity.has(ShieldComponent)) {
            val shield = entity[ShieldComponent]
            drawer.setColor(0f, 0.2f, 0.3f, 0.6f); drawer.filledRectangle(startX, currentY, barWidth, 14f)
            drawer.setColor(Color.CYAN); drawer.filledRectangle(startX, currentY, barWidth * (shield.current / shield.max).coerceIn(0f, 1f), 14f)
            drawer.setColor(Color.WHITE); drawer.rectangle(startX, currentY, barWidth, 14f, 1.5f)
        }
    }

    override fun onDispose() { font.dispose() }
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
                if (aperture.openProgress >= 1f) { aperture.openProgress = 1f; aperture.state = ApertureComponent.State.OPEN }
            }
            ApertureComponent.State.CLOSING -> {
                aperture.openProgress -= speed * deltaTime
                if (aperture.openProgress <= 0f) { aperture.openProgress = 0f; aperture.state = ApertureComponent.State.CLOSED }
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
