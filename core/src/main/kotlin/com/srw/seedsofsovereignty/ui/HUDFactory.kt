package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.srw.seedsofsovereignty.component.InputChannel
import com.srw.seedsofsovereignty.component.ControlClassification
import com.srw.seedsofsovereignty.component.TacticalHubComponent
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.PlayerComponent
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

        // TVM v0.4.0: Integrate procedural textures
        val industrialCollection = HUDTextureGenerator.generate("INDUSTRIAL", skin)

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
            up = industrialCollection.buttonUp
            down = industrialCollection.buttonDown
            over = industrialCollection.buttonUp
            font = mainFont
            fontColor = Color.WHITE
            overFontColor = Color.CYAN
            downFontColor = Color.BLACK
        })

        registerStyle("mode-toggle", TextButton.TextButtonStyle().apply {
            up = industrialCollection.toggleOff
            checked = industrialCollection.toggleOn
            over = industrialCollection.toggleOff
            down = industrialCollection.toggleOn
            font = mainFont
            fontColor = Color.BLACK
        })

        // 7. Joysticks
        skin.add("default", Touchpad.TouchpadStyle().apply {
            background = industrialCollection.joystickBg
            knob = industrialCollection.joystickKnob
        })

        skin.add("aim", Touchpad.TouchpadStyle().apply {
            background = industrialCollection.joystickBg
            knob = industrialCollection.joystickKnob
        })

        skin.add("drone-mode", Touchpad.TouchpadStyle().apply {
            background = industrialCollection.joystickBg
            knob = industrialCollection.joystickKnob
        })

        skin.add("default", ScrollPane.ScrollPaneStyle().apply {
            vScroll = skin.newDrawable("white", Color(0.1f, 0.3f, 0.5f, 0.3f))
            vScrollKnob = skin.newDrawable("white", Color(0.2f, 0.6f, 1f, 0.8f))
        })

        return skin
    }

    /**
     * TVM Phase 6.3: Factory Visual Refactor.
     * v0.4.0: Updated to use procedural skins and signal hooks.
     * v0.4.1: Signal-Aware Actors and Footprint Calibration.
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
                        style = Slider.SliderStyle(style).apply { background = skin.getDrawable("ind_s_bg"); knob = skin.getDrawable("ind_s_knob") }
                    }
                    add(slider).fillX().expandX()
                    name = channel.actionKey
                }
            }
            ControlClassification.STATUS_LIGHT -> {
                Image(skin.getDrawable("ind_light_yellow")).apply {
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
