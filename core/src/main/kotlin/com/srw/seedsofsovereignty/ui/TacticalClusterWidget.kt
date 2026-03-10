package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.srw.seedsofsovereignty.component.InputChannel
import com.srw.seedsofsovereignty.component.TacticalSchema

/**
 * The Render Engine for Tactical Schemas (v0.4.0).
 * Refactored for Bidirectional Chained Instrument model.
 * v0.5.3: Implemented Anchor-Based Layout supporting 'relativeTo' DNA.
 * v0.5.4: Two-Pass Rendering Loop to fix layout occlusion and anchor availability.
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

        // Pass 1: Creation and Indexing
        // Create all actors first so that anchors (relativeTo) are guaranteed to exist.
        schema.inputs.forEach { channel ->
            val actor = inputFactory(channel, actionCallback)
            channelActors[channel.actionKey] = actor
            actor.setSize(channel.width, channel.height)
            addActor(actor)
        }

        // Pass 2: Layout and Positioning
        // Iterate through the map and calculate positions using the 'relativeTo' keys.
        schema.inputs.forEach { channel ->
            val actor = channelActors[channel.actionKey] ?: return@forEach

            // MISSION 2: Local Coordinate Chaining - Remove lastActor fallback
            val anchor = if (channel.relativeTo != null) channelActors[channel.relativeTo] else null

            val baseX = anchor?.x ?: 0f
            val baseY = anchor?.y ?: 0f

            val posX = baseX + channel.relX
            val posY = baseY + channel.relY

            actor.setPosition(posX, posY)
        }
    }

    private fun clearPassData() {
        channelActors.clear()
        clearChildren()
    }
}
