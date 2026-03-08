package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.Color

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

/**
 * Defines the input method for a module (v0.2.0).
 * Rule #11: Control DNA dictates HUD Command Matrix morphing.
 */
enum class ControlType { BUTTON, DIRECTIONAL_STICK, TACTICAL_FOCUS_STICK }

enum class EffectType {
    HULL_DAMAGE, SHIELD_DAMAGE, SPEED_MULTIPLIER,
    COOLDOWN_MULTIPLIER, SHIELD_PIERCE, REPAIR_HULL
}

enum class ImpactVfxType {
    SPARK_YELLOW, SPARK_RED, SHIELD_RIPPLE_BLUE, ACID_GREEN, REPAIR_GLOW
}

data class ProjectileEffect(
    val type: EffectType,
    val potency: Float,
    val duration: Float = 0f,
    val vfx: ImpactVfxType = ImpactVfxType.SPARK_YELLOW
)

data class WeaponComponent(
    var cooldown: Float = 0f,
    var isFiring: Boolean = false,
    var barrels: Int = 1,
    var currentBarrel: Int = 0,
    var aimAngleRad: Float = MathUtils.PI / 2f,
    val barrelSequence: MutableList<Int> = mutableListOf(0)
) : Component<WeaponComponent> {
    override fun type() = WeaponComponent
    companion object : ComponentType<WeaponComponent>()
}

data class WeaponModuleComponent(
    var name: String = "Stock Kinetic",
    var description: String = "Standard kinetic stream.",
    var projectileTier: Int = 1,
    var rarity: Rarity = Rarity.COMMON,
    var controlType: ControlType = ControlType.BUTTON, // Rule #11: Control DNA
    var projectileSpeed: Float = 1000f,
    var fireRate: Float = 0.5f,
    var projectileColor: Color = Color.YELLOW,
    val effects: MutableList<ProjectileEffect> = mutableListOf(
        ProjectileEffect(EffectType.HULL_DAMAGE, 10f),
        ProjectileEffect(EffectType.SHIELD_DAMAGE, 10f)
    )
) : Component<WeaponModuleComponent> {
    override fun type() = WeaponModuleComponent
    companion object : ComponentType<WeaponModuleComponent>()
}

data class ProjectileComponent(
    var tier: Int = 1,
    var damage: Float = 10f,
    var color: Color = Color.YELLOW,
    val effects: MutableList<ProjectileEffect> = mutableListOf(),
    var ownerIsEnemy: Boolean = false
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

data class EnemyComponent(
    var health: Float = 10f,
    var type: String = "Swarm",
    var state: State = State.HUNTING
) : Component<EnemyComponent> {
    enum class State { HUNTING, RETREATING }
    override fun type() = EnemyComponent
    companion object : ComponentType<EnemyComponent>()
}
