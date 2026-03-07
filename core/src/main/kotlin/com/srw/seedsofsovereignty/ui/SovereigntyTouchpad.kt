package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad

/**
 * A customized Touchpad that supports persistent "Locking" (doesn't reset to zero on release).
 */
class SovereigntyTouchpad(deadzone: Float, skin: Skin, styleName: String) : Touchpad(deadzone, skin, styleName) {

    var isLocked: Boolean = false

    // Captured percentages for logic layer use
    var persistentX: Float = 0f
    var persistentY: Float = 0f

    init {
        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                persistentX = knobPercentX
                persistentY = knobPercentY
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)
        // Note: We avoid calling protected internal methods that might vary by backend version.
        // The visual 'lock' will be reinforced by the InputSystem and RenderSystem feedback.
    }

    fun updateStyle(newStyleName: String, skin: Skin) {
        style = skin.get(newStyleName, TouchpadStyle::class.java)
    }
}
