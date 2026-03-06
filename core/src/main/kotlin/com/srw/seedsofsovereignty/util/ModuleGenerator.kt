package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.component.*

/**
 * Procedural generation utility for ship modules.
 * Enforces DNA Weapon Projectile Rules (1-7).
 */
object ModuleGenerator {

    private enum class Theme { KINETIC, ION, ACID, SHIELD_PIERCER, SYSTEM_DISRUPTOR }

    /**
     * Generates a unique weapon module for a specific tier.
     * Enforces Rule #1 (Tier Gating) and Rule #2 (Power Clamping).
     */
    fun generateWeapon(tier: Int, rarity: Rarity = rollRarity()): WeaponModuleComponent {
        val theme = rollTheme()
        val baselineDmg = getTierBaseline(tier)

        // Rarity Multiplier: Rule #7 Deviation
        val rarityMult = when(rarity) {
            Rarity.COMMON -> 1.0f
            Rarity.UNCOMMON -> 1.15f
            Rarity.RARE -> 1.35f
            Rarity.EPIC -> 1.6f
            Rarity.LEGENDARY -> 2.0f
        }

        val name = "${rarity.name} ${theme.name.replace("_", " ")} T$tier"
        val desc = generateDescription(theme, tier, rarity)

        val module = WeaponModuleComponent(
            name = name,
            description = desc,
            projectileTier = tier,
            rarity = rarity,
            projectileSpeed = 800f + (tier * 50f) + MathUtils.random(0f, 200f),
            fireRate = 0.8f - (tier * 0.05f) - (rarityMult * 0.05f),
            effects = mutableListOf()
        )

        // Apply Theme-Specific Visuals and Effects (Rule #3, #4, #6)
        when(theme) {
            Theme.KINETIC -> {
                module.projectileColor = Color.YELLOW
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baselineDmg * rarityMult, vfx = ImpactVfxType.SPARK_YELLOW))
                module.effects.add(ProjectileEffect(EffectType.SHIELD_DAMAGE, baselineDmg * 0.5f, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE))
            }
            Theme.ION -> {
                module.projectileColor = Color.VIOLET
                module.effects.add(ProjectileEffect(EffectType.SHIELD_DAMAGE, baselineDmg * 2.5f * rarityMult, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE))
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baselineDmg * 0.2f, vfx = ImpactVfxType.SPARK_RED))
            }
            Theme.ACID -> {
                module.projectileColor = Color.GREEN
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baselineDmg * 0.4f, vfx = ImpactVfxType.ACID_GREEN))
                module.effects.add(ProjectileEffect(EffectType.SPEED_MULTIPLIER, 0.8f - (rarityMult * 0.05f), duration = 3f + rarityMult, vfx = ImpactVfxType.ACID_GREEN))
            }
            Theme.SHIELD_PIERCER -> {
                module.projectileColor = Color.ORANGE
                module.effects.add(ProjectileEffect(EffectType.SHIELD_PIERCE, 1.0f))
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baselineDmg * 0.8f * rarityMult, vfx = ImpactVfxType.SPARK_RED))
            }
            Theme.SYSTEM_DISRUPTOR -> {
                module.projectileColor = Color.CYAN
                module.effects.add(ProjectileEffect(EffectType.COOLDOWN_MULTIPLIER, 1.5f + (rarityMult * 0.2f), duration = 2f + rarityMult, vfx = ImpactVfxType.SPARK_YELLOW))
                module.effects.add(ProjectileEffect(EffectType.HULL_DAMAGE, baselineDmg * 0.3f, vfx = ImpactVfxType.SPARK_YELLOW))
            }
        }

        return module
    }

    private fun getTierBaseline(tier: Int): Float {
        return when(tier) {
            1 -> 10f; 2 -> 25f; 3 -> 15f; 4 -> 40f; 5 -> 35f
            6 -> 45f; 7 -> 60f; 8 -> 100f; 9 -> 150f; else -> 10f
        }
    }

    private fun rollTheme(): Theme = Theme.values()[MathUtils.random(Theme.values().size - 1)]

    private fun rollRarity(): Rarity {
        val roll = MathUtils.random(100)
        return when {
            roll > 95 -> Rarity.LEGENDARY
            roll > 85 -> Rarity.EPIC
            roll > 70 -> Rarity.RARE
            roll > 40 -> Rarity.UNCOMMON
            else -> Rarity.COMMON
        }
    }

    private fun generateDescription(theme: Theme, tier: Int, rarity: Rarity): String {
        return "A ${rarity.name.lowercase()} T$tier module utilizing ${theme.name.lowercase()} technology."
    }
}
