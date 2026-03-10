package com.srw.seedsofsovereignty.util

import com.srw.seedsofsovereignty.component.*

/**
 * DroneGenerator (Phase 9.8): Hub-Relative Anchoring Refactor.
 * v0.5.6: All secondary controls now anchor to "DRONE_MOVE" hub.
 * Wide Deck Lockdown: Satellite Panel Ergonomics.
 * v0.6.5: Classification fix. Hub is now TACTICAL_STICK to enable persistence.
 */
object DroneGenerator {

    fun generateDroneModule(tier: Int, rarity: Rarity = WeaponGenerator.rollRarity()): DroneModuleComponent {
        val rarityMult = WeaponGenerator.getRarityMultiplier(rarity)

        val droneCount = when {
            tier >= 13 -> 16; tier == 12 -> 14; tier == 11 -> 12; tier == 10 -> 10
            tier == 9 -> 8; tier == 8 -> 6; tier == 7 -> 4; tier == 6 -> 2
            tier == 5 -> 1; else -> 1
        }

        val schema = TacticalSchema(listOf(
            // 1. Primary Flight Stick (Anchor Hub)
            // v0.6.5: Classification upgraded to TACTICAL_STICK to trigger persistence in HUDInputService.
            InputChannel(
                classification = ControlClassification.TACTICAL_STICK,
                actionKey = "DRONE_MOVE",
                label = "FLIGHT",
                relX = 300f,
                relY = 0f,
                width = 250f,
                height = 250f
            ),
            // 2. Deploy Button (Hub-Relative)
            InputChannel(
                classification = ControlClassification.ACTION_BUTTON,
                actionKey = "DRONE_DEPLOY",
                label = "DEPLOY",
                relativeTo = "DRONE_MOVE",
                relX = -120f,
                relY = 130f,
                width = 120f,
                height = 120f
            ),
            // 3. Mode Toggle (Hub-Relative)
            InputChannel(
                classification = ControlClassification.TOGGLE_SWITCH,
                actionKey = "DRONE_MODE",
                signalKey = "DRONE_MODE_SIGNAL",
                label = "MODE",
                relativeTo = "DRONE_MOVE",
                relX = -120f,
                relY = 20f,
                width = 100f,
                height = 100f
            ),
            // 4. Active Drones Readout (Hub-Relative)
            InputChannel(
                classification = ControlClassification.DATA_READOUT,
                actionKey = "READOUT_ACTIVE",
                signalKey = "DRONES_ACTIVE",
                label = "ACTIVE",
                relativeTo = "DRONE_MOVE",
                relX = -280f,
                relY = 130f,
                width = 150f,
                height = 60f
            ),
            // 5. Remaining Drones Readout (Hub-Relative)
            InputChannel(
                classification = ControlClassification.DATA_READOUT,
                actionKey = "READOUT_REMAINING",
                signalKey = "DRONES_REMAINING",
                label = "RESERVE",
                relativeTo = "DRONE_MOVE",
                relX = -280f,
                relY = 70f,
                width = 150f,
                height = 60f
            )
        ))

        return DroneModuleComponent(
            name = "GUARDIAN ${tier}${rarity.name[0]}",
            category = if (WeaponGenerator.rollTheme() == WeaponGenerator.Theme.ION) DroneCategory.DEFEND else DroneCategory.ATTACK,
            maxHealth = (50f + tier * 20f) * rarityMult,
            maxDrones = droneCount,
            rarity = rarity,
            tacticalSchema = schema,
            weaponDNA = WeaponGenerator.generateWeapon(tier, rarity)
        )
    }
}
