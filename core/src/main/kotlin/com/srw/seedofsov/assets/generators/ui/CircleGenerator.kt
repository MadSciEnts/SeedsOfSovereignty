package com.srw.seedofsov.assets.generators.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ProceduralGenerator

/**
 * Generates a circular Pixmap.
 * Used for handles, radar points, and circular UI elements.
 */
class CircleGenerator(
    private val radius: Int,
    private val color: Color,
    private val isFilled: Boolean = true,
    private val borderWidth: Int = 1
) : ProceduralGenerator {

    override fun generate(): Pixmap {
        val size = radius * 2
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)

        if (isFilled) {
            pixmap.fillCircle(radius, radius, radius - 1)
        } else {
            for (i in 0 until borderWidth) {
                pixmap.drawCircle(radius, radius, radius - 1 - i)
            }
        }

        return pixmap
    }
}
