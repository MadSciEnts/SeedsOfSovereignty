package com.srw.seedofsov.ui.hud

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.HudFxComponent
import com.srw.seedofsov.ecs.components.ShieldModuleComponent
import com.srw.seedofsov.ecs.components.WeaponModuleComponent
import com.srw.seedofsov.model.*
import com.srw.seedofsov.ui.Panel
import com.srw.seedofsov.ui.UiAssetListener
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with

/**
 * Reusable Command Matrix UI component.
 * Truth on Disk: Scales interaction areas and bridges modular input to technical HUD effects.
 */
class CommandMatrix(
    val game: SovereigntyGame,
    val isLeftAligned: Boolean
) : Panel(game.uiFactory.skin.getDrawable("panel")), UiAssetListener {

    private val controlArea = Panel()
    private var activeJoystick: Joystick? = null
    private var activeHudFxEntity: Entity? = null
    private var pendingSlot: ModuleSlot? = null

    var activeSlot: ModuleSlot? = null
        private set

    private val uiScale = 1.4f

    init {
        setSize(650f, 650f)
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

        // Clean up previous HUD FX if any
        activeHudFxEntity?.let { game.engine.removeEntity(it) }
        activeHudFxEntity = null

        pendingSlot = null
        activeSlot = slot
        val module = slot.equippedModule ?: return
        controlArea.clearChildren()
        activeJoystick = null

        val controls = module.getModuleControls()
        var maxWidth = 650f

        // Instantiate new HUD FX if this module supports it (e.g. Shields)
        val hudFxComp = if (module is ShieldModuleComponent) {
            val entity = game.engine.entity {
                with<HudFxComponent> {
                    type = HudFxType.SHIELD_MANIFOLD
                }
            }
            activeHudFxEntity = entity
            entity[HudFxComponent.mapper]
        } else null

        controls.forEach { ctrl ->
            var sx = ctrl.x * uiScale
            var sy = ctrl.y * uiScale
            var sw = ctrl.width * uiScale
            var sh = ctrl.height * uiScale

            if (module is ShieldModuleComponent && ctrl.type == "TOUCHPAD") {
                sw = 450f
                sh = 450f
                sx = (width - sw) / 2f
                sy = (height - sh) / 2f
            }

            maxWidth = Math.max(maxWidth, sx + sw + 30f)
            val channel = slot.getChannel(ctrl.channelName)

            when (ctrl.type) {
                "JOYSTICK" -> {
                    val joystick = Joystick(game, sw / 2f, channel).apply { setPosition(sx, sy) }
                    activeJoystick = joystick
                    controlArea.addActor(joystick)
                }
                "TOUCHPAD" -> {
                    val touchpad = Touchpad(game, channel, module, hudFxComp).apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                    }
                    controlArea.addActor(touchpad)
                }
                "SLIDER" -> {
                    val slider = Slider(game, channel).apply { setSize(sw, sh); setPosition(sx, sy) }
                    controlArea.addActor(slider)
                }
                "STATS" -> {
                    if (module !is ShieldModuleComponent) {
                        val readout = StatsReadout(game, channel, ctrl.channelName.uppercase()).apply { setPosition(sx, sy) }
                        controlArea.addActor(readout)
                    }
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
                    if (channel.valueEnum == null) channel.valueEnum = module.commandSpace
                    val btn = game.uiFactory.createButton("").apply {
                        setSize(sw, sh)
                        setPosition(sx, sy)
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                val current = channel.valueEnum as? CoordinateSpace ?: CoordinateSpace.OBJECT
                                channel.valueEnum = if (current == CoordinateSpace.OBJECT) CoordinateSpace.WORLD else CoordinateSpace.OBJECT
                            }
                        })
                    }
                    btn.addAction(object : com.badlogic.gdx.scenes.scene2d.Action() {
                        override fun act(delta: Float): Boolean {
                            val current = channel.valueEnum as? CoordinateSpace ?: CoordinateSpace.OBJECT
                            val targetText = if (current == CoordinateSpace.OBJECT) "SHIP" else "CAMERA"
                            if (btn.text.toString() != targetText) btn.setText(targetText)
                            return false
                        }
                    })
                    controlArea.addActor(btn)
                }
            }
        }

        this.width = maxWidth
        controlArea.width = maxWidth
    }

    fun getNavigationOutput(): Vector2 = activeJoystick?.output ?: Vector2.Zero
}
