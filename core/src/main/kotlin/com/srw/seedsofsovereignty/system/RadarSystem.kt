package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
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
        batch.use(uiViewport.camera.combined) {
            drawer.update(); super.onTick(); drawer.setColor(Color.WHITE)
        }
    }
    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val sensor = entity[SensorComponent]
        val centerX = uiViewport.worldWidth - 160f; val centerY = uiViewport.worldHeight - 160f
        drawer.setColor(0f, 0.1f, 0.15f, 0.6f); drawer.filledCircle(centerX, centerY, 120f)
        drawer.setColor(0f, 0.8f, 1f, 0.4f); drawer.circle(centerX, centerY, 120f, 2f)
        drawer.line(centerX - 120f, centerY, centerX + 120f, centerY, 1f)
        drawer.line(centerX, centerY - 120f, centerX, centerY + 120f, 1f)
        val sweepAngle = (timeCount * 140f) % 360f
        drawer.setColor(0f, 1f, 0.6f, 0.3f)
        val rad = sweepAngle * MathUtils.degRad
        drawer.line(centerX, centerY, centerX + MathUtils.cos(rad) * 120f, centerY + MathUtils.sin(rad) * 120f, 2f)
        targetsFamily.forEach { target ->
            val targetPos = target[PositionComponent].pos
            // GSF 0.1: No change needed here if sensor.radius is correctly scaled to 100f
            val rx = centerX + (targetPos.x - playerPos.x) * (120f / sensor.radius)
            val ry = centerY + (targetPos.y - playerPos.y) * (120f / sensor.radius)
            if (Vector2.dst(centerX, centerY, rx, ry) <= 120f) {
                drawer.setColor(if (target.has(EnemyComponent)) Color.RED else Color.CYAN); drawer.filledCircle(rx, ry, 3f)
            }
        }
    }
}
