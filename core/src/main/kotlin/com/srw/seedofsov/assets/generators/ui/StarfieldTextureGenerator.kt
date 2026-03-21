package com.srw.seedofsov.assets.generators.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ProceduralGenerator
import java.util.*

/**
 * Generates a tiled starfield texture with varying star sizes, brightness, and hues.
 * Optimized for world-scale tiling without obvious repetition.
 */
class StarfieldTextureGenerator(
    private val width: Int,
    private val height: Int,
    private val starCount: Int,
    private val seed: Long = System.currentTimeMillis()
) : ProceduralGenerator {

    override fun generate(): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        val random = Random(seed)

        pixmap.setColor(Color.CLEAR)
        pixmap.fill()

        for (i in 0 until starCount) {
            val x = random.nextInt(width)
            val y = random.nextInt(height)

            // Star size constrained to 1-2 pixel radius (2-4px diameter)
            val radius = 0.5f + random.nextFloat() * 1.5f

            val hueType = random.nextInt(100)
            val color = when {
                hueType < 15 -> Color(0.6f, 0.8f, 1f, 1f)   // Technical Blue
                hueType < 30 -> Color(1f, 0.95f, 0.7f, 1f)  // Scientific Yellow
                hueType < 40 -> Color(1f, 0.7f, 0.5f, 1f)   // Orange Giant
                hueType < 45 -> Color(1f, 0.5f, 0.5f, 1f)   // Red Dwarf
                else -> Color.WHITE
            }.cpy()

            // Luminosity variation
            val brightness = 0.4f + random.nextFloat() * 0.6f
            color.mul(brightness, brightness, brightness, 1f)

            pixmap.setColor(color)
            val r = radius.toInt()
            if (r <= 1) {
                pixmap.drawPixel(x, y)
            } else {
                pixmap.fillCircle(x, y, r)

                // Sub-pixel antialiasing/stability glow
                color.a = 0.4f
                pixmap.setColor(color)
                pixmap.drawCircle(x, y, r + 1)
            }
        }

        return pixmap
    }
}
