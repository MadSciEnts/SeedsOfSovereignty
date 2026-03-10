package com.srw.seedsofsovereignty.util

import com.srw.seedsofsovereignty.component.*

object ArmorGenerator {
    fun generateArmor(tier: Int, rarity: Rarity): ArmorModuleComponent {
        val baseHP = 100f * tier
        val hpMult = when(rarity) {
            Rarity.COMMON -> 1.0f; Rarity.RARE -> 1.4f; Rarity.LEGENDARY -> 2.0f; else -> 1.2f
        }
        return ArmorModuleComponent(
            tier = tier,
            currentHP = baseHP * hpMult,
            maxHP = baseHP * hpMult,
            flatReduction = tier.toFloat()
        )
    }

    fun generateHull(tier: Int, rarity: Rarity): HullModuleComponent {
        val baseHP = 200f * tier
        val hpMult = when(rarity) {
            Rarity.COMMON -> 1.0f; Rarity.RARE -> 1.3f; Rarity.LEGENDARY -> 1.8f; else -> 1.1f
        }
        return HullModuleComponent(
            tier = tier,
            currentHP = baseHP * hpMult,
            maxHP = baseHP * hpMult
        )
    }

    fun getArmorSchema(tier: Int): TacticalSchema {
        return TacticalSchema(
            inputs = listOf(
                InputChannel(
                    actionKey = "ARMOR_STATUS",
                    signalKey = "ARMOR_INTEGRITY",
                    classification = ControlClassification.STATUS_LIGHT,
                    relX = 0f, relY = 0f, width = 50f, height = 50f
                )
            )
        )
    }
}
