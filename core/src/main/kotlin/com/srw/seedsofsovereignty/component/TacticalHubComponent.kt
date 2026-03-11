package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * Bidirectional data hub for the TVM (v0.4.0).
 * Handles both outgoing control data and incoming status signals.
 */
data class StatusSignal(
    val value: Float,
    val label: String? = null,
    val color: Color? = null,
    val maxValue: Float? = null
)

data class TacticalHubComponent(
    // Momentary Button bitmask (supports up to 64 simple buttons)
    var activeBitmask: Long = 0L,

    // Complex Input Slots (Joysticks/Sliders)
    val vectors: MutableMap<String, Vector2> = mutableMapOf(),
    val scalars: MutableMap<String, Float> = mutableMapOf(),

    // Incoming Feedback Signals from Modules
    val signals: MutableMap<String, StatusSignal> = mutableMapOf()
) : Component<TacticalHubComponent> {

    /**
     * Resets the input/signal state without reallocating maps.
     */
    fun reset() {
        activeBitmask = 0L
        vectors.clear()
        scalars.clear()
        signals.clear()
    }

    override fun type() = TacticalHubComponent
    companion object : ComponentType<TacticalHubComponent>()
}

/**
 * Tracks the player's current tactical focus or weapon lock.
 */
data class TacticalLockComponent(
    var targetEntity: Entity? = null,
    var lockStrength: Float = 0f, // 0 to 1 for "locking on" animation
    var isConfirmed: Boolean = false
) : Component<TacticalLockComponent> {
    override fun type() = TacticalLockComponent
    companion object : ComponentType<TacticalLockComponent>()
}

/**
 * Interface for UI elements that listen to StatusSignals from the ECS.
 */
interface SignalObserver {
    fun onSignalReceived(signal: StatusSignal)
}
