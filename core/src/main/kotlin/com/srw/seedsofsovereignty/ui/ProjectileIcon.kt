package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.srw.seedsofsovereignty.util.ProjectileDrawer
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * High-Bit Tactical Specimen Display.
 * Optimized for transformations (Scale, Rotate) during Tier Evolution sequences.
 */
class ProjectileIcon(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    var tier: Int = 1,
    var iconScale: Float = 1f,
    var projectileColor: Color = Color.YELLOW,
    var isEmpty: Boolean = false
) : Table(skin) {

    init {
        // Task Fix: Enable transform to allow Scene2D Actions (Scale/Rotate) to work natively
        setTransform(true)
        setOrigin(Align.center)
    }

    private fun getNormalizationScale(tier: Int): Float {
        return when (tier) {
            1 -> 1.6f; 2 -> 1.1f; 3 -> 1.0f; 4 -> 1.1f; 5 -> 1.3f
            6 -> 1.1f; 7 -> 0.7f; 8 -> 0.55f; 9 -> 0.4f; else -> 1.0f
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // Table's default background handling
        super.draw(batch, parentAlpha)

        // Note: Because transform = true, the batch is already translated, rotated, and scaled.
        // We draw everything relative to (0, 0) using current width/height.
        val drawW = width
        val drawH = height
        val centerX = drawW / 2f
        val centerY = drawH / 2f
        val time = System.currentTimeMillis() / 1000f

        // 1. INTERNAL FILL
        val fillAlpha = if (isEmpty) 0.2f else 0.8f
        drawer.setColor(0.01f, 0.02f, 0.05f, fillAlpha * parentAlpha)
        drawer.filledRectangle(0f, 0f, drawW, drawH)

        if (!isEmpty) {
            // 2. ROTATING APERTURE SEAL
            drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.15f * parentAlpha)
            val rot = time * 0.5f
            val radius = drawW / 2.6f
            for (i in 0 until 4) {
                val angle = rot + (i * MathUtils.PI / 2f)
                drawer.arc(centerX, centerY, radius, angle, 0.7f, 2.5f)
            }

            // 3. PROJECTILE SPECIMEN
            val finalScale = iconScale * getNormalizationScale(tier)
            // Draw centered at the local (already transformed) centerX/Y
            ProjectileDrawer.draw(drawer, centerX, centerY, 1.57f, tier, projectileColor, finalScale)
        }

        // 4. EXTERNAL BORDER FRAME
        val frameAlpha = if (isEmpty) 0.3f else 1.0f
        if (skin.has("specimen-frame", com.badlogic.gdx.scenes.scene2d.utils.Drawable::class.java)) {
            val frame = skin.getDrawable("specimen-frame")
            val oldColor = batch.color.cpy()
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, oldColor.a * frameAlpha * parentAlpha)
            frame.draw(batch, 0f, 0f, drawW, drawH)
            batch.setColor(oldColor)
        } else {
            drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.6f * frameAlpha * parentAlpha)
            drawer.rectangle(0f, 0f, drawW, drawH, 2f)
        }

        // 5. DATA NODES
        if (skin.has("data-node", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val node = skin.getRegion("data-node")
            val nS = 16f
            val nodeAlpha = if (isEmpty) 0.2f else 0.9f
            drawer.setColor(1f, 1f, 1f, nodeAlpha * parentAlpha)
            batch.draw(node, -nS/2f, drawH - nS/2f, nS, nS)
            batch.draw(node, drawW - nS/2f, drawH - nS/2f, nS, nS)
            batch.draw(node, -nS/2f, -nS/2f, nS, nS)
            batch.draw(node, drawW - nS/2f, -nS/2f, nS, nS)
        }
    }
}
