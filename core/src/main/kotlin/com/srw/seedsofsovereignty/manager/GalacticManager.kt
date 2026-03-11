package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*

class GalacticManager(private val world: World, private val sectorManager: SectorManager) {
    fun transitionTo(name: String, faction: Faction) {
        val player = world.family { all(PlayerComponent) }.firstOrNull() ?: return

        // 1. World Wipe: Remove all except Player & HUD
        world.family { none(PlayerComponent, HUDElementComponent, HUDRendererComponent) }.forEach {
            world -= it
        }

        // 2. Trigger Warp & Glitch
        with(world) {
            player.configure {
                it += HUDGlitchComponent(intensity = 1.0f, duration = 0.5f)
                it += WarpComponent(isActive = true, speed = 1.0f)
            }
        }

        // 3. Update Sector
        sectorManager.generateSector(name, faction)
    }
}
