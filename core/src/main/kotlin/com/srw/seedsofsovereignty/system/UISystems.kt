package com.srw.seedsofsovereignty.system

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
) : IteratingSystem(family { all(PlayerComponent).any(EvolutionComponent, HealthComponent, ShieldComponent) }) {

    private val font = BitmapFont() // Default font for tier label

    override fun onTick() {
        batch.use(uiViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        // 1. Essence Progress Bar (Bottom Center)
        if (entity.has(EvolutionComponent)) {
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

            // TIER LABEL DISPLAY
            val tierName = when(evo.currentTier) {
                1 -> "PILOT"
                2 -> "INTERCEPTOR"
                3 -> "CARRIER"
                4 -> "SENTINEL"
                5 -> "BULWARK"
                else -> "UNKNOWN"
            }
            font.color = Color.CYAN
            font.draw(batch, "TIER ${evo.currentTier}: $tierName [Sub-${evo.subTier}]", x, y + 35f)
        }

        val barWidth = 250f
        val barHeight = 14f
        val startX = 30f
        var currentY = uiViewport.worldHeight - 40f

        if (entity.has(HealthComponent)) {
            val health = entity[HealthComponent]
            val hProgress = (health.current / health.max).coerceIn(0f, 1f)
            drawer.setColor(0.3f, 0f, 0f, 0.6f)
            drawer.filledRectangle(startX, currentY, barWidth, barHeight)
            drawer.setColor(Color.RED)
            drawer.filledRectangle(startX, currentY, barWidth * hProgress, barHeight)
            drawer.setColor(Color.WHITE)
            drawer.rectangle(startX, currentY, barWidth, barHeight, 1.5f)
            currentY -= (barHeight + 10f)
        }

        if (entity.has(ShieldComponent)) {
            val shield = entity[ShieldComponent]
            val sProgress = (shield.current / shield.max).coerceIn(0f, 1f)
            drawer.setColor(0f, 0.2f, 0.3f, 0.6f)
            drawer.filledRectangle(startX, currentY, barWidth, barHeight)
            drawer.setColor(Color.CYAN)
            drawer.filledRectangle(startX, currentY, barWidth * sProgress, barHeight)
            drawer.setColor(Color.WHITE)
            drawer.rectangle(startX, currentY, barWidth, barHeight, 1.5f)
        }
    }

    override fun onDispose() {
        font.dispose()
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
