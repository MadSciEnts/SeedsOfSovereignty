package com.srw.seedofsov.assets.generators.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ProceduralGenerator
import kotlin.math.*

/**
 * Generates curved "Nautilus" arc textures for health, shield, and hull displays.
 */
class NautilusArcGenerator(
    private val radius: Int,
    private val thickness: Int,
    private val startAngle: Float,
    private val arcDegrees: Float,
    private val color: Color
) : ProceduralGenerator {

    override fun generate(): Pixmap {
        val size = radius * 2
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)

        // Draw the arc using a simple point-in-arc check
        val center = radius.toFloat()
        val innerRadiusSq = (radius - thickness).toDouble().pow(2.0)
        val outerRadiusSq = radius.toDouble().pow(2.0)

        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val distSq = (dx * dx + dy * dy).toDouble()

                if (distSq in innerRadiusSq..outerRadiusSq) {
                    // Check angle (libGDX angles are clockwise from positive X)
                    var angle = atan2(-dy, dx).toDouble() // Use -dy because Y is down
                    var degrees = Math.toDegrees(angle).toFloat()
                    if (degrees < 0) degrees += 360f

                    // Basic arc check
                    if (isAngleInArc(degrees)) {
                        pixmap.drawPixel(x, y)
                    }
                }
            }
        }
        return pixmap
    }

    private fun isAngleInArc(angle: Float): Boolean {
        var normalizedAngle = (angle - startAngle) % 360f
        if (normalizedAngle < 0) normalizedAngle += 360f
        return normalizedAngle <= arcDegrees
    }
}
