package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.srw.seedsofsovereignty.component.InputChannel
import com.srw.seedsofsovereignty.component.ControlClassification
import com.srw.seedsofsovereignty.component.TacticalHubComponent
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.github.quillraven.fleks.World

/**
 * Technical Specification: HUDInputService (v0.7.0)
 * ROLE: Specialized Factory for TVM interactive instruments.
 *
 * ARCHITECTURAL STANDARDS:
 * 1. [CONTAINER WRAPPING] - Every instrument is wrapped in a "Cradle" Container with
 *    the "panel" background. Callers must unwrap the container to access the internal actor.
 * 2. [SIGNAL BINDING] - UI Labels are "Signal-Aware" and self-update via act().
 * 3. [SCALING] - Component footprints are calibrated here (e.g., 250f Joysticks).
 */
object HUDInputService {

    /**
     * CORE FACTORY: Translates DNA classification into physical LibGDX actors.
     */
    fun createTacticalInput(skin: Skin, channel: InputChannel, world: World, restoreState: Vector2? = null): Actor {
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
                }.apply { name = channel.actionKey }
            }
            ControlClassification.TOGGLE_SWITCH -> {
                object : TextButton(channel.label ?: channel.actionKey, skin, "mode-toggle") {
                    override fun act(delta: Float) {
                        super.act(delta)
                        if (channel.signalKey != null) {
                            world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()?.let { player ->
                                val hub = with(world) { player[TacticalHubComponent] }
                                val signal = hub.signals[channel.signalKey]
                                if (signal != null) {
                                    isChecked = signal.value > 0.5f
                                    setText("${channel.label ?: channel.actionKey}: ${if (isChecked) "ON" else "OFF"}")
                                }
                            }
                        }
                    }
                }.apply { name = channel.actionKey }
            }
            ControlClassification.MOVEMENT_STICK -> {
                createAttackJoystick(skin).apply { name = channel.actionKey }
            }
            ControlClassification.TACTICAL_STICK -> {
                SovereigntyTouchpad(15f, skin, "default").apply {
                    name = channel.actionKey
                    isPersistent = true
                    knobRestore = restoreState
                }
            }
            ControlClassification.TACTICAL_SURFACE -> {
                TacticalSurface(skin, channel.styleId ?: "default").apply {
                    name = channel.actionKey
                    positionRestore = restoreState
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
                    }
                    add(slider).fillX().expandX()
                    name = channel.actionKey
                }
            }
            ControlClassification.STATUS_LIGHT -> {
                Image(skin.getDrawable("current_status_light")).apply { name = channel.actionKey }
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
                }.apply { name = channel.actionKey }
            }
        }

        return Container(baseActor).apply {
            background = skin.getDrawable("panel")
            pad(5f)
            fill()
            when (channel.classification) {
                ControlClassification.ACTION_BUTTON, ControlClassification.TOGGLE_SWITCH -> {
                    size(100f, 100f); prefSize(100f, 100f)
                }
                ControlClassification.TACTICAL_STICK, ControlClassification.TACTICAL_SURFACE -> {
                    size(250f, 250f); prefSize(250f, 250f)
                }
                ControlClassification.DATA_READOUT -> {
                    size(channel.width, channel.height); prefSize(channel.width, channel.height)
                }
                else -> prefSize(channel.width, channel.height)
            }
        }
    }

    fun createMoveJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "default").apply { setSize(250f, 250f) }
    fun createAimJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "aim").apply { setSize(250f, 250f) }
    fun createDroneJoystick(skin: Skin): SovereigntyTouchpad = SovereigntyTouchpad(15f, skin, "drone-mode").apply { setSize(250f, 250f) }

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
