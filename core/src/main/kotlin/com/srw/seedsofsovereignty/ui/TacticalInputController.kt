package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * Technical Specification: TacticalInputController (v0.7.0)
 * ROLE: Signal Bridge & Schema Processor for the TVM.
 *
 * DESIGN PRINCIPLES:
 * 1. [SELECTIVE SUFFIXING] - Only module-specific keys (DRONE_, etc.) are suffixed.
 * 2. [IDENTITY BINDING] - Uses slotIndex for signal parity between UI and Launcher.
 * 3. [SIGNAL HARDENING] - Updates hub.signals immediately to bypass system latency.
 * 4. [PERSISTENCE LOCK] - v0.7.0: Now supports TacticalSurface binding.
 */
class TacticalInputController(
    private val world: World,
    private val skin: Skin
) {
    private val signalMap = mutableMapOf<String, String>()

    /**
     * DNA PROCESSOR: Suffixes module-specific keys with [slotIndex].
     */
    fun processSchema(schema: TacticalSchema, slotIndex: Int): TacticalSchema {
        signalMap.clear()
        val suffix = "_$slotIndex"

        val processedInputs = schema.inputs.map { input ->
            var nextActionKey = input.actionKey
            if (nextActionKey.startsWith("DRONE_") || nextActionKey.startsWith("READOUT_")) {
                nextActionKey = "${nextActionKey}$suffix"
            }

            var nextSignalKey = input.signalKey
            if (nextSignalKey != null) {
                if (nextSignalKey.startsWith("DRONE_") || nextSignalKey.startsWith("DRONES_") || nextSignalKey.startsWith("READOUT_")) {
                    nextSignalKey = "${nextSignalKey}$suffix"
                }
                signalMap[nextActionKey] = nextSignalKey
            }

            var nextRelativeTo = input.relativeTo
            if (nextRelativeTo != null) {
                if (nextRelativeTo.startsWith("DRONE_") || nextRelativeTo.startsWith("READOUT_")) {
                    nextRelativeTo = "${nextRelativeTo}$suffix"
                }
            }

            input.copy(actionKey = nextActionKey, signalKey = nextSignalKey, relativeTo = nextRelativeTo)
        }
        return schema.copy(inputs = processedInputs)
    }

    /**
     * ECS HUB CONDUIT: Maps UI events to TacticalHubComponent.
     */
    val actionCallback: (String, Any) -> Unit = { actionKey, value ->
        with(world) {
            val playerFamily = family { all(PlayerComponent, TacticalHubComponent) }
            playerFamily.firstOrNull()?.let { player ->
                val hub = player[TacticalHubComponent]
                when (value) {
                    is Boolean -> {
                        if (actionKey.startsWith("FIRE")) {
                            if (value) hub.activeBitmask = hub.activeBitmask or (1L shl 0)
                            else hub.activeBitmask = hub.activeBitmask and (1L shl 0).inv()
                        } else {
                            val scalarVal = if (value) 1f else 0f
                            hub.scalars[actionKey] = scalarVal
                            signalMap[actionKey]?.let { sigKey ->
                                hub.signals[sigKey] = StatusSignal(value = scalarVal)
                            }
                        }
                    }
                    is Vector2 -> {
                        hub.vectors[actionKey] = hub.vectors[actionKey]?.set(value) ?: Vector2(value)

                        if (player.has(TacticalPersistenceComponent)) {
                            val persistence = player[TacticalPersistenceComponent]
                            persistence.persistentInputs[actionKey] = Vector2(value)
                        }
                    }
                    is Float -> {
                        hub.scalars[actionKey] = value
                        signalMap[actionKey]?.let { sigKey ->
                            hub.signals[sigKey] = StatusSignal(value = value)
                        }
                    }
                }
            }
        }
    }

    /**
     * INSTRUMENT BINDER: Creates actors and attaches event listeners.
     * v0.7.0: Added TACTICAL_SURFACE support.
     */
    fun bindInstrument(channel: InputChannel, callback: (String, Any) -> Unit): Actor {
        var restoreVector: Vector2? = null
        with(world) {
            val playerFamily = family { all(PlayerComponent, TacticalPersistenceComponent) }
            playerFamily.firstOrNull()?.let { player ->
                restoreVector = player[TacticalPersistenceComponent].persistentInputs[channel.actionKey]
            }
        }

        val actor = HUDInputService.createTacticalInput(skin, channel, world, restoreVector)
        val instrument = if (actor is Container<*>) actor.actor else actor

        if (instrument is SovereigntyTouchpad) {
            instrument.knobRestore = restoreVector
        }

        when (channel.classification) {
            ControlClassification.ACTION_BUTTON -> {
                instrument.addListener(object : ClickListener() {
                    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        callback(channel.actionKey, true)
                        return true
                    }
                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        callback(channel.actionKey, false)
                    }
                })
            }
            ControlClassification.TOGGLE_SWITCH -> {
                instrument.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent?, target: Actor?) {
                        val btn = target as? Button
                        btn?.let { callback(channel.actionKey, it.isChecked) }
                    }
                })
            }
            ControlClassification.MOVEMENT_STICK, ControlClassification.TACTICAL_STICK, ControlClassification.TACTICAL_SURFACE -> {
                instrument.addListener(object : ChangeListener() {
                    private val vec = Vector2()
                    override fun changed(event: ChangeEvent?, target: Actor?) {
                        when (target) {
                            is Touchpad -> {
                                vec.set(target.knobPercentX, target.knobPercentY)
                                callback(channel.actionKey, vec)
                            }
                            is TacticalSurface -> {
                                callback(channel.actionKey, target.getValue())
                            }
                        }
                    }
                })
            }
            ControlClassification.MODIFIER_SLIDER -> {
                instrument.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent?, target: Actor?) {
                        val sld = target as? Slider
                        sld?.let { callback(channel.actionKey, it.value) }
                    }
                })
            }
            else -> { /* No-op */ }
        }
        return actor
    }

    /**
     * AGGREGATOR BRIDGE: Broadcasts all StatusSignals from the Hub to UI observers.
     */
    fun updateSignals(callback: (String, StatusSignal) -> Unit) {
        with(world) {
            val playerFamily = family { all(PlayerComponent, TacticalHubComponent) }
            playerFamily.firstOrNull()?.let { player ->
                val hub = player[TacticalHubComponent]
                hub.signals.forEach { (key, signal) ->
                    callback(key, signal)
                }
            }
        }
    }
}
