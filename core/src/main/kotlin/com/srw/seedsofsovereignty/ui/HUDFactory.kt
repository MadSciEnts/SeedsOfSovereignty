package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import ktx.style.*

object HUDFactory {
    fun createSkin(): Skin {
        return skin {
            val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.WHITE)
                fill()
            }
            val whiteTex = Texture(pixmap)
            pixmap.dispose()

            this["white"] = whiteTex
            this["default"] = BitmapFont()

            touchpad("default") {
                background = this@skin.newDrawable("white", Color(0.2f, 0.2f, 0.2f, 0.5f))
                knob = this@skin.newDrawable("white", Color(0.8f, 0.8f, 0.8f, 0.8f)).apply {
                    minWidth = 40f
                    minHeight = 40f
                }
            }

            touchpad("aim") {
                background = this@skin.newDrawable("white", Color(0.4f, 0.2f, 0.2f, 0.3f))
                knob = this@skin.newDrawable("white", Color(1f, 0.2f, 0.2f, 0.6f)).apply {
                    minWidth = 30f
                    minHeight = 30f
                }
            }

            textButton("default") {
                up = this@skin.newDrawable("white", Color(0.4f, 0.2f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.8f, 0.2f, 0.2f, 0.8f))
                font = this@skin.getFont("default")
            }

            textButton("station") {
                up = this@skin.newDrawable("white", Color(0.1f, 0.4f, 0.4f, 0.7f))
                down = this@skin.newDrawable("white", Color(0.2f, 0.8f, 0.8f, 0.9f))
                font = this@skin.getFont("default")
            }

            textButton("debug") {
                up = this@skin.newDrawable("white", Color(0.2f, 0.4f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.2f, 0.8f, 0.2f, 0.8f))
                font = this@skin.getFont("default")
            }
        }
    }

    fun createMoveJoystick(skin: Skin): Touchpad = Touchpad(10f, skin, "default").apply { setSize(200f, 200f) }
    fun createAimJoystick(skin: Skin): Touchpad = Touchpad(10f, skin, "aim").apply { setSize(200f, 200f) }
    fun createFireButton(skin: Skin): TextButton = TextButton("FIRE", skin).apply { setSize(200f, 200f) }
    fun createStationButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "station").apply { setSize(300f, 80f) }
    fun createDebugButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "debug").apply { setSize(150f, 50f) }
}
