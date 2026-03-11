package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * Tactical Sector Orchestrator (v2.2.3)
 * ROLE: Manages galactic positioning, tension, and warp transitions.
 */
class SectorManager {
    var currentSector = SectorComponent("SECTOR ALPHA-1", Faction.NEUTRAL, 0.1f)

    fun generateSector(name: String, faction: Faction) {
        currentSector = SectorComponent(name, faction, 0.0f)
    }

    /**
     * Triggers a Diegetic Warp Event.
     * v2.2.3: Spawns the Warp VFX entities and prepares for sector handoff.
     */
    fun initiateWarp(world: World) {
        with(world) {
            // Spawn Warp Tunnel VFX (Concentric Arcs in World Space)
            entity {
                it += HUDElementComponent(
                    space = HUDSpace.WORLD,
                    width = 200f,
                    height = 200f,
                    layer = HUDLayer.VFX
                )
                it += HUDRendererComponent(renderType = HUDRenderType.ARC, styleId = "WARP_TUNNEL")
                it += HUDAnimationComponent(animationId = "WARP_ZOOM", intensity = 5f)
            }

            // Future: Trigger actual sector swap logic after animation delay
        }
    }

    fun updateTension(delta: Float) {
        currentSector.tension = (currentSector.tension + delta).coerceIn(0f, 1f)
    }
}
