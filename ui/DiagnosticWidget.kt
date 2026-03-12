package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.SignalObserver
import com.srw.seedsofsovereignty.component.StatusSignal
import com.srw.seedsofsovereignty.component.TacticalHubComponent
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical Specification: DiagnosticWidget (v0.8.13)
 * ROLE: Diegetic "Blueprint" readout for 4-quadrant health.
 *
 * DESIGN PRINCIPLES:
 * 1. [ABSOLUTE POSITIONING] - v0.8.9: Bypasses all Table logic for direct drawing.
 * 2. [Z-INDEX PRIORITY] - Must be added last to the stage to prevent occlusion.
 *
 * FIXES:
 * 1. [VISIBILITY] - Physically ensuring drawer.update() is called before draw logic.
 * 2. [ALPHA] - Factoring parentAlpha into ring colors.
 * 3. [COMPILATION] - Fixed lowercase 'float' to 'Float'.
 */
class DiagnosticWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : Widget(), SignalObserver {

    private val shieldLevels = mutableMapOf<String, Float>()
    private val hullLevels = mutableMapOf<String, Float>()
    private var systemsIntegrity = 1.0f

    // Visual Constants (v0.8.13 - Calibrated)
    private val thickness = 8f
    private val gap = 4f
    private val baseRadius = 50f

    private val quadrantAngles = floatArrayOf(-45f, 45f, 135f, 225f)
    private val sweep = 88f

    init {
        setSize(200f, 200f)
    }

    /**
     * Updates internal state by pulling signals from the player's TacticalHub.
     * Physically reads Health/Shield layers into internal maps.
     */
    fun update(player: Entity) {
        world.run {
            if (player.has(TacticalHubComponent)) {
                val hub = player[TacticalHubComponent]
                hub.signals.values.forEach { onSignalReceived(it) }
            }
        }
    }

    override fun onSignalReceived(signal: StatusSignal) {
        val key = signal.label ?: return
        when {
            key.startsWith("SHIELD_") -> shieldLevels[key] = signal.value
            key.startsWith("HULL_") -> hullLevels[key] = signal.value
            key.startsWith("SYSTEMS_INTEGRITY") -> systemsIntegrity = signal.value / (signal.maxValue ?: 100f)
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (batch == null || !isVisible) return

        // MANDATORY: Sync drawer with current batch state
        drawer.update()

        // DRAWING COORDINATES (UI Viewport relative)
        val cx = x + width / 2f
        val cy = y + height / 2f

        // 1. Core Systems
        val coreColor = Color.YELLOW.cpy().lerp(Color.RED, 1f - systemsIntegrity)
        drawer.setColor(coreColor.r, coreColor.g, coreColor.b, coreColor.a * parentAlpha)
        drawer.circle(cx, cy, baseRadius - 10f, 3f)

        // 2. Hull
        drawRings(cx, cy, baseRadius, hullLevels, Color.WHITE, Color.FIREBRICK, "HULL", parentAlpha)

        // 3. Shields
        val maxHull = getMaxLayer(hullLevels)
        val shieldStartRadius = baseRadius + (maxHull + 1) * (thickness + gap)
        drawRings(cx, cy, shieldStartRadius, shieldLevels, Color.CYAN, Color.BLUE, "SHIELD", parentAlpha)
    }

    private fun drawRings(cx: Float, cy: Float, startR: Float, levels: Map<String, Float>, full: Color, empty: Color, prefix: String, alpha: Float) {
        val maxL = getMaxLayer(levels)
        if (maxL < 0) return

        for (l in 0..maxL) {
            val r = startR + l * (thickness + gap)
            for (q in 0..3) {
                val key = "${prefix}_Q${q}_L${l}"
                val pct = levels[key] ?: 0f
                val ringColor = empty.cpy().lerp(full, pct)
                drawer.setColor(ringColor.r, ringColor.g, ringColor.b, ringColor.a * alpha)
                drawer.arc(cx, cy, r, quadrantAngles[q] * (Math.PI.toFloat() / 180f), sweep * (Math.PI.toFloat() / 180f), thickness)
            }
        }
    }

    private fun getMaxLayer(levels: Map<String, Float>): Int {
        var max = -1
        levels.keys.forEach { key ->
            val parts = key.split("_")
            if (parts.size > 2) {
                val lStr = parts.last()
                if (lStr.startsWith("L")) {
                    val l = lStr.substring(1).toIntOrNull() ?: 0
                    if (l > max) max = l
                }
            }
        }
        return max
    }

    override fun getPrefWidth(): Float = 200f
    override fun getPrefHeight(): Float = 200f
}
