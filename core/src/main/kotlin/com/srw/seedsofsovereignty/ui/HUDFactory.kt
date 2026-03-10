package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

object HUDFactory {
    fun createSkin(styleId: String = "INDUSTRIAL"): Skin {
        val skin = Skin()

        // 1. Core Pixels
        val whitePixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE); fill()
        }
        val whiteTex = Texture(whitePixmap)
        whitePixmap.dispose()
        skin.add("white", whiteTex)

        // 2. High-Bit Procedural Assets
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

        // 5. Labels & Styles
        skin.add("default", Label.LabelStyle(mainFont, Color.WHITE))
        skin.add("title", Label.LabelStyle(titleFont, Color.CYAN))
        skin.add("medium", Label.LabelStyle(mediumFont, Color.LIGHT_GRAY))
        skin.add("small", Label.LabelStyle(smallFont, Color.LIGHT_GRAY))

        skin.add("default", ScrollPane.ScrollPaneStyle().apply {
            vScroll = skin.newDrawable("white", Color(0.1f, 0.3f, 0.5f, 0.3f))
            vScrollKnob = skin.newDrawable("white", Color(0.2f, 0.6f, 1f, 0.8f))
        })

        // TVM Phase 7.2: Apply initial style
        applyStyleToSkin(styleId, skin)

        return skin
    }

    private fun applyStyleToSkin(styleId: String, skin: Skin) {
        val collection = HUDTextureGenerator.generate(styleId, skin)
        val mainFont = skin.getFont("default")

        // Phase 7.2: Dynamic Panel Skinning
        skin.add("panel", collection.panel, Drawable::class.java)

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
            up = collection.buttonUp
            down = collection.buttonDown
            over = collection.buttonUp
            font = mainFont
            fontColor = if (styleId == "IMPERIAL") Color.GOLD else Color.WHITE
            overFontColor = Color.CYAN
            downFontColor = Color.BLACK
        })

        registerStyle("mode-toggle", TextButton.TextButtonStyle().apply {
            up = collection.toggleOff
            checked = collection.toggleOn
            over = collection.toggleOff
            down = collection.toggleOn
            font = mainFont
            fontColor = Color.BLACK
        })

        // Phase 8.1: Hangar Styles
        registerStyle("scifi-selected", TextButton.TextButtonStyle().apply {
            up = collection.buttonDown
            down = collection.buttonDown
            over = collection.buttonDown
            font = mainFont
            fontColor = Color.CYAN
        })

        registerStyle("market-swap", TextButton.TextButtonStyle().apply {
            up = collection.buttonUp
            down = collection.buttonDown
            over = collection.buttonUp
            font = mainFont
            fontColor = if (styleId == "IMPERIAL") Color.GOLD else Color.ORANGE
        })

        // 7. Joysticks
        skin.add("default", Touchpad.TouchpadStyle().apply {
            background = collection.joystickBg
            knob = collection.joystickKnob
        })

        skin.add("aim", Touchpad.TouchpadStyle().apply {
            background = collection.joystickBg
            knob = collection.joystickKnob
        })

        skin.add("drone-mode", Touchpad.TouchpadStyle().apply {
            background = collection.joystickBg
            knob = collection.joystickKnob
        })

        // Phase 7.2: Add generic keys for dynamic swapping
        skin.add("current_slider_bg", collection.sliderBg, Drawable::class.java)
        skin.add("current_slider_knob", collection.sliderKnob, Drawable::class.java)
        skin.add("current_status_light", collection.statusLightYellow, Drawable::class.java)
    }

    fun swapSkin(styleId: String, skin: Skin) {
        applyStyleToSkin(styleId, skin)
    }

    /**
     * Phase 7.2: Force UI update for all relevant actors in stage.
     */
    fun refreshUI(stage: Stage, skin: Skin) {
        stage.actors.forEach { refreshActor(it, skin) }
    }

    private fun refreshActor(actor: Actor, skin: Skin) {
        when (actor) {
            is TextButton -> {
                // Determine style name. Most use "scifi", but some might use "mode-toggle"
                val styleName = when (actor.style) {
                    skin.get("mode-toggle", TextButton.TextButtonStyle::class.java) -> "mode-toggle"
                    skin.get("scifi-selected", TextButton.TextButtonStyle::class.java) -> "scifi-selected"
                    skin.get("market-swap", TextButton.TextButtonStyle::class.java) -> "market-swap"
                    else -> "scifi"
                }
                actor.style = skin.get(styleName, TextButton.TextButtonStyle::class.java)
            }
            is Touchpad -> {
                // Standard or Aim?
                actor.style = skin.get("default", Touchpad.TouchpadStyle::class.java)
            }
            is Container<*> -> {
                actor.background = skin.getDrawable("panel")
                actor.actor?.let { refreshActor(it, skin) }
            }
            is Table -> {
                if (actor is StationHangarUI || actor is MarketTable || actor is EvolutionReportPopup) {
                    actor.background = skin.getDrawable("panel")
                }
                actor.children.forEach { refreshActor(it, skin) }
            }
            is Group -> {
                actor.children.forEach { refreshActor(it, skin) }
            }
        }
    }
}
