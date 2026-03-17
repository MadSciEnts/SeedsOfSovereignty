package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import java.util.*

object ShipTextureGenerator {

    /**
     * GENERATION STRATEGY:
     * 1. Base Hull (Silhouette) - Solid or Beveled
     * 2. Greeble Layer (Detail) - Internal mechanical lines
     * 3. Neon Layer (Emissive) - High-contrast geometric accents
     */
    fun generateLayered(seed: Long, tier: Int, baseColor: Color): List<Triple<TextureRegion, Color, Boolean>> {
        val random = Random(seed)
        val size = if (tier < 5) 32 else 64
        val layers = mutableListOf<Triple<TextureRegion, Color, Boolean>>()

        // 1. Hull Layer (Darker base)
        val hullColor = baseColor.cpy().mul(0.3f, 0.3f, 0.3f, 1f)
        layers.add(Triple(generateHull(random, tier, size, true), hullColor, false))

        // 2. Greeble Layer (Mechanical detail)
        val detailColor = baseColor.cpy().mul(0.6f, 0.6f, 0.6f, 1f)
        layers.add(Triple(generateGreebles(random, tier, size), detailColor, false))

        // 3. Neon Layer (Glow/Outline)
        layers.add(Triple(generateHull(random, tier, size, false), baseColor, true))

        return layers
    }

    private fun generateHull(random: Random, tier: Int, size: Int, solid: Boolean): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val mid = size / 2
        val margin = size / 8

        pixmap.setColor(Color.WHITE)

        // Procedural Hull Shape based on Tier
        // T1-4: Interceptors (Compact)
        // T5-8: Corvettes (Elongated)
        // T9-12: Frigates/Destroyers (Modular/Wide)

        val width = (size / 2) - margin
        val length = (size / 2) - margin

        when {
            tier <= 4 -> { // Compact/Dagger
                val points = intArrayOf(
                    mid, margin, // Nose
                    mid + width, size - margin, // Right Back
                    mid - width, size - margin  // Left Back
                )
                if (solid) pixmap.fillTriangle(points[0], points[1], points[2], points[3], points[4], points[5])
                else drawOutline(pixmap, points)
            }
            tier <= 8 -> { // Elongated/Submarine
                val x = mid - width / 2
                val y = margin
                val w = width
                val h = size - 2 * margin
                if (solid) pixmap.fillRectangle(x, y, w, h)
                else pixmap.drawRectangle(x, y, w, h)
            }
            else -> { // Modular/Carrier
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
            val x = random.nextInt(size)
            val y = random.nextInt(size)
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

    // Deprecated but kept for compatibility until factory update
    fun generate(seed: Long, tier: Int, outlineColor: Color): TextureRegion {
        return generateHull(Random(seed), tier, 32, false)
    }
}
