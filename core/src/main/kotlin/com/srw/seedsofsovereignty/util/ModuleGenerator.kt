package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.component.*

/**
 * Procedural generation utility for ship modules and salvage rewards.
 * Updated (v0.4.0): Populates TacticalSchema DNA using Bidirectional Instrument channels.
 */
object ModuleGenerator {

    private enum class Theme { KINETIC, ION, ACID, SHIELD_PIERCER, SYSTEM_DISRUPTOR }

    fun generateWeapon(tier: Int, rarity: Rarity = rollRarity()): WeaponModuleComponent {
        val theme = rollTheme()
        val targetDPS = getTierDPSBaseline(tier)
        val rarityMult = getRarityMultiplier(rarity)

        val fireRate = (0.6f - (tier * 0.03f)).coerceAtLeast(0.2f)
        val damage = (targetDPS * fireRate) * rarityMult

        val name = "${rarity.name} ${theme.name.replace("_", " ")} T$tier"
        val desc = generateDescription(theme, tier, rarity)

        // Rule #11: Inject Control DNA based on Tier jump (T4+ unlocks independent turrets)
        val controlType = if (tier >= 4) ControlType.DIRECTIONAL_STICK else ControlType.BUTTON

        // TVM Phase 6.1: Assign appropriate TacticalSchema with InputChannel
        val schema = if (tier < 4) {
            TacticalSchema(listOf(InputChannel(ControlClassification.ACTION_BUTTON, "FIRE", relX = 100f, relY = 0f)))
        } else {
            TacticalSchema(listOf(InputChannel(ControlClassification.MOVEMENT_STICK, "AIM", relX = 100f, relY = 0f)))
        }

        val module = WeaponModuleComponent(
            name = name, description = desc, projectileTier = tier, rarity = rarity,
            controlType = controlType,
            tacticalSchema = schema,
            projectileSpeed = 950f + (tier * 60f), fireRate = fireRate, effects = mutableListOf()
        )

        applyTheme(module, theme, damage, rarityMult)
        return module
    }

    fun generateDroneModule(tier: Int, rarity: Rarity = rollRarity(), slotIndex: Int = 0): DroneModuleComponent {
        val theme = rollTheme()
        val rarityMult = getRarityMultiplier(rarity)

        val droneCount = when {
            tier >= 13 -> 16; tier == 12 -> 14; tier == 11 -> 12; tier == 10 -> 10
            tier == 9 -> 8; tier == 8 -> 6; tier == 7 -> 4; tier == 6 -> 2
            tier == 5 -> 1; else -> 1
        }

        // TVM Phase 6.2: Drones use chained instrument model (Stick -> Button -> Readout 1 -> Readout 2)
        // Phase 9.1: Appending slotIndex to actionKeys/signalKeys to prevent crosstalk.
        val schema = TacticalSchema(listOf(
            // 1. Primary Flight Stick (Anchor at 0,0)
            InputChannel(
                classification = ControlClassification.MOVEMENT_STICK,
                actionKey = "DRONE_MOVE_$slotIndex",
                label = "FLIGHT",
                relX = 0f,
                relY = 0f,
                width = 250f,
                height = 250f
            ),
            // 2. Deploy Button (Offset from Stick)
            InputChannel(
                classification = ControlClassification.ACTION_BUTTON,
                actionKey = "DRONE_DEPLOY_$slotIndex",
                label = "DEPLOY",
                relX = 0f,
                relY = 300f, // Positioned above the stick
                width = 150f,
                height = 150f
            ),
            // 3. Active Drones Readout (Offset from Button)
            InputChannel(
                classification = ControlClassification.DATA_READOUT,
                actionKey = "READOUT_ACTIVE_$slotIndex",
                signalKey = "DRONES_ACTIVE_$slotIndex",
                label = "ACTIVE",
                relX = 200f, // Offset to the right of the button
                relY = 0f,
                width = 150f,
                height = 60f
            ),
            // 4. Remaining Drones Readout (Offset from Active Readout)
            InputChannel(
                classification = ControlClassification.DATA_READOUT,
                actionKey = "READOUT_REMAINING_$slotIndex",
                signalKey = "DRONES_REMAINING_$slotIndex",
                label = "RESERVE",
                relX = 0f,
                relY = -80f, // Positioned below Active readout
                width = 150f,
                height = 60f
            )
        ))

        return DroneModuleComponent(
            name = "GUARDIAN ${tier}${rarity.name[0]}",
            category = if (theme == Theme.ION) DroneCategory.DEFEND else DroneCategory.ATTACK,
            maxHealth = (50f + tier * 20f) * rarityMult,
            maxDrones = droneCount,
            rarity = rarity,
            tacticalSchema = schema,
            weaponDNA = generateWeapon(tier, rarity)
        )
    }

    /**
     * Task: Unified Salvage Pipeline.
     * Generates a procedural salvage payload based on enemy difficulty.
     */
    fun generateSalvage(tier: Int): SalvageComponent {
        val rarity = rollRarity()
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

    private fun applyTheme(module: WeaponModuleComponent, theme: Theme, baseDmg: Float, rarityMult: Float) {
        when (theme) {
            Theme.KINETIC -> {
                module.projectileColor = Color.YELLOW
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baseDmg, vfx = ImpactVfxType.SPARK_YELLOW))
                module.effects.add(ProjectileEffect(EffectType.SHIELD_DAMAGE, baseDmg * 0.8f, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE))
            }
            Theme.ION -> {
                module.projectileColor = Color.VIOLET
                module.effects.add(ProjectileEffect(EffectType.SHIELD_DAMAGE, baseDmg * 2.5f, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE))
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baseDmg * 0.4f, vfx = ImpactVfxType.SPARK_RED))
            }
            Theme.ACID -> {
                module.projectileColor = Color.GREEN
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baseDmg * 0.6f, duration = 3f, vfx = ImpactVfxType.ACID_GREEN))
                if (rarityMult > 1.1f) module.effects.add(ProjectileEffect(EffectType.SPEED_MULTIPLIER, 0.75f, duration = 2.5f, vfx = ImpactVfxType.ACID_GREEN))
            }
            Theme.SHIELD_PIERCER -> {
                module.projectileColor = Color.ORANGE
                module.effects.add(ProjectileEffect(EffectType.SHIELD_PIERCE, 1f))
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baseDmg * 0.9f, vfx = ImpactVfxType.SPARK_RED))
            }
            Theme.SYSTEM_DISRUPTOR -> {
                module.projectileColor = Color.CYAN
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baseDmg * 0.5f, vfx = ImpactVfxType.SPARK_YELLOW))
                module.effects.add(ProjectileEffect(EffectType.COOLDOWN_MULTIPLIER, 1.6f, duration = 3f, vfx = ImpactVfxType.SPARK_YELLOW))
            }
        }
    }

    private fun getTierDPSBaseline(tier: Int): Float = 20f * Math.pow(3.2, tier.toDouble() - 1).toFloat()

    private fun getRarityMultiplier(rarity: Rarity): Float {
        return when (rarity) {
            Rarity.COMMON -> 1.0f; Rarity.UNCOMMON -> 1.15f
            Rarity.RARE -> 1.3f; Rarity.EPIC -> 1.5f; Rarity.LEGENDARY -> 1.8f
        }
    }

    private fun rollTheme(): Theme = Theme.values()[MathUtils.random(Theme.values().size - 1)]

    private fun rollRarity(): Rarity {
        val roll = MathUtils.random(100)
        return when {
            roll > 96 -> Rarity.LEGENDARY; roll > 88 -> Rarity.EPIC
            roll > 75 -> Rarity.RARE; roll > 45 -> Rarity.UNCOMMON
            else -> Rarity.COMMON
        }
    }

    private fun generateDescription(theme: Theme, tier: Int, rarity: Rarity): String {
        return "A T$tier ${rarity.name.lowercase()} module using ${theme.name.lowercase().replace("_", " ")} tech."
    }
}
