package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * POWER DISTRIBUTION SYSTEM: Syncs hardware slider values to the Ship's priority bus.
 * Standardized: Runs at 10Hz (0.1s interval) to minimize CPU load.
 * Compliant: Zero-allocation logic using direct component access.
 */
class PowerDistributionSystem : IntervalSystem(interval = Fixed(0.1f)) {

    private val shipFamily = world.family { all(ShipPowerPriorityComponent, ShipManifestComponent) }

    override fun onTick() {
        shipFamily.forEach { ship ->
            syncPower(ship)
        }
    }

    private fun syncPower(ship: Entity) {
        val priority = ship[ShipPowerPriorityComponent]
        val manifest = ship[ShipManifestComponent]

        // Scan manifest for POWER_CORE modules
        for (i in 0 until manifest.modules.size) {
            val moduleEntity = manifest.modules[i]

            // Safety check for destroyed or invalid modules
            if (!world.run { moduleEntity.has(ModuleChannelsComponent) }) continue

            val channels = moduleEntity[ModuleChannelsComponent]
            if (channels.type != ModuleType.POWER_CORE) continue

            // Sync Slider Values to Priority Array
            // Note: We use the PowerCategory enum indices for the priority array.
            priority.priorities[PowerCategory.WEAPON.ordinal] = channels.inputs["WEAPON"]?.value ?: 1.0f
            priority.priorities[PowerCategory.SHIELD.ordinal] = channels.inputs["SHIELD"]?.value ?: 1.0f
            priority.priorities[PowerCategory.ENGINE.ordinal] = channels.inputs["ENGINE"]?.value ?: 1.0f
            priority.priorities[PowerCategory.UTILITY.ordinal] = channels.inputs["UTILITY"]?.value ?: 1.0f

            // Optimization: Assuming one primary power core per ship for now.
            break
        }
    }
}
