package com.srw.seedofsov.assets.generators.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ProceduralGenerator

/**
 * Enhanced generator for NinePatch assets.
 * Supports rectangular borders, chamfered corners, and optional filling.
 */
class NinePatchGenerator(
    private val width: Int,
    private val height: Int,
    private val borderWidth: Int,
    private val borderColor: Color,
    private val fillColor: Color = Color.CLEAR,
    private val chamferSize: Int = 0
) : ProceduralGenerator {

    override fun generate(): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)

        // Fill background (considering chamfer)
        if (fillColor != Color.CLEAR) {
            pixmap.setColor(fillColor)
            if (chamferSize <= 0) {
                pixmap.fill()
            } else {
                // Manually fill to respect chamfers
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        if (isInside(x, y)) {
                            pixmap.drawPixel(x, y)
                        }
                    }
                }
            }
        }

        // Draw border
        pixmap.setColor(borderColor)
        if (chamferSize <= 0) {
            for (i in 0 until borderWidth) {
                pixmap.drawRectangle(i, i, width - 2 * i, height - 2 * i)
            }
        } else {
            for (i in 0 until borderWidth) {
                drawChamferedBorder(pixmap, i)
            }
        }

        return pixmap
    }

    private fun isInside(x: Int, y: Int): Boolean {
        if (chamferSize <= 0) return true
        // Check 4 corners
        if (x + y < chamferSize) return false // Top-left
        if ((width - 1 - x) + y < chamferSize) return false // Top-right
        if (x + (height - 1 - y) < chamferSize) return false // Bottom-left
        if ((width - 1 - x) + (height - 1 - y) < chamferSize) return false // Bottom-right
        return true
    }

    private fun drawChamferedBorder(pixmap: Pixmap, offset: Int) {
        val w = width - 1 - offset
        val h = height - 1 - offset
        val s = chamferSize

        // Lines between chamfers
        pixmap.drawLine(offset + s, offset, w - s, offset) // Top
        pixmap.drawLine(offset + s, h, w - s, h) // Bottom
        pixmap.drawLine(offset, offset + s, offset, h - s) // Left
        pixmap.drawLine(w, offset + s, w, h - s) // Right

        // Chamfer diagonals
        pixmap.drawLine(offset, offset + s, offset + s, offset) // TL
        pixmap.drawLine(w - s, offset, w, offset + s) // TR
        pixmap.drawLine(offset, h - s, offset + s, h) // BL
        pixmap.drawLine(w - s, h, w, h - s) // BR
    }
}
