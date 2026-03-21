package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.model.DataChannel

/**
 * A procedural vertical slider for module data control.
 */
class Slider(
    val game: SovereigntyGame,
    private val channel: DataChannel? = null
) : Actor() {
    private val bgTexture: Texture
    private val knobTexture: Texture

    var value: Float = 0.5f // 0 to 1
        set(v) {
            field = MathUtils.clamp(v, 0f, 1f)
            channel?.valueFloat = field
        }

    init {
        setSize(60f, 300f)

        // Procedural Background (Groove)
        val bgPixmap = Pixmap(60, 300, Pixmap.Format.RGBA8888).apply {
            setColor(0.1f, 0.1f, 0.2f, 0.8f)
            fillRectangle(25, 10, 10, 280) // Central groove
            setColor(game.uiFactory.style.palette.primary)
            drawRectangle(0, 0, 60, 300)
        }
        bgTexture = Texture(bgPixmap)
        bgPixmap.dispose()

        // Procedural Knob
        val knobPixmap = Pixmap(50, 20, Pixmap.Format.RGBA8888).apply {
            setColor(game.uiFactory.style.palette.highlight)
            fill()
        }
        knobTexture = Texture(knobPixmap)
        knobPixmap.dispose()

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                updateValue(y)
                return true
            }
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updateValue(y)
            }
        })
    }

    private fun updateValue(touchY: Float) {
        // Map Y coordinate within height to 0-1 value
        value = (touchY - 10f) / (height - 20f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(bgTexture, x, y, width, height)

        // Draw knob at current value position
        val knobY = y + 10f + (value * (height - 20f)) - 10f
        batch.draw(knobTexture, x + 5f, knobY, width - 10f, 20f)
    }

    fun dispose() {
        bgTexture.dispose()
        knobTexture.dispose()
    }
}
