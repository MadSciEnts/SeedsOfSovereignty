package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.WeaponModuleComponent
import com.srw.seedofsov.model.CoordinateSpace
import com.srw.seedofsov.model.ModuleSlot
import com.srw.seedofsov.ui.Panel
import com.srw.seedofsov.ui.UiAssetListener

/**
 * Reusable Command Matrix UI component.
 * Truth on Disk: Tracks active slot and ensures UI state mirrors module state.
 */
class CommandMatrix(
    val game: SovereigntyGame,
    val isLeftAligned: Boolean
) : Panel(game.uiFactory.skin.getDrawable("panel")), UiAssetListener {

    private val controlArea = Panel()
    private var activeJoystick: Joystick? = null
    private var pendingSlot: ModuleSlot? = null

    // Exposed for InterfaceSystem sync
    var activeSlot: ModuleSlot? = null
        private set

    private val uiScale = 1.4f

    init {
        setSize(525f, 600f)
        controlArea.setSize(width, height)
        controlArea.setPosition(0f, 0f)
        addActor(controlArea)

        game.uiFactory.addListener(this)
    }

    override fun onAssetsReady() {
        pendingSlot?.let { selectModuleFromSlot(it) }
    }

    fun selectModuleFromSlot(slot: ModuleSlot) {
        if (!game.uiFactory.isLoaded) {
            pendingSlot = slot
            return
        }

        pendingSlot = null
        activeSlot = slot
        val module = slot.equippedModule ?: return
        controlArea.clearChildren()
        activeJoystick = null

        val controls = module.getModuleControls()
        var maxWidth = 525f

        controls.forEach { ctrl ->
            val sx = ctrl.x * uiScale
            val sy = ctrl.y * uiScale
            val sw = ctrl.width * uiScale
            val sh = ctrl.height * uiScale

            maxWidth = Math.max(maxWidth, sx + sw + 30f)
            val channel = slot.getChannel(ctrl.channelName)

            when (ctrl.type) {
                "JOYSTICK" -> {
                    val joystick = Joystick(game, sw / 2f, channel).apply {
                        setPosition(sx, sy)
                    }
                    activeJoystick = joystick
                    controlArea.addActor(joystick)
                }
                "TOUCHPAD" -> {
                    val touchpad = Touchpad(game, channel).apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                    }
                    controlArea.addActor(touchpad)
                }
                "SLIDER" -> {
                    val slider = Slider(game, channel).apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                    }
                    controlArea.addActor(slider)
                }
                "STATS" -> {
                    val readout = StatsReadout(game, channel, ctrl.channelName.uppercase()).apply {
                        setPosition(sx, sy)
                    }
                    controlArea.addActor(readout)
                }
                "LABEL" -> {
                    val text = if (ctrl.channelName == "ammo_type" && module is WeaponModuleComponent) {
                        module.projectileName
                    } else {
                        ctrl.channelName.uppercase()
                    }

                    val label = game.uiFactory.createLabel(text).apply {
                        setPosition(sx, sy)
                    }
                    controlArea.addActor(label)
                }
                "FIRE_BUTTON", "BUTTON" -> {
                    val label = if (ctrl.type == "FIRE_BUTTON") "FIRE" else ctrl.channelName.replace("_", " ").uppercase()
                    val btn = game.uiFactory.createButton(label).apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                        addListener(object : ClickListener() {
                            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                                channel.valueBoolean = true
                                return super.touchDown(event, x, y, pointer, button)
                            }
                            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                                channel.valueBoolean = false
                                super.touchUp(event, x, y, pointer, button)
                            }
                        })
                    }
                    controlArea.addActor(btn)
                }
                "CYCLE_BUTTON" -> {
                    // Initialize channel from module state if first selection
                    if (channel.valueEnum == null) {
                        channel.valueEnum = module.commandSpace
                    }

                    val btn = game.uiFactory.createButton("").apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                val current = channel.valueEnum as? CoordinateSpace ?: CoordinateSpace.OBJECT
                                val next = if (current == CoordinateSpace.OBJECT) CoordinateSpace.WORLD else CoordinateSpace.OBJECT
                                channel.valueEnum = next
                            }
                        })
                    }

                    // Reactive behavior: Keep button text in sync with DataChannel
                    if (btn is TextButton) {
                        btn.addAction(object : com.badlogic.gdx.scenes.scene2d.Action() {
                            override fun act(delta: Float): Boolean {
                                val current = channel.valueEnum as? CoordinateSpace ?: CoordinateSpace.OBJECT
                                val targetText = if (current == CoordinateSpace.OBJECT) "SHIP" else "CAMERA"
                                if (btn.text.toString() != targetText) {
                                    btn.setText(targetText)
                                }
                                return false // Persistent action
                            }
                        })
                    }

                    controlArea.addActor(btn)
                }
            }
        }

        this.width = maxWidth
        controlArea.width = maxWidth
    }

    fun getNavigationOutput(): Vector2 {
        return activeJoystick?.output ?: Vector2.Zero
    }
}
