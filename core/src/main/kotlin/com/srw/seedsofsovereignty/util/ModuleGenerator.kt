package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.component.*

/**
 * Procedural generation utility for ship modules and salvage rewards.
 * Updated (v0.4.0): Populates TacticalSchema DNA using Bidirectional Instrument channels.
 * v0.4.2 (Phase 9.6): Modularized into WeaponGenerator and DroneGenerator.
 */
object ModuleGenerator {

    fun generateWeapon(tier: Int, rarity: Rarity = WeaponGenerator.rollRarity()) =
        WeaponGenerator.generateWeapon(tier, rarity)

    fun generateDroneModule(tier: Int, rarity: Rarity = WeaponGenerator.rollRarity()) =
        DroneGenerator.generateDroneModule(tier, rarity)

    /**
     * Task: Unified Salvage Pipeline.
     * Generates a procedural salvage payload based on enemy difficulty.
     */
    fun generateSalvage(tier: Int): SalvageComponent {
        val rarity = WeaponGenerator.rollRarity()
        val roll = MathUtils.random(100)

        val type = when {
            roll > 95 -> SalvageType.MODULE_CRATE
            roll > 80 -> SalvageType.SCRAP
            else -> SalvageType.ESSENCE
        }

        return SalvageComponent(
            type = type,
            rarity = rarity,
            amount = if (type == SalvageType.ESSENCE) 1 else tier * 5,
            visualScale = when(rarity) {
                Rarity.COMMON -> 1.0f; Rarity.RARE -> 1.4f; Rarity.LEGENDARY -> 2.0f; else -> 1.2f
            }
        )
    }
}
