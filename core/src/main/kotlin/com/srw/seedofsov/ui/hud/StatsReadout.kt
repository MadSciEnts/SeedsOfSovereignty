package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.scenes.scene2d.Group
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.model.DataChannel
import java.util.Locale

/**
 * A dynamic text readout for module technical data.
 * Locale-safe and synchronized with DataChannel state.
 */
class StatsReadout(
    val game: SovereigntyGame,
    private val channel: DataChannel? = null,
    private val prefix: String = ""
) : Group() {
    private val label = game.uiFactory.createLabel("${prefix}: --")

    init {
        addActor(label)
    }

    override fun act(delta: Float) {
        super.act(delta)
        // Update label with current channel value using locale-safe formatting
        channel?.let {
            val formattedValue = String.format(Locale.US, "%.1f", it.valueFloat)
            label.setText("${prefix}: $formattedValue")
        }
    }
}
