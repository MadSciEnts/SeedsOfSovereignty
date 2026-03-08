package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad

/**
 * A customized Touchpad that supports persistent "Locking" and custom visual states.
 * When [isPersistent] is true, the knob stays at its last position when released.
 */
class SovereigntyTouchpad(
    deadzone: Float,
    skin: Skin,
    styleName: String,
    // Task Fix: Allow constructor to define initial persistence state
    var isPersistent: Boolean = false
) : Touchpad(deadzone, skin, styleName) {

    private var persistentX: Float = 0f
    private var persistentY: Float = 0f

    override fun getKnobPercentX(): Float = if (isTouched) super.getKnobPercentX() else {
        if (isPersistent) persistentX else 0f
    }

    override fun getKnobPercentY(): Float = if (isTouched) super.getKnobPercentY() else {
        if (isPersistent) persistentY else 0f
    }

    override fun act(delta: Float) {
        // Capture live percentage while touched to lock it in
        if (isTouched) {
            persistentX = super.getKnobPercentX()
            persistentY = super.getKnobPercentY()
        }
        super.act(delta)
    }

    /**
     * Programmatically sets the persistent knob position.
     */
    fun setKnobPosition(px: Float, py: Float) {
        persistentX = px
        persistentY = py
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val style = style
        val color = color
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        val x = x
        val y = y
        val width = width
        val height = height

        style.background?.draw(batch, x, y, width, height)

        val knob = style.knob
        if (knob != null) {
            // Visual override: Draw the knob at the persistent position if released
            val kpx = if (isTouched) super.getKnobPercentX() else (if (isPersistent) persistentX else 0f)
            val kpy = if (isTouched) super.getKnobPercentY() else (if (isPersistent) persistentY else 0f)

            val bg = style.background
            val bgLeft = bg?.leftWidth ?: 0f
            val bgRight = bg?.rightWidth ?: 0f
            val bgBottom = bg?.bottomHeight ?: 0f
            val bgTop = bg?.topHeight ?: 0f

            val radius = (Math.min(width - bgLeft - bgRight, height - bgBottom - bgTop)) / 2f

            val knobX = x + width / 2f + (kpx * radius) - knob.minWidth / 2f
            val knobY = y + height / 2f + (kpy * radius) - knob.minHeight / 2f

            knob.draw(batch, knobX, knobY, knob.minWidth, knob.minHeight)
        }
    }

    fun updateStyle(newStyleName: String, skin: Skin) {
        style = skin.get(newStyleName, TouchpadStyle::class.java)
    }
}
