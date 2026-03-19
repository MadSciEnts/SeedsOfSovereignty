package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * SELECTED: Marks an entity as the current target of the player's command matrix.
 * Standardized: Only one entity should have this component at a time for the local player.
 */
class SelectedComponent : Component<SelectedComponent> {
    override fun type() = SelectedComponent
    companion object : ComponentType<SelectedComponent>()
}
