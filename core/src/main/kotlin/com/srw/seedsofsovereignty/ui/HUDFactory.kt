package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import space.earlygrey.shapedrawer.ShapeDrawer

object HUDFactory {
    fun createSkin(): Skin {
        val skin = Skin()

        // 1. Core Pixels
        val whitePixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE); fill()
        }
        val whiteTex = Texture(whitePixmap)
        whitePixmap.dispose()
        skin.add("white", whiteTex)

        // 2. High-Bit Procedural Assets (MUST be first)
        AssetFactory.addProceduralAssets(skin)

        // 3. Fonts
        val mainFont = BitmapFont().apply { data.setScale(1.5f) }
        val titleFont = BitmapFont().apply { data.setScale(2.2f) }
        val mediumFont = BitmapFont().apply { data.setScale(1.4f) }
        val smallFont = BitmapFont().apply { data.setScale(1.1f) }

        skin.add("default", mainFont)
        skin.add("title-font", titleFont)
        skin.add("medium-font", mediumFont)
        skin.add("small-font", smallFont)

        // 4. Panel Pixmap
        val panelPixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(0.01f, 0.01f, 0.03f, 0.96f); fill()
            setColor(0.2f, 0.5f, 0.8f, 1f); drawRectangle(0, 0, 64, 64)
            fillRectangle(0, 0, 16, 4); fillRectangle(0, 0, 4, 16)
            fillRectangle(48, 0, 16, 4); fillRectangle(60, 0, 4, 16)
            fillRectangle(0, 60, 16, 4); fillRectangle(0, 48, 4, 16)
            fillRectangle(48, 60, 16, 4); fillRectangle(60, 48, 4, 16)
        }
        skin.add("panel", NinePatchDrawable(NinePatch(Texture(panelPixmap), 20, 20, 20, 20)), Drawable::class.java)
        panelPixmap.dispose()

        // 5. Labels & Styles
        skin.add("default", Label.LabelStyle(mainFont, Color.WHITE))
        skin.add("title", Label.LabelStyle(titleFont, Color.CYAN))
        skin.add("medium", Label.LabelStyle(mediumFont, Color.LIGHT_GRAY))
        skin.add("small", Label.LabelStyle(smallFont, Color.LIGHT_GRAY))

        val bUpPix = Pixmap(32, 32, Pixmap.Format.RGBA8888).apply {
            setColor(0.1f, 0.15f, 0.25f, 0.95f); fill()
            setColor(0.4f, 0.7f, 1f, 1f); drawRectangle(0, 0, 32, 32)
        }
        skin.add("button-up", NinePatchDrawable(NinePatch(Texture(bUpPix), 4, 4, 4, 4)), Drawable::class.java)
        bUpPix.dispose()

        val bSelPix = Pixmap(32, 32, Pixmap.Format.RGBA8888).apply {
            setColor(0.1f, 0.3f, 0.5f, 1.0f); fill()
            setColor(Color.CYAN); drawRectangle(0, 0, 32, 32)
        }
        skin.add("button-selected", NinePatchDrawable(NinePatch(Texture(bSelPix), 4, 4, 4, 4)), Drawable::class.java)
        bSelPix.dispose()

        val bDownPix = Pixmap(32, 32, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE); fill()
        }
        skin.add("button-down", NinePatchDrawable(NinePatch(Texture(bDownPix), 4, 4, 4, 4)), Drawable::class.java)
        bDownPix.dispose()

        // 6. Registration Helper
        fun registerStyle(name: String, textStyle: TextButton.TextButtonStyle) {
            skin.add(name, textStyle, TextButton.TextButtonStyle::class.java)
            val base = Button.ButtonStyle().apply {
                up = textStyle.up
                down = textStyle.down
                over = textStyle.over
                checked = textStyle.checked
            }
            skin.add(name, base, Button.ButtonStyle::class.java)
        }

        registerStyle("scifi", TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("button-up")
            down = skin.getDrawable("button-down")
            over = skin.getDrawable("button-selected")
            font = mainFont
            fontColor = Color.WHITE
            overFontColor = Color.CYAN
            downFontColor = Color.BLACK
        })

        registerStyle("scifi-selected", TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("button-selected")
            down = skin.getDrawable("button-down")
            font = mainFont
            fontColor = Color.CYAN
            downFontColor = Color.BLACK
        })

        registerStyle("market-swap", TextButton.TextButtonStyle().apply {
            up = skin.newDrawable("white", Color(0.1f, 0.6f, 0.2f, 0.8f))
            down = skin.newDrawable("white", Color.WHITE)
            font = mainFont
            fontColor = Color.WHITE
            downFontColor = Color.BLACK
        })

        registerStyle("mode-toggle", TextButton.TextButtonStyle().apply {
            up = skin.newDrawable("button-up", Color(1f, 0.7f, 0f, 0.8f))
            down = skin.newDrawable("white", Color.WHITE)
            font = mainFont
            fontColor = Color.BLACK
        })

        // 7. Joysticks
        skin.add("default", Touchpad.TouchpadStyle().apply {
            background = skin.newDrawable("white", Color(0.2f, 0.2f, 0.2f, 0.5f))
            knob = skin.newDrawable("white", Color(0.8f, 0.8f, 0.8f, 0.8f)).apply { minWidth = 60f; minHeight = 60f }
        })

        skin.add("aim", Touchpad.TouchpadStyle().apply {
            background = skin.newDrawable("white", Color(0.4f, 0.2f, 0.3f, 0.3f))
            knob = skin.newDrawable("white", Color(1f, 0.2f, 0.2f, 0.6f)).apply { minWidth = 45f; minHeight = 45f }
        })

        skin.add("drone-mode", Touchpad.TouchpadStyle().apply {
            background = skin.newDrawable("white", Color(0.4f, 0.35f, 0.1f, 0.4f))
            knob = skin.newDrawable("white", Color(1f, 0.8f, 0.1f, 0.8f)).apply { minWidth = 50f; minHeight = 50f }
        })

        skin.add("default", ScrollPane.ScrollPaneStyle().apply {
            vScroll = skin.newDrawable("white", Color(0.1f, 0.3f, 0.5f, 0.3f))
            vScrollKnob = skin.newDrawable("white", Color(0.2f, 0.6f, 1f, 0.8f))
        })

        return skin
    }

    fun createMoveJoystick(skin: Skin): Touchpad = Touchpad(15f, skin, "default").apply { setSize(250f, 250f) }
    fun createAimJoystick(skin: Skin): Touchpad = SovereigntyTouchpad(15f, skin, "aim").apply { setSize(250f, 250f) }
    fun createFireButton(skin: Skin): TextButton = TextButton("FIRE", skin, "scifi").apply { setSize(250f, 250f) }
    fun createStationButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "scifi").apply { setSize(400f, 90f) }
    fun createMarketButton(text: String, style: String, skin: Skin): TextButton = TextButton(text, skin, style).apply { setSize(250f, 80f) }
    fun createDebugButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "scifi").apply { setSize(200f, 60f) }
    fun createModeToggle(skin: Skin): TextButton = TextButton("WPN", skin, "mode-toggle").apply { setSize(120f, 120f) }
    fun createLabel(text: String, skin: Skin, style: String = "default"): Label = Label(text, skin, style)
    fun createStatBar(drawer: ShapeDrawer, skin: Skin, color: Color, rtl: Boolean = false): StatBar = StatBar(drawer, skin, barColor = color, rightToLeft = rtl)
}
