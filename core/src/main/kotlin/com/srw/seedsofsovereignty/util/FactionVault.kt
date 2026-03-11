package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.srw.seedsofsovereignty.component.Faction

/**
 * ARCHITECTURAL WARNING: LEAD ARCHITECT MANDATE (v2.6.0)
 * ROLE: The Immutable Visual DNA Anchor.
 *
 * This object maps Factions to their energy colors and style keys.
 * DO NOT ALTER without T1 HIGH TABLE CONSENSUS.
 */
object FactionVault {

    /**
     * Returns the primary energy color for a specific faction.
     */
    fun getEnergyColor(faction: Faction): Color {
        return when (faction) {
            Faction.VANGUARD -> Color.CYAN
            Faction.SWARM -> Color.LIME
            Faction.ANCIENTS -> Color.GOLD
            Faction.NEUTRAL -> Color.LIGHT_GRAY
        }
    }

    /**
     * Returns the Style ID used for HUD elements associated with this faction.
     */
    fun getStyleId(faction: Faction): String {
        return when (faction) {
            Faction.VANGUARD -> "VANGUARD"
            Faction.SWARM -> "SWARM"
            Faction.ANCIENTS -> "IMPERIAL"
            Faction.NEUTRAL -> "INDUSTRIAL"
        }
    }

    /**
     * Returns the standard hostile color for AR Pips and Radar.
     */
    fun getHostileColor(): Color = Color.RED
}
