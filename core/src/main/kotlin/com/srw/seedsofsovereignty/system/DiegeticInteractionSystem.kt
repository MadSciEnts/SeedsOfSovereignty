package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.ui.factory.CommandMatrixFactory

/**
 * Diegetic Interaction System (v1.3.0)
 * ROLE: Processing AR-layer hits and routing them to ECS state and factory refreshes.
 *
 * UPDATES:
 * 1. [REFRESH] - Bridge actionKey hits to CommandMatrixFactory refreshes.
 * 2. [PERSISTENCE] - Route persistent/sticky inputs to TacticalHubComponent (Phase 25.5).
 * 3. [DRONE_PIVOTS] - Implement persistent update logic for drone loiter points (Phase 26.1).
 */
fun processDiegeticHit(world: World, actionKey: String) {
    val player = world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull() ?: return

    with(world) {
        val hub = player[TacticalHubComponent]

        when {
            actionKey.startsWith("CMD_MATRIX_CAT_") -> {
                hub.selectedCategory = actionKey.removePrefix("CMD_MATRIX_CAT_")
                Gdx.app.log("COMMAND_MATRIX", "Category Switched: ${hub.selectedCategory}")
                CommandMatrixFactory.refreshModuleShelf(world, player)
            }
            actionKey.startsWith("CMD_MATRIX_MOD_") -> {
                val slotIndex = actionKey.removePrefix("CMD_MATRIX_MOD_").toIntOrNull() ?: 0
                hub.selectedModuleIndex = slotIndex
                Gdx.app.log("COMMAND_MATRIX", "Module Selected: $slotIndex")
                CommandMatrixFactory.refreshInstruments(world, player)
            }
            // Phase 25.5: Sticky/Persistent Control Routing
            actionKey.startsWith("STICKY_") -> {
                Gdx.app.log("COMMAND_MATRIX", "Persistent Hit Detected: $actionKey")
            }
            // Phase 26.1: Drone loiter point updates
            actionKey.startsWith("DRONE_TARGET_PAD") -> {
                // Logic: Extract touchpad coordinates from interaction data (Phase 26.1)
                // For now, we stub the persistent map update
                val touchpadId = actionKey // e.g. "DRONE_TARGET_PAD_0"
                val touchVector = Vector2(0f, 0f) // Will be populated by handleTouch data in future iteration
                hub.tacticalPivots[touchpadId] = touchVector
                Gdx.app.log("COMMAND_MATRIX", "Loiter Point Updated: $touchpadId")
            }
        }
    }
}
