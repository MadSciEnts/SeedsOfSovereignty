package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import java.lang.reflect.Method

/**
 * A customized Touchpad that supports persistent "Locking" and custom visual states.
 * When [isPersistent] is true, the knob stays at its last position when released.
 * v0.6.5: Behavior Lockdown. Uses reflection to force internal engine sync during restoration.
 */
class SovereigntyTouchpad(
    deadzone: Float,
    skin: Skin,
    styleName: String,
    var isPersistent: Boolean = false
) : Touchpad(deadzone, skin, styleName) {

    private var persistentX: Float = 0f
    private var persistentY: Float = 0f

    // Reflection bridge to access package-private calculation method in base Touchpad
    private val calculateMethod: Method? by lazy {
        try {
            Touchpad::class.java.getDeclaredMethod(
                "calculatePositionAndValue",
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            ).apply { isAccessible = true }
        } catch (e: Exception) { null }
    }

    /**
     * Setting this value triggers a one-time restoration in the next act() pass.
     */
    var knobRestore: Vector2? = null

    override fun getKnobPercentX(): Float = if (isTouched) super.getKnobPercentX() else {
        if (isPersistent) persistentX else 0f
    }

    override fun getKnobPercentY(): Float = if (isTouched) super.getKnobPercentY() else {
        if (isPersistent) persistentY else 0f
    }

    override fun act(delta: Float) {
        // 1. Force engine-level restoration to move physical handle and update internal percentages
        knobRestore?.let {
            val bg = style.background
            val bgLeft = bg?.leftWidth ?: 0f
            val bgRight = bg?.rightWidth ?: 0f
            val bgBottom = bg?.bottomHeight ?: 0f
            val bgTop = bg?.topHeight ?: 0f
            val radius = (Math.min(width - bgLeft - bgRight, height - bgBottom - bgTop)) / 2f

            try {
                calculateMethod?.invoke(
                    this,
                    width / 2f + it.x * radius,
                    height / 2f + it.y * radius,
                    true
                )
                persistentX = it.x
                persistentY = it.y
            } catch (e: Exception) {}
            knobRestore = null
        }

        // 2. Capture memory while touched, but only for meaningful movements
        if (isTouched) {
            val lx = super.getKnobPercentX()
            val ly = super.getKnobPercentY()
            if (Math.abs(lx) > 0.01f || Math.abs(ly) > 0.01f) {
                persistentX = lx
                persistentY = ly
            }
        }
        super.act(delta)
    }

    fun setKnobPosition(px: Float, py: Float) {
        persistentX = px
        persistentY = py
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val style = style
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        style.background?.draw(batch, x, y, width, height)

        val knob = style.knob
        if (knob != null) {
            // Visual override to ensure the handle matches our persistent state when released
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
}
