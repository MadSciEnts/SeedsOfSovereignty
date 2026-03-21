package com.srw.seedofsov.assets.generators.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ProceduralGenerator

/**
 * Generates specialized radar textures.
 */
class RadarGenerator(
    private val radius: Int,
    private val color: Color
) : ProceduralGenerator {

    fun generateBackground(): Pixmap {
        val size = radius * 2
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, 0.3f)
        pixmap.fillCircle(radius, radius, radius - 1)
        pixmap.setColor(color)
        pixmap.drawCircle(radius, radius, radius - 1)
        // Add crosshair lines
        pixmap.drawLine(0, radius, size, radius)
        pixmap.drawLine(radius, 0, radius, size)
        return pixmap
    }

    fun generateSweep(angle: Float): Pixmap {
        // This is a simple placeholder. Real sweep would involve drawing a gradient arc.
        val size = radius * 2
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        // Draw a line representing the sweep arm for now
        val endX = (radius + (radius - 1) * Math.cos(Math.toRadians(angle.toDouble()))).toInt()
        val endY = (radius + (radius - 1) * Math.sin(Math.toRadians(angle.toDouble()))).toInt()
        pixmap.drawLine(radius, radius, endX, endY)
        return pixmap
    }

    override fun generate(): Pixmap = generateBackground()
}
