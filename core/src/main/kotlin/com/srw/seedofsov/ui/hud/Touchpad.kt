package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.model.DataChannel

/**
 * Interactive area that maps touch coordinates to a DataChannel.
 * Truth on Disk: Implements 15% center-snap to origin for easy Guard Mode activation.
 */
class Touchpad(
    val game: SovereigntyGame,
    val channel: DataChannel
) : Widget() {

    private val handlePos = Vector2(0.5f, 0.5f) // Normalized 0-1
    private val knobSize = 12f

    init {
        // Map channel (-1..1) back to 0..1
        handlePos.set(
            (channel.valueVector2.x + 1f) / 2f,
            (channel.valueVector2.y + 1f) / 2f
        )

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                updatePosition(x, y)
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updatePosition(x, y)
            }
        })
    }

    private fun updatePosition(x: Float, y: Float) {
        var nx = (x / width).coerceIn(0f, 1f)
        var ny = (y / height).coerceIn(0f, 1f)

        // Center Snap Logic (15% threshold)
        val cx = nx * 2f - 1f
        val cy = ny * 2f - 1f
        val dist = Math.sqrt((cx * cx + cy * cy).toDouble()).toFloat()

        if (dist < 0.15f) {
            nx = 0.5f
            ny = 0.5f
            channel.valueVector2.set(0f, 0f)
        } else {
            channel.valueVector2.set(cx, cy)
        }

        handlePos.set(nx, ny)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!game.uiFactory.isLoaded) return

        val skin = game.uiFactory.skin
        if (!skin.has("panel", com.badlogic.gdx.scenes.scene2d.utils.Drawable::class.java) ||
            !skin.has("button-up", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) return

        val color = Color(game.uiFactory.style.palette.highlight)
        color.a *= parentAlpha

        // 1. Background
        skin.getDrawable("panel").draw(batch, x, y, width, height)

        // 2. Crosshair & Handle
        batch.color = color
        val centerX = x + (handlePos.x * width)
        val centerY = y + (handlePos.y * height)
        val pixel = skin.getRegion("button-up")

        batch.draw(pixel, x, centerY - 1f, width, 2f)
        batch.draw(pixel, centerX - 1f, y, 2f, height)
        batch.draw(pixel, centerX - knobSize/2f, centerY - knobSize/2f, knobSize, knobSize)

        batch.color = Color.WHITE
    }
}
