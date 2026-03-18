package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * TACTICAL RENDER SYSTEM: Draws the Nautilus arcs in the top-left.
 * Visualizes Power Core + Quad Hull + Quad Shield.
 */
class TacticalRenderSystem(
    private val batch: Batch = inject("batch"),
    private val drawer: ShapeDrawer = inject("drawer"),
    private val uiViewport: Viewport = inject("uiViewport")
) : IteratingSystem(
    family { all(PlayerComponent, ArcedDefenseComponent, PowerCoreComponent) }
) {
    override fun onTick() {
        batch.projectionMatrix = uiViewport.camera.combined
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val defense = entity[ArcedDefenseComponent]
        val core = entity[PowerCoreComponent]

        val margin = 30f
        val centerX = margin + 150f
        val centerY = uiViewport.worldHeight - margin - 150f

        // 1. Draw Inner Core (Yellow)
        drawer.setColor(Color.YELLOW)
        drawer.filledCircle(centerX, centerY, 30f * core.integrity)

        // 2. Draw Hull Arcs (Inner Ring - White/Grey)
        drawArcs(centerX, centerY, 60f, 20f, defense.hullFore, defense.hullAft, defense.hullPort, defense.hullStbd, Color.WHITE)

        // 3. Draw Shield Arcs (Outer Ring - Blue)
        drawArcs(centerX, centerY, 95f, 15f, defense.shieldFore, defense.shieldAft, defense.shieldPort, defense.shieldStbd, Color.BLUE)
    }

    private fun drawArcs(x: Float, y: Float, radius: Float, thickness: Float, f: Float, a: Float, p: Float, s: Float, baseColor: Color) {
        val arcLen = 80f * MathUtils.degreesToRadians
        drawer.setColor(baseColor)

        // Fore
        drawer.setColor(getHealthColor(f, baseColor))
        drawer.arc(x, y, radius, 90f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Aft
        drawer.setColor(getHealthColor(a, baseColor))
        drawer.arc(x, y, radius, 270f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Port
        drawer.setColor(getHealthColor(p, baseColor))
        drawer.arc(x, y, radius, 180f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Starboard
        drawer.setColor(getHealthColor(s, baseColor))
        drawer.arc(x, y, radius, 0f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
    }

    private fun getHealthColor(hp: Float, base: Color): Color {
        return when {
            hp > 40f -> base
            hp > 0f -> Color.RED
            else -> Color.DARK_GRAY
        }
    }
}

/**
 * RADAR RENDER SYSTEM: Draws the scanning radar in the top-right.
 * Size: 150f radius. Transparency: 0.65f.
 */
class RadarRenderSystem(
    private val batch: Batch = inject("batch"),
    private val drawer: ShapeDrawer = inject("drawer"),
    private val uiViewport: Viewport = inject("uiViewport")
) : IteratingSystem(
    family { all(RadarComponent) }
) {
    override fun onTick() {
        batch.projectionMatrix = uiViewport.camera.combined
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val radar = entity[RadarComponent]
        radar.range = 400f

        val margin = 30f
        val radarRadius = 150f // 3/4 Size of previous 200f
        val centerX = uiViewport.worldWidth - margin - radarRadius
        val centerY = uiViewport.worldHeight - margin - radarRadius
        val baseAlpha = 0.65f

        // Draw Background (Semi-Transparent)
        drawer.setColor(0f, 0.1f, 0.2f, baseAlpha)
        drawer.filledCircle(centerX, centerY, radarRadius)

        // Rings
        drawer.setColor(0f, 0.5f, 1f, baseAlpha * 0.5f)
        drawer.circle(centerX, centerY, radarRadius, 2f)
        drawer.circle(centerX, centerY, radarRadius * 0.5f, 1f)

        // Draw Sweep
        drawer.setColor(0f, 1f, 1f, baseAlpha)
        val sweepX = centerX + MathUtils.cosDeg(radar.sweepAngle) * radarRadius
        val sweepY = centerY + MathUtils.sinDeg(radar.sweepAngle) * radarRadius
        drawer.line(centerX, centerY, sweepX, sweepY, 2f)

        // Draw Blips
        radar.blips.forEach { blip ->
            val screenX = centerX + (blip.relativePos.x / radar.range) * radarRadius
            val screenY = centerY + (blip.relativePos.y / radar.range) * radarRadius

            // Ensure blip alpha matches radar transparency
            val blipColor = blip.color.cpy().apply { a = baseAlpha }
            drawer.setColor(blipColor)
            drawer.filledCircle(screenX, screenY, 5f)
        }
    }
}
