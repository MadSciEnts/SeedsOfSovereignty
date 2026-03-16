package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
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
 */
class TacticalRenderSystem(
    private val drawer: ShapeDrawer = inject("drawer"),
    private val uiViewport: Viewport = inject("uiViewport")
) : IteratingSystem(
    family { all(PlayerComponent, ArcedDefenseComponent, PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val defense = entity[ArcedDefenseComponent]
        val core = entity[PowerCoreComponent]

        val margin = 20f
        val centerX = margin + 100f
        val centerY = uiViewport.worldHeight - margin - 100f

        // 1. Draw Power Core (Center)
        drawer.setColor(if (core.integrity > 0.5f) Color.CYAN else Color.ORANGE)
        drawer.filledCircle(centerX, centerY, 20f * core.integrity)

        // 2. Draw Hull Arcs (Inner Ring)
        drawArcs(centerX, centerY, 40f, 15f, defense.hullFore, defense.hullAft, defense.hullPort, defense.hullStbd)

        // 3. Draw Shield Arcs (Outer Ring)
        drawArcs(centerX, centerY, 65f, 10f, defense.shieldFore, defense.shieldAft, defense.shieldPort, defense.shieldStbd)
    }

    private fun drawArcs(x: Float, y: Float, radius: Float, thickness: Float, f: Float, a: Float, p: Float, s: Float) {
        val arcLen = 80f * MathUtils.degreesToRadians
        // Fore (Up)
        drawer.setColor(getHealthColor(f))
        drawer.arc(x, y, radius, 90f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Aft (Down)
        drawer.setColor(getHealthColor(a))
        drawer.arc(x, y, radius, 270f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Port (Left)
        drawer.setColor(getHealthColor(p))
        drawer.arc(x, y, radius, 180f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
        // Starboard (Right)
        drawer.setColor(getHealthColor(s))
        drawer.arc(x, y, radius, 0f * MathUtils.degreesToRadians - arcLen / 2f, arcLen, thickness)
    }

    private fun getHealthColor(hp: Float): Color {
        return when {
            hp > 75f -> Color.CYAN
            hp > 40f -> Color.YELLOW
            hp > 0f -> Color.RED
            else -> Color.DARK_GRAY
        }
    }
}

/**
 * RADAR RENDER SYSTEM: Draws the scanning radar in the top-right.
 */
class RadarRenderSystem(
    private val drawer: ShapeDrawer = inject("drawer"),
    private val uiViewport: Viewport = inject("uiViewport")
) : IteratingSystem(
    family { all(RadarComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val radar = entity[RadarComponent]

        val margin = 20f
        val radarRadius = 80f
        val centerX = uiViewport.worldWidth - margin - radarRadius
        val centerY = uiViewport.worldHeight - margin - radarRadius

        // Draw Background
        drawer.setColor(0f, 0.2f, 0f, 0.5f)
        drawer.filledCircle(centerX, centerY, radarRadius)
        drawer.setColor(0f, 0.8f, 0f, 0.3f)
        drawer.circle(centerX, centerY, radarRadius, 2f)
        drawer.circle(centerX, centerY, radarRadius * 0.5f, 1f)

        // Draw Sweep
        drawer.setColor(0f, 1f, 0f, 0.4f)
        val sweepX = centerX + MathUtils.cosDeg(radar.sweepAngle) * radarRadius
        val sweepY = centerY + MathUtils.sinDeg(radar.sweepAngle) * radarRadius
        drawer.line(centerX, centerY, sweepX, sweepY, 2f)

        // Draw Blips
        radar.blips.forEach { blip ->
            val screenX = centerX + (blip.relativePos.x / radar.range) * radarRadius
            val screenY = centerY + (blip.relativePos.y / radar.range) * radarRadius

            drawer.setColor(blip.color)
            drawer.filledCircle(screenX, screenY, 3f)
        }
    }
}
