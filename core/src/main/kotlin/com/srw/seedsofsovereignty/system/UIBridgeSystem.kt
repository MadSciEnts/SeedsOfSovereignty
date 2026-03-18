package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.ui.CommandMatrixView

/**
 * UI BRIDGE SYSTEM: Decouples ECS state from Scene2D UI.
 * Scans for player module changes and updates the Command Matrix shelf.
 */
class UIBridgeSystem(
    private val commandMatrix: CommandMatrixView = inject()
) : IntervalSystem(interval = Fixed(0.5f)) {

    private var lastCategories = setOf<ModuleCategory>()

    override fun onTick() {
        val player = world.family { all(PlayerComponent) }.firstOrNull() ?: return
        val activeCategories = mutableSetOf<ModuleCategory>()

        world.run {
            // Check core systems
            if (player.has(PowerCoreComponent)) activeCategories.add(ModuleCategory.POWER)
            if (player.has(ArcedDefenseComponent)) activeCategories.add(ModuleCategory.SHIELDS)

            // Check attached modules
            family { all(ModuleComponent, AttachmentComponent) }.forEach { entity ->
                if (entity[AttachmentComponent].parent == player) {
                    activeCategories.add(entity[ModuleComponent].category)
                }
            }
        }

        // Only update UI if the category set has changed
        if (activeCategories != lastCategories) {
            lastCategories = activeCategories
            commandMatrix.updateCategories(activeCategories)
        }
    }
}
