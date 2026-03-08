package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * A modular, high-fidelity attribute bar (v0.2.0).
 * Handles its own labeling, pulsing, and technical shimmer.
 */
class HudBar(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val labelText: String,
    private val barColor: Color,
    var current: Float = 100f,
    var max: Float = 100f
) : Table(skin) {

    private val label = Label(labelText, skin, "small")
    private var pulseTime = 0f

    init {
        left()
        add(label).width(100f).padRight(10f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        pulseTime += com.badlogic.gdx.Gdx.graphics.deltaTime

        val barWidth = 250f
        val barHeight = 14f
        val renderX = x + 110f
        val renderY = y + (height - barHeight) / 2f

        val ratio = (current / max).coerceIn(0f, 1f)

        // 1. Background (Low Opacity)
        drawer.setColor(barColor.r, barColor.g, barColor.b, 0.2f * parentAlpha)
        drawer.filledRectangle(renderX, renderY, barWidth, barHeight)

        // 2. Main Fill
        drawer.setColor(barColor.r, barColor.g, barColor.b, 0.8f * parentAlpha)
        drawer.filledRectangle(renderX, renderY, barWidth * ratio, barHeight)

        // 3. Technical Shimmer (If nearly full)
        if (ratio > 0.95f) {
            val shimmer = 0.5f + MathUtils.sin(pulseTime * 5f) * 0.5f
            drawer.setColor(1f, 1f, 1f, 0.2f * shimmer * parentAlpha)
            drawer.filledRectangle(renderX, renderY, barWidth, barHeight)
        }

        // 4. Border Frame
        drawer.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.6f * parentAlpha)
        drawer.rectangle(renderX, renderY, barWidth, barHeight, 1.5f)
    }
}
