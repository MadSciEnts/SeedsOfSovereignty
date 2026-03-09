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
import com.srw.seedsofsovereignty.component.InputChannel
import com.srw.seedsofsovereignty.component.ControlClassification
import com.srw.seedsofsovereignty.component.TacticalHubComponent
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.PlayerComponent
import space.earlygrey.shapedrawer.ShapeDrawer

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
                val styleName = if (actor.style == skin.get("mode-toggle", TextButton.TextButtonStyle::class.java)) "mode-toggle" else "scifi"
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
                actor.children.forEach { refreshActor(it, skin) }
            }
            is Group -> {
                actor.children.forEach { refreshActor(it, skin) }
            }
        }
    }

    /**
     * TVM Phase 6.3: Factory Visual Refactor.
     * v0.4.0: Updated to use procedural skins and signal hooks.
     * v0.4.1: Signal-Aware Actors and Footprint Calibration.
     * Phase 7.2: Updated to use dynamic skin keys.
     */
    fun createTacticalInput(skin: Skin, channel: InputChannel, world: World): Actor {
        val baseActor: Actor = when (channel.classification) {
            ControlClassification.ACTION_BUTTON -> {
                object : TextButton(channel.label ?: channel.actionKey, skin, "scifi") {
                    override fun act(delta: Float) {
                        super.act(delta)
                        if (channel.signalKey != null) {
                            world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()?.let { player ->
                                val signal = with(world) { player[TacticalHubComponent].signals[channel.signalKey] }
                                if (signal != null) {
                                    setText("${channel.label ?: channel.actionKey}: ${signal.value.toInt()}")
                                }
                            }
                        }
                    }
                }.apply {
                    name = channel.actionKey
                }
            }
            ControlClassification.TOGGLE_SWITCH -> {
                object : TextButton(channel.label ?: channel.actionKey, skin, "mode-toggle") {
                    override fun act(delta: Float) {
                        super.act(delta)
                        if (channel.signalKey != null) {
                            world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()?.let { player ->
                                val signal = with(world) { player[TacticalHubComponent].signals[channel.signalKey] }
                                if (signal != null) {
                                    setText("${channel.label ?: channel.actionKey}: ${signal.value.toInt()}")
                                }
                            }
                        }
                    }
                }.apply {
                    name = channel.actionKey
                }
            }
            ControlClassification.MOVEMENT_STICK -> {
                createAttackJoystick(skin).apply {
                    name = channel.actionKey
                }
            }
            ControlClassification.TACTICAL_STICK -> {
                SovereigntyTouchpad(15f, skin, "default").apply {
                    name = channel.actionKey
                    isPersistent = true
                }
            }
            ControlClassification.DIAMOND_GRID -> {
                Table(skin).apply {
                    val btnSize = 80f
                    add().size(btnSize); add(Button(skin, "scifi")).size(btnSize); add().size(btnSize).row()
                    add(Button(skin, "scifi")).size(btnSize); add().size(btnSize); add(Button(skin, "scifi")).size(btnSize).row()
                    add().size(btnSize); add(Button(skin, "scifi")).size(btnSize); add().size(btnSize)
                    name = channel.actionKey
                }
            }
            ControlClassification.MODIFIER_SLIDER -> {
                Table(skin).apply {
                    val slider = Slider(0f, 1f, 0.1f, false, skin).apply {
                        name = channel.actionKey
                        style = Slider.SliderStyle(style).apply {
                            background = skin.getDrawable("current_slider_bg")
                            knob = skin.getDrawable("current_slider_knob")
                        }
                    }
                    add(slider).fillX().expandX()
                    name = channel.actionKey
                }
            }
            ControlClassification.STATUS_LIGHT -> {
                Image(skin.getDrawable("current_status_light")).apply {
                    name = channel.actionKey
                }
            }
            ControlClassification.DATA_READOUT -> {
                object : Label(channel.label ?: channel.actionKey, skin, "small") {
                    override fun act(delta: Float) {
                        super.act(delta)
                        if (channel.signalKey != null) {
                            world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()?.let { player ->
                                val signal = with(world) { player[TacticalHubComponent].signals[channel.signalKey] }
                                if (signal != null) {
                                    setText("${channel.label ?: channel.actionKey}: ${signal.value.toInt()}")
                                }
                            }
                        }
                    }
                }.apply {
                    name = channel.actionKey
                }
            }
        }

        // Wrap in technical frame "The Cradle"
        return Container(baseActor).apply {
            background = skin.getDrawable("panel")
            pad(5f)
            fill()
            // Step 2 calibration
            when (channel.classification) {
                ControlClassification.ACTION_BUTTON, ControlClassification.TOGGLE_SWITCH -> {
                    size(100f, 100f)
                    prefSize(100f, 100f)
                }
                ControlClassification.TACTICAL_STICK -> {
                    size(250f, 250f)
                    prefSize(250f, 250f)
                }
                ControlClassification.DATA_READOUT -> {
                    size(channel.width, channel.height)
                    prefSize(channel.width, channel.height)
                }
                else -> prefSize(channel.width, channel.height)
            }
        }
    }

    // Task Fix: Return explicit SovereigntyTouchpad for persistent tracking support. v0.3.25: 250px standard.
    fun createMoveJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "default").apply { setSize(250f, 250f) }
    fun createAimJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "aim").apply { setSize(250f, 250f) }
    fun createDroneJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "drone-mode").apply { setSize(250f, 250f) }

    /**
     * Specialized Attack Joystick for tactical schemas.
     * v0.3.25: Zero deadzone and 250f size.
     */
    fun createAttackJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(0f, skin, "aim").apply {
        setDeadzone(0f)
        setSize(250f, 250f)
    }

    fun createFireButton(skin: Skin): TextButton = TextButton("FIRE", skin, "scifi").apply { setSize(250f, 250f) }
    fun createStationButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "scifi").apply { setSize(400f, 90f) }
    fun createMarketButton(text: String, style: String, skin: Skin): TextButton = TextButton(text, skin, style).apply { setSize(250f, 80f) }
    fun createDebugButton(text: String, skin: Skin): TextButton = TextButton(text, skin, "scifi").apply { setSize(200f, 60f) }
    fun createModeToggle(skin: Skin): TextButton = TextButton("WPN", skin, "mode-toggle").apply { setSize(120f, 120f) }
    fun createLabel(text: String, skin: Skin, style: String = "default"): Label = Label(text, skin, style)
}
