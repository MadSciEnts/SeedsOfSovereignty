package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

enum class ProjectileType { KINETIC, ION, PHOTON, PLASMA }

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

/**
 * Physical Trail Data (v3.1.9).
 * ROLE: Stores position history for Ribbon rendering.
 *
 * FIXES:
 * 1. [AMPLITUDE] - Added turbulenceAmplitude to control displacement power independently.
 */
data class TrailComponent(
    val history: MutableList<Vector2> = mutableListOf(),
    var maxLength: Int = 48,
    var width: Float = 1.0f,
    var color: Color = Color.WHITE,
    var turbulenceIntensity: Float = 0.05f, // Frequency/Scale
    var turbulenceAmplitude: Float = 1.0f   // Power/Spread
) : Component<TrailComponent> {
    override fun type() = TrailComponent
    companion object : ComponentType<TrailComponent>()
}

data class TacticalStatsComponent(
    var attackDistance: Float = 50f,
    var maxSpeed: Float = 60f,
    var rotationSpeed: Float = 2.0f,
    var fireRateMult: Float = 1.0f,
    var orbitDrift: Float = 0f,
    var engagementAlpha: Float = 1.0f
) : Component<TacticalStatsComponent> {
    override fun type() = TacticalStatsComponent
    companion object : ComponentType<TacticalStatsComponent>()
}

data class WeaponModuleComponent(
    var name: String = "Stock Kinetic",
    var description: String = "Standard kinetic stream.",
    var type: ProjectileType = ProjectileType.KINETIC,
    var projectileTier: Int = 1,
    var rarity: Rarity = Rarity.COMMON,
    var controlType: ControlType = ControlType.BUTTON,
    var tacticalSchema: TacticalSchema = TacticalSchema(listOf(InputChannel(ControlClassification.ACTION_BUTTON, "FIRE"))),
    var projectileSpeed: Float = 1000f,
    var fireRate: Float = 0.5f,
    var projectileColor: Color = Color.YELLOW,
    var passiveDistanceMod: Float = 1.0f,
    var passiveSpeedMod: Float = 1.0f,
    var passiveTurnMod: Float = 1.0f,
    val effects: MutableList<ProjectileEffect> = mutableListOf(
        ProjectileEffect(EffectType.HULL_DAMAGE, 10f),
        ProjectileEffect(EffectType.SHIELD_DAMAGE, 10f)
    )
) : Component<WeaponModuleComponent> {
    override fun type() = WeaponModuleComponent
    companion object : ComponentType<WeaponModuleComponent>()
}

data class ProjectileComponent(
    var type: ProjectileType = ProjectileType.KINETIC,
    var tier: Int = 1,
    var mass: Float = 1.0f,
    var color: Color = Color.YELLOW,
    val effects: MutableList<ProjectileEffect> = mutableListOf(),
    var ownerIsEnemy: Boolean = false
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

data class EnemyComponent(
    var type: String = "Swarm",
    var state: State = State.HUNTING,
    var aiTimer: Float = 0f
) : Component<EnemyComponent> {
    enum class State { HUNTING, RETREATING, PATROLLING }
    override fun type() = EnemyComponent
    companion object : ComponentType<EnemyComponent>()
}
