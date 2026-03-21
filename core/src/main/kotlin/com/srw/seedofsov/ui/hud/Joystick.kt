package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.UiAssetGenerator
import com.srw.seedofsov.model.DataChannel

/**
 * A procedural joystick for navigation and tactical control.
 * Binds directly to a DataChannel for Slot communication.
 */
class Joystick(
    val game: SovereigntyGame,
    val radius: Float,
    private val channel: DataChannel? = null
) : Actor() {
    private val bgTexture: Texture
    private val knobTexture: Texture

    // Raw output in range [-1, 1]
    val output = Vector2()
    val knobPosition = Vector2()
    private var isDragging = false

    init {
        setSize(radius * 2, radius * 2)
        val assetGen = UiAssetGenerator(game.uiFactory.style)

        bgTexture = Texture(assetGen.generateJoystickBackground(radius.toInt()))
        knobTexture = Texture(assetGen.generateJoystickHandle((radius * 0.3f).toInt()))

        resetKnob()

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isDragging = true
                updateKnob(x, y)
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updateKnob(x, y)
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isDragging = false
                resetKnob()
            }
        })
    }

    private fun updateKnob(touchX: Float, touchY: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        val dx = touchX - centerX
        val dy = touchY - centerY
        val dist = Vector2.len(dx, dy)

        // Calculate raw -1 to 1 coordinates
        output.set(
            MathUtils.clamp(dx / radius, -1f, 1f),
            MathUtils.clamp(dy / radius, -1f, 1f)
        )

        // Sync to bound channel
        channel?.valueVector2?.set(output)

        // Visual knob clamping (stays within circle)
        val angle = MathUtils.atan2(dy, dx)
        val limitedDist = Math.min(dist, radius)
        knobPosition.set(
            centerX + MathUtils.cos(angle) * limitedDist,
            centerY + MathUtils.sin(angle) * limitedDist
        )
    }

    private fun resetKnob() {
        knobPosition.set(width / 2f, height / 2f)
        output.set(0f, 0f)
        channel?.valueVector2?.set(0f, 0f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(bgTexture, x, y, width, height)

        val knobSize = radius * 0.6f
        batch.draw(knobTexture, x + knobPosition.x - knobSize/2f, y + knobPosition.y - knobSize/2f, knobSize, knobSize)
    }

    fun dispose() {
        bgTexture.dispose()
        knobTexture.dispose()
    }
}
