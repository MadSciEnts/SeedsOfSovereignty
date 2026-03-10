package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.component.*

/**
 * WeaponGenerator (Phase 9.6): Extracted procedural weapon generation logic.
 */
object WeaponGenerator {

    enum class Theme { KINETIC, ION, ACID, SHIELD_PIERCER, SYSTEM_DISRUPTOR }

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

    fun getRarityMultiplier(rarity: Rarity): Float {
        return when (rarity) {
            Rarity.COMMON -> 1.0f; Rarity.UNCOMMON -> 1.15f
            Rarity.RARE -> 1.3f; Rarity.EPIC -> 1.5f; Rarity.LEGENDARY -> 1.8f
        }
    }

    fun rollTheme(): Theme = Theme.values()[MathUtils.random(Theme.values().size - 1)]

    fun rollRarity(): Rarity {
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
