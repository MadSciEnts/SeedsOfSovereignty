package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.util.ProjectileDrawer
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * High-Bit Tactical Specimen Display.
 * Optimized for scifi HUD aesthetics with deep containment chambers.
 */
class ProjectileIcon(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    var tier: Int = 1,
    var iconScale: Float = 1f,
    var projectileColor: Color = Color.YELLOW,
    var isEmpty: Boolean = false
) : Widget() {

    override fun getPrefWidth(): Float = width
    override fun getPrefHeight(): Float = height

    private fun getNormalizationScale(tier: Int): Float {
        return when (tier) {
            1 -> 1.6f; 2 -> 1.1f; 3 -> 1.0f; 4 -> 1.1f; 5 -> 1.3f
            6 -> 1.1f; 7 -> 0.7f; 8 -> 0.55f; 9 -> 0.4f; else -> 1.0f
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val drawW = if (width > 0) width else 64f
        val drawH = if (height > 0) height else 64f

        val centerX = x + drawW / 2f
        val centerY = y + drawH / 2f
        val time = System.currentTimeMillis() / 1000f

        // 1. BACKGROUND / INTERNAL FILL
        val fillAlpha = if (isEmpty) 0.2f else 0.8f
        drawer.setColor(0.01f, 0.02f, 0.05f, fillAlpha * parentAlpha)
        drawer.filledRectangle(x, y, drawW, drawH)

        if (!isEmpty) {
            // 2. ROTATING APERTURE SEAL (Animated Layer - Inside)
            drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.15f * parentAlpha)
            val rot = time * 0.5f
            val radius = drawW / 2.6f
            for (i in 0 until 4) {
                val angle = rot + (i * MathUtils.PI / 2f)
                drawer.arc(centerX, centerY, radius, angle, 0.7f, 2.5f)
            }

            // 3. PROJECTILE SPECIMEN (Top Layer - Inside)
            val finalScale = iconScale * getNormalizationScale(tier)
            ProjectileDrawer.draw(drawer, centerX, centerY, 1.57f, tier, projectileColor, finalScale)
        } else {
            // Draw a subtle "Empty" indicator (e.g., a dim X or ghost frame)
            drawer.setColor(1f, 1f, 1f, 0.05f * parentAlpha)
            drawer.line(x + 10f, y + 10f, x + drawW - 10f, y + drawH - 10f, 1f)
            drawer.line(x + 10f, y + drawH - 10f, x + drawW - 10f, y + 10f, 1f)
        }

        // 4. METADATA DECORATIONS (Inside)
        val metaAlpha = if (isEmpty) 0.05f else 0.1f
        drawer.setColor(1f, 1f, 1f, metaAlpha * parentAlpha)
        drawer.line(centerX - 15f, y + 10f, centerX + 15f, y + 10f, 1f)
        drawer.line(x + 10f, centerY - 8f, x + 10f, centerY + 8f, 1f)

        // 5. EXTERNAL BORDER FRAME
        val frameAlpha = if (isEmpty) 0.3f else 1.0f
        if (skin.has("specimen-frame", com.badlogic.gdx.scenes.scene2d.utils.Drawable::class.java)) {
            val frame = skin.getDrawable("specimen-frame")
            val offset = 2f
            val oldColor = batch.color.cpy()
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, oldColor.a * frameAlpha)
            frame.draw(batch, x - offset, y - offset, drawW + offset * 2f, drawH + offset * 2f)
            batch.setColor(oldColor)
        } else {
            drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.6f * frameAlpha * parentAlpha)
            drawer.rectangle(x, y, drawW, drawH, 2f)
        }

        // 6. DATA NODES (On corners of the frame)
        if (skin.has("data-node", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val node = skin.getRegion("data-node")
            val nS = 16f
            val nodeAlpha = if (isEmpty) 0.2f else 0.9f
            drawer.setColor(1f, 1f, 1f, nodeAlpha * parentAlpha)
            batch.draw(node, x - nS/2f, y + drawH - nS/2f, nS, nS)
            batch.draw(node, x + drawW - nS/2f, y + drawH - nS/2f, nS, nS)
            batch.draw(node, x - nS/2f, y - nS/2f, nS, nS)
            batch.draw(node, x + drawW - nS/2f, y - nS/2f, nS, nS)
        }
    }
}
