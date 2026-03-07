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
    var projectileColor: Color = Color.YELLOW
) : Widget() {

    private fun getNormalizationScale(tier: Int): Float {
        return when (tier) {
            1 -> 1.6f; 2 -> 1.1f; 3 -> 1.0f; 4 -> 1.1f; 5 -> 1.3f
            6 -> 1.1f; 7 -> 0.7f; 8 -> 0.55f; 9 -> 0.4f; else -> 1.0f
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val centerX = x + width / 2f
        val centerY = y + height / 2f
        val time = System.currentTimeMillis() / 1000f

        // 1. ADVANCED SPECIMEN CHAMBER (Task: Complicated low-res border)
        if (skin.has("specimen-frame", com.badlogic.gdx.scenes.scene2d.utils.Drawable::class.java)) {
            val frame = skin.getDrawable("specimen-frame")
            frame.draw(batch, x, y, width, height)
        } else {
            drawer.setColor(0.1f, 0.2f, 0.3f, 0.5f * parentAlpha)
            drawer.rectangle(x, y, width, height, 1f)
        }

        // 2. DATA NODES (VERIFIED 8x8 SOURCE)
        if (skin.has("data-node", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val node = skin.getRegion("data-node")
            val nS = 14f // Increased node size for the new 64x64 frame
            drawer.setColor(1f, 1f, 1f, 0.8f * parentAlpha)
            batch.draw(node, x - nS/2f, y + height - nS/2f, nS, nS) // TL
            batch.draw(node, x + width - nS/2f, y + height - nS/2f, nS, nS) // TR
            batch.draw(node, x - nS/2f, y - nS/2f, nS, nS) // BL
            batch.draw(node, x + width - nS/2f, y - nS/2f, nS, nS) // BR
        }

        // 3. ROTATING APERTURE SEAL (Animated Layer)
        drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.2f * parentAlpha)
        val rot = time * 0.5f
        val radius = width / 2.6f
        for (i in 0 until 4) {
            val angle = rot + (i * MathUtils.PI / 2f)
            drawer.arc(centerX, centerY, radius, angle, 0.7f, 2.5f)
        }

        // 4. METADATA DECORATIONS
        drawer.setColor(1f, 1f, 1f, 0.15f * parentAlpha)
        drawer.line(centerX - 15f, y + 6f, centerX + 15f, y + 6f, 1f)
        drawer.line(x + 6f, centerY - 8f, x + 6f, centerY + 8f, 1f)

        // 5. PROJECTILE SPECIMEN (Top Layer)
        val finalScale = iconScale * getNormalizationScale(tier)
        ProjectileDrawer.draw(drawer, centerX, centerY, 1.57f, tier, projectileColor, finalScale)
    }
}
