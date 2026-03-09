package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.srw.seedsofsovereignty.component.InputChannel
import com.srw.seedsofsovereignty.component.TacticalSchema

/**
 * The Render Engine for Tactical Schemas (v0.4.0).
 * Refactored for Bidirectional Chained Instrument model.
 * v0.4.0: Uses relative chaining (relX, relY) and binds to TacticalHubComponent.
 */
class TacticalClusterWidget(private val skin: Skin) : WidgetGroup() {
    private val channelActors = mutableMapOf<String, Actor>()

    /**
     * Renders the schema and connects UI events to the action callback.
     * @param schema The manifest of input channels required by the hardware.
     * @param actionCallback Zero-allocation conduit to the ECS TacticalHubComponent.
     * @param inputFactory Creates the UI Actor for a channel, utilizing the callback for event binding.
     */
    fun renderSchema(
        schema: TacticalSchema,
        actionCallback: (String, Any) -> Unit,
        inputFactory: (InputChannel, (String, Any) -> Unit) -> Actor
    ) {
        clearPassData()

        var lastX = 0f
        var lastY = 0f

        schema.inputs.forEach { channel ->
            val actor = inputFactory(channel, actionCallback)
            channelActors[channel.actionKey] = actor

            // Chained Relative Positioning
            val posX = lastX + channel.relX
            val posY = lastY + channel.relY

            actor.setSize(channel.width, channel.height)
            actor.setPosition(posX, posY)
            addActor(actor)

            lastX = posX
            lastY = posY
        }
    }

    private fun clearPassData() {
        channelActors.clear()
        clearChildren()
    }
}
