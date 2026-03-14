package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * Bidirectional data hub for the TVM (v1.0.0).
 * ROLE: The bridge between diegetic AR HUD input and module operational state.
 *
 * UPDATES:
 * 1. [RENAMING] - Standardized persistent positional storage to 'tacticalPivots' (Phase 27.0).
 */
data class StatusSignal(
    val value: Float,
    val label: String? = null,
    val color: Color? = null,
    val maxValue: Float? = null
)

data class TacticalHubComponent(
    var selectedCategory: String = "WEAPON",
    var selectedModuleIndex: Int = 0,
    var activeBitmask: Long = 0L,

    // Transient Inputs (Cleared on reset)
    val vectors: MutableMap<String, Vector2> = mutableMapOf(),
    val scalars: MutableMap<String, Float> = mutableMapOf(),

    // Persistent Inputs (Preserved on reset)
    val persistentVectors: MutableMap<String, Vector2> = mutableMapOf(),
    val tacticalPivots: MutableMap<String, Vector2> = mutableMapOf(),

    // Incoming Feedback Signals
    val signals: MutableMap<String, StatusSignal> = mutableMapOf()
) : Component<TacticalHubComponent> {

    /**
     * Resets transient state. Preserves persistence maps.
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
    var lockStrength: Float = 0f,
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
