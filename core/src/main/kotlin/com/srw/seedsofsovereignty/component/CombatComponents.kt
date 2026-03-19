package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color

/**
 * COMBAT & WEAPONRY
 */

enum class DamageType {
    PHYSICAL,   // Balanced
    ENERGY,     // Bonus vs Shields
    PIERCING,   // Ignores/Penetrates Shields
    ACID,       // Lingering DOT
    ELECTRICAL  // Disruptive / Lingering
}

enum class ProjectileStyle { KINETIC, ION, PLASMA }
enum class ImpactTarget { SHIELD, HULL, CORE, ENVIRONMENT }
enum class ImpactFXType { SPARK, FLARE, DETONATION, SIZZLE, GLITCH, NONE }
enum class ImpactQuadrant { FORE, AFT, PORT, STBD }

/**
 * STANDARD CHANNELS: Universal keys for AI and UI interaction.
 */
object StandardChannels {
    const val TARGET = "TARGET"     // Vector2: Aim, Patrol, Move
    const val TRIGGER = "TRIGGER"   // Boolean: Fire, Launch, Deploy
    const val VALUE = "VALUE"       // Float: Power, Throttle, Intensity
    const val MODE = "MODE"         // Float/Int: Toggle, Selection, State
}

/**
 * WEAPON LAUNCHER: The "How" of firing.
 */
data class WeaponLauncherComponent(
    var fireRate: Float = 0.5f,
    var currentCooldown: Float = 0f,
    var burstCount: Int = 1,
    var currentBurst: Int = 0,
    var burstInterval: Float = 0.1f,
    var barrelIndex: Int = 0,
    val barrelOffsets: MutableList<Vector2> = mutableListOf(Vector2(0f, 0f)),
    var preferredRange: Float = 6f
) : Component<WeaponLauncherComponent> {
    override fun type() = WeaponLauncherComponent
    companion object : ComponentType<WeaponLauncherComponent>()
}

/**
 * PROJECTILE BLUEPRINT: The "What" of firing.
 */
data class ProjectileBlueprint(
    val type: ProjectileType,
    val style: ProjectileStyle = ProjectileStyle.KINETIC,
    val damageType: DamageType = DamageType.PHYSICAL,
    val baseDamage: Float,
    val shieldMultiplier: Float = 1.0f,
    val speed: Float,
    val lifeTime: Float = 2f,
    val color: Color = Color.WHITE,
    val tier: Int = 1,
    val minWidthPixels: Float = 2f,
    val impactFX: Map<ImpactTarget, ImpactFXType> = emptyMap()
)

enum class ProjectileType { KINETIC, ENERGY, TORPEDO, DRONE, FIGHTER }

data class ProjectilePayloadComponent(
    var blueprint: ProjectileBlueprint
) : Component<ProjectilePayloadComponent> {
    override fun type() = ProjectilePayloadComponent
    companion object : ComponentType<ProjectilePayloadComponent>()
}

data class DamageComponent(
    var amount: Float = 0f,
    var type: DamageType = DamageType.PHYSICAL,
    var shieldMultiplier: Float = 1.0f,
    var impactFX: Map<ImpactTarget, ImpactFXType> = emptyMap()
) : Component<DamageComponent> {
    override fun type() = DamageComponent
    companion object : ComponentType<DamageComponent>()
}

/**
 * IMPACT COMPONENT: Signal Entity data for damage resolution.
 */
data class ImpactComponent(
    val target: Entity,
    val projectileDamage: DamageComponent,
    val impactPos: Vector2,
    val relativeImpact: Vector2
) : Component<ImpactComponent> {
    override fun type() = ImpactComponent
    companion object : ComponentType<ImpactComponent>()
}

/**
 * RESOLVED IMPACT: Enrichment for ballistics logic.
 */
data class ResolvedImpactComponent(
    var quadrant: ImpactQuadrant = ImpactQuadrant.FORE
) : Component<ResolvedImpactComponent> {
    override fun type() = ResolvedImpactComponent
    companion object : ComponentType<ResolvedImpactComponent>()
}

/**
 * DATA CHANNELS: The "Mailbox" for recursive logic and AI.
 */
data class InputChannelData(
    val vector: Vector2 = Vector2(), // For Targets/Positions
    var value: Float = 0f,           // For Sliders/Throttles
    var triggered: Boolean = false   // For Buttons/Discrete Actions
)

data class OutputChannelData(
    var status: Float = 0f,
    var active: Boolean = false,
    var message: String = ""
)

data class ModuleChannelsComponent(
    val type: ModuleType,
    val inputs: MutableMap<String, InputChannelData> = mutableMapOf<String, InputChannelData>(),
    val outputs: MutableMap<String, OutputChannelData> = mutableMapOf<String, OutputChannelData>()
) : Component<ModuleChannelsComponent> {
    override fun type() = ModuleChannelsComponent
    companion object : ComponentType<ModuleChannelsComponent>()
}

/**
 * STATUS EFFECTS: Components for lingering logic.
 */
data class StatusEffectComponent(
    var type: DamageType,
    var duration: Float,
    var intensity: Float,
    var timer: Float = 0f,
    var lastQuadrant: ImpactQuadrant = ImpactQuadrant.FORE
) : Component<StatusEffectComponent> {
    override fun type() = StatusEffectComponent
    companion object : ComponentType<StatusEffectComponent>()
}

/**
 * LIFECYCLE TAGS
 */
data class ProjectileComponent(
    var lifeTime: Float = 0f,
    var style: ProjectileStyle = ProjectileStyle.KINETIC,
    var minWidthPixels: Float = 2f
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

data class FactionComponent(
    val type: FactionType
) : Component<FactionComponent> {
    override fun type() = FactionComponent
    companion object : ComponentType<FactionComponent>()
}

enum class FactionType { PLAYER, ENEMY, NEUTRAL }

/**
 * IMPACT FX COMPONENT: Transient visual effects spawned on impact.
 */
data class ImpactFXComponent(
    val type: ImpactFXType,
    var lifeTime: Float = 0.3f,
    val color: Color = Color.WHITE
) : Component<ImpactFXComponent> {
    override fun type() = ImpactFXComponent
    companion object : ComponentType<ImpactFXComponent>()
}
