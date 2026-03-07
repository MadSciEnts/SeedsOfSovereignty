package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Optimized StatBar with bidirectional drawing and high-visibility terminal data-caps.
 */
class StatBar(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    var value: Float = 0f,
    var max: Float = 100f,
    var barColor: Color = Color.CYAN,
    var rightToLeft: Boolean = false
) : Widget() {

    // Upgraded to medium-font for Task: Increase size of numerical values
    private val font: BitmapFont = skin.getFont("medium-font")

    override fun draw(batch: Batch, parentAlpha: Float) {
        // 1. Core Bar Layout: Increased cap width for larger text
        val capWidth = 60f
        val barWidth = width - capWidth - 8f
        val barX = if (rightToLeft) x + capWidth + 8f else x
        val capX = if (rightToLeft) x else x + barWidth + 8f

        // 2. Background Track
        drawer.setColor(0.02f, 0.05f, 0.1f, 0.5f * parentAlpha)
        drawer.filledRectangle(barX, y, barWidth, height)

        // 3. Value Bar
        val ratio = (value / max).coerceIn(0f, 1f)
        drawer.setColor(barColor.r, barColor.g, barColor.b, 0.85f * parentAlpha)

        val fillWidth = barWidth * ratio
        val fillX = if (rightToLeft) barX + (barWidth - fillWidth) else barX

        if (fillWidth > 0) {
            drawer.filledRectangle(fillX, y, fillWidth, height)
            drawer.setColor(1f, 1f, 1f, 0.2f * parentAlpha)
            drawer.rectangle(fillX, y, fillWidth, height, 0.5f)
        }

        // 4. High-Contrast Data-Cap Bracket
        drawer.setColor(0f, 0f, 0f, 0.7f * parentAlpha)
        drawer.filledRectangle(capX, y, capWidth, height)

        drawer.setColor(barColor.r, barColor.g, barColor.b, 0.7f * parentAlpha)
        if (rightToLeft) {
            drawer.line(capX + capWidth, y, capX + capWidth, y + height, 3f)
        } else {
            drawer.line(capX, y, capX, y + height, 3f)
        }

        // 5. Large Numerical Value
        val text = value.toInt().toString()
        // Center text precisely in the taller bar
        val textY = y + height / 2f + font.lineHeight / 2.5f

        font.color = Color.WHITE
        font.draw(batch, text, capX, textY, capWidth, Align.center, false)

        // 6. Definition Borders
        drawer.setColor(1f, 1f, 1f, 0.4f * parentAlpha)
        drawer.rectangle(barX, y, barWidth, height, 1f)
        drawer.rectangle(capX, y, capWidth, height, 1f)
    }
}
