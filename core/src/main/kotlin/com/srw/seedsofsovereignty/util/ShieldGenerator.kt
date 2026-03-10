package com.srw.seedsofsovereignty.util

import com.srw.seedsofsovereignty.component.*

/**
 * Technical Specification: ShieldGenerator (v0.8.6)
 * ROLE: Procedural Hardware DNA for defensive layers.
 *
 * DESIGN PRINCIPLES:
 * 1. [DIAMOND GRID] - Mandatory 4-quadrant reinforcement UI.
 * 2. [SIGNAL PARITY] - Maps to DiagnosticWidget quadrant signals.
 */
object ShieldGenerator {
    fun generateShield(tier: Int, rarity: Rarity): ShieldModuleComponent {
        val baseHP = 50f * tier
        val hpMult = when(rarity) {
            Rarity.COMMON -> 1.0f
            Rarity.RARE -> 1.5f
            Rarity.LEGENDARY -> 2.5f
            else -> 1.2f
        }

        return ShieldModuleComponent(
            tier = tier,
            currentHP = baseHP * hpMult,
            maxHP = baseHP * hpMult,
            rechargeRate = 5f * tier
        )
    }

    /**
     * TVM DNA: Defines the 4-Quadrant Shield Management Instrument.
     */
    fun getShieldSchema(tier: Int): TacticalSchema {
        return TacticalSchema(
            inputs = listOf(
                InputChannel(
                    classification = ControlClassification.DIAMOND_GRID,
                    actionKey = "SHIELD_REINFORCE",
                    label = "REINFORCE",
                    relX = 300f,
                    relY = 0f,
                    width = 250f,
                    height = 250f
                ),
                // Data readouts for individual quadrant status
                InputChannel(
                    classification = ControlClassification.DATA_READOUT,
                    actionKey = "READOUT_F",
                    signalKey = "SHIELD_FRONT_L0",
                    label = "F",
                    relativeTo = "SHIELD_REINFORCE",
                    relX = -50f, relY = 150f, width = 60f, height = 40f
                ),
                InputChannel(
                    classification = ControlClassification.DATA_READOUT,
                    actionKey = "READOUT_R",
                    signalKey = "SHIELD_REAR_L0",
                    label = "R",
                    relativeTo = "SHIELD_REINFORCE",
                    relX = -50f, relY = -50f, width = 60f, height = 40f
                )
            )
        )
    }
}
