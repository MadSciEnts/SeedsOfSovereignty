package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.component.*

/**
 * Procedural generation utility for ship modules and salvage rewards.
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

        // Task Fix: Inject Control DNA based on Tier
        val controlType = if (tier >= 4) ControlType.DIRECTIONAL_STICK else ControlType.BUTTON

        val module = WeaponModuleComponent(
            name = name, description = desc, projectileTier = tier, rarity = rarity,
            controlType = controlType,
            projectileSpeed = 950f + (tier * 60f), fireRate = fireRate, effects = mutableListOf()
        )

        applyTheme(module, theme, damage, rarityMult)
        return module
    }

    fun generateDroneModule(tier: Int, rarity: Rarity = rollRarity()): DroneModuleComponent {
        val theme = rollTheme()
        val rarityMult = getRarityMultiplier(rarity)

        val droneCount = when {
            tier >= 13 -> 16; tier == 12 -> 14; tier == 11 -> 12; tier == 10 -> 10
            tier == 9 -> 8; tier == 8 -> 6; tier == 7 -> 4; tier == 6 -> 2
            tier == 5 -> 1; else -> 1
        }

        return DroneModuleComponent(
            name = "GUARDIAN ${tier}${rarity.name[0]}",
            category = if (theme == Theme.ION) DroneCategory.DEFEND else DroneCategory.ATTACK,
            maxHealth = (50f + tier * 20f) * rarityMult,
            maxDrones = droneCount,
            rarity = rarity,
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
