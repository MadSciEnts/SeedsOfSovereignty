package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.util.*

/**
 * SHIP TEXTURE GENERATOR: Procedural layering for tiered ship visuals.
 * Tiers 1-4: Interceptors/Daggers
 * Tiers 5-8: Corvettes/Submarines
 * Tiers 9-12: Frigates/Carriers (Modular)
 */
object ShipTextureGenerator {

    data class VisualLayerData(
        val region: TextureRegion,
        val color: Color,
        val pulse: Boolean,
        val zIndex: Int
    )

    fun generateLayered(seed: Long, tier: Int, baseColor: Color): List<VisualLayerData> {
        val random = Random(seed + tier)
        val size = if (tier < 9) 32 else 64
        val layers = mutableListOf<VisualLayerData>()

        // 1. Hull Layer (Bottom) - Darker base
        val hullColor = baseColor.cpy().mul(0.2f, 0.2f, 0.2f, 1f)
        layers.add(VisualLayerData(generateHull(random, tier, size, true), hullColor, false, 0))

        // 2. Greeble Layer (Middle) - Mechanical detail
        val detailColor = baseColor.cpy().mul(0.5f, 0.5f, 0.5f, 1f)
        layers.add(VisualLayerData(generateGreebles(random, tier, size), detailColor, false, 1))

        // 3. Neon Layer (Top) - Glow/Outline
        layers.add(VisualLayerData(generateHull(random, tier, size, false), baseColor, true, 2))

        return layers
    }

    /**
     * Generates a 16x16 or 32x32 texture card for a module.
     */
    fun generateModuleCard(seed: Long, color: Color): TextureRegion {
        val size = 16
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.drawRectangle(2, 2, size - 4, size - 4)
        pixmap.drawRectangle(4, 4, size - 8, size - 8)

        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateHull(random: Random, tier: Int, size: Int, solid: Boolean): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val mid = size / 2
        val margin = size / 8
        pixmap.setColor(Color.WHITE)

        val width = (size / 2) - margin
        val length = (size / 2) - margin

        when {
            tier <= 4 -> { // INTERCEPTOR (Triangle/Arrow)
                val points = intArrayOf(
                    mid, margin, // Nose
                    mid + width, size - margin, // Right Back
                    mid - width, size - margin  // Left Back
                )
                if (solid) pixmap.fillTriangle(points[0], points[1], points[2], points[3], points[4], points[5])
                else drawOutline(pixmap, points)
            }
            tier <= 8 -> { // CORVETTE (Elongated/Submarine)
                val x = mid - width / 3
                val y = margin
                val w = (width * 0.66f).toInt()
                val h = size - 2 * margin
                if (solid) pixmap.fillRectangle(x, y, w, h)
                else pixmap.drawRectangle(x, y, w, h)

                // Add Small Wings
                if (solid) {
                    pixmap.fillRectangle(x - margin, y + h/2, w + 2*margin, margin)
                } else {
                    pixmap.drawRectangle(x - margin, y + h/2, w + 2*margin, margin)
                }
            }
            else -> { // CAPITAL SHIP (Modular/Carrier)
                val x = margin
                val y = mid - length / 4
                val w = size - 2 * margin
                val h = length / 2
                if (solid) {
                    pixmap.fillRectangle(x, y, w, h) // Main beam
                    pixmap.fillRectangle(mid - width/2, margin, width, size - 2*margin) // Cross
                } else {
                    pixmap.drawRectangle(x, y, w, h)
                    pixmap.drawRectangle(mid - width/2, margin, width, size - 2*margin)
                }
            }
        }

        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateGreebles(random: Random, tier: Int, size: Int): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)

        val density = 5 + tier
        for (i in 0 until density) {
            val x = random.nextInt(size - 4) + 2
            val y = random.nextInt(size - 4) + 2
            val w = random.nextInt(size / 4) + 2
            val h = random.nextInt(2) + 1
            pixmap.drawRectangle(x, y, w, h)
        }

        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun drawOutline(pixmap: Pixmap, points: IntArray) {
        for (i in 0 until points.size step 2) {
            val next = (i + 2) % points.size
            pixmap.drawLine(points[i], points[i+1], points[next], points[next+1])
        }
    }

    // LEGACY COMPATIBILITY
    fun generate(seed: Long, tier: Int, outlineColor: Color): TextureRegion {
        return generateHull(Random(seed), tier, 32, false)
    }
}
