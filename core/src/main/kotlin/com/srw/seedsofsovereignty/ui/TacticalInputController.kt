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
 * Encapsulates input signal routing and tactical schema processing.
 * Part of Phase 9.6 Load Shedding.
 * v0.5.6: Signal Bridge Hardening. Immediate signal updates for toggle switches and scalars.
 * v0.5.7: Entity ID Binding. Tactical keys are suffixed by Entity ID for crosstalk isolation.
 * Phase 9.9.2: Slot-Based Suffixing. Uses slotIndex for universal suffixing.
 * Phase 9.9.3: Selective Suffixing. Protects global keys (FIRE, AIM) from suffixing.
 * Phase 9.9.4: Robust Prefix Detection. Suffixes DRONE_, DRONES_, and READOUT_ patterns.
 */
class TacticalInputController(
    private val world: World,
    private val skin: Skin
) {
    private val signalMap = mutableMapOf<String, String>()

    /**
     * Maps hardware action keys to their signal feedback keys and applies Slot Index suffixes.
     * Only suffixes keys that are module-specific (DRONE_, DRONES_, READOUT_).
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
                // v0.6.3: Detection expanded to include "DRONES_" pattern used by regen system.
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
     * Centralized callback for all tactical instruments.
     * v0.5.6: Immediately updates hub.signals to bypass system-latency for toggle feedback.
     */
    val actionCallback: (String, Any) -> Unit = { actionKey, value ->
        with(world) {
            val playerFamily = family { all(PlayerComponent, TacticalHubComponent) }
            playerFamily.firstOrNull()?.let { player ->
                val hub = player[TacticalHubComponent]
                when (value) {
                    is Boolean -> {
                        // FIRE is a global bitmask action. We check startsWith to handle potential legacy suffixing.
                        if (actionKey.startsWith("FIRE")) {
                            if (value) hub.activeBitmask = hub.activeBitmask or (1L shl 0)
                            else hub.activeBitmask = hub.activeBitmask and (1L shl 0).inv()
                        } else {
                            val scalarVal = if (value) 1f else 0f
                            hub.scalars[actionKey] = scalarVal
                            // Signal Bridge Hardening: Sync signal immediately
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
                        // Signal Bridge Hardening: Sync signal immediately
                        signalMap[actionKey]?.let { sigKey ->
                            hub.signals[sigKey] = StatusSignal(value = value)
                        }
                    }
                }
            }
        }
    }

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

        // Phase 9.9.1: Hard-wired restoration to bypass layout lifecycle reset
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
            ControlClassification.MOVEMENT_STICK, ControlClassification.TACTICAL_STICK -> {
                instrument.addListener(object : ChangeListener() {
                    private val knobPercent = Vector2()
                    override fun changed(event: ChangeEvent?, target: Actor?) {
                        val pad = target as? Touchpad
                        pad?.let {
                            knobPercent.set(it.knobPercentX, it.knobPercentY)
                            callback(channel.actionKey, knobPercent)
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
}
