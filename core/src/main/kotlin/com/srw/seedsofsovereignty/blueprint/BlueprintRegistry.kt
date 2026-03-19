package com.srw.seedsofsovereignty.blueprint

import com.badlogic.gdx.math.Vector2
import com.srw.seedsofsovereignty.component.*

/**
 * PROJECTILE DNA: Atomic damage delivery.
 */
data class ProjectileDNA(
    val damage: Float,
    val speed: Float,
    val size: Float,
    val texture: String = "white",
    val fxMap: Map<ImpactTarget, ImpactFXType> = emptyMap(),
    val afflictions: List<DamageType> = emptyList()
)

/**
 * AI PROFILE: Defines how a unit processes its target input.
 */
enum class AiProfile {
    DIRECT_CONTROL,
    OFFENSIVE,
    DEFENSIVE_WALL,
    POINT_DEFENSE,
    REPAIR
}

/**
 * UNIT DNA: Recursive container for Ships, Drones, and Fighters.
 */
data class UnitDNA(
    val tier: Int,
    val hp: Float, // Maps to base Core integrity
    val speed: Float,
    val texture: String,
    val aiProfile: AiProfile,
    val slots: List<SlotDNA> = emptyList()
)

/**
 * SLOT DNA: Defines physical mounting point on a Unit.
 */
data class SlotDNA(
    val position: Vector2,
    val compatibleTypes: List<ModuleType>,
    val defaultModule: ModuleType? = null,
    val groupId: Int = 0
)

/**
 * MODULE DNA: Link between a slot and a spawned entity (Projectile or Unit).
 */
data class ModuleDNA(
    val type: ModuleType,
    val category: ModuleCategory,
    val spawnDNA: Any? = null, // Can be ProjectileDNA or UnitDNA
    val powerDraw: Float = 10f,
    val integrity: Float = 0f,      // For Hull/Armor modules
    val shieldCapacity: Float = 0f  // For Shield modules
)

/**
 * BLUEPRINT REGISTRY: Central database for all ship and module DNA.
 * Standardized Tier 1A Metrics: 5 HP Core, 1 Damage Projectiles.
 */
object BlueprintRegistry {
    private val unitBlueprints = mutableMapOf<Int, UnitDNA>()
    private val moduleBlueprints = mutableMapOf<ModuleType, ModuleDNA>()

    init {
        registerTier1()
    }

    private fun registerTier1() {
        // 1. Atomic Projectiles - Base T1 Damage is 1
        val basicKinetic = ProjectileDNA(
            damage = 1f,
            speed = 16f,
            size = 1.0f,
            texture = "white"
        )

        // 2. Modules - Default T1 Stats
        moduleBlueprints[ModuleType.KINETIC_CANNON] = ModuleDNA(
            type = ModuleType.KINETIC_CANNON,
            category = ModuleCategory.PRIMARY,
            spawnDNA = basicKinetic
        )

        moduleBlueprints[ModuleType.SHIELD_GEN] = ModuleDNA(
            type = ModuleType.SHIELD_GEN,
            category = ModuleCategory.SHIELDS,
            shieldCapacity = 5f // 5 HP per quadrant advantage for player
        )

        moduleBlueprints[ModuleType.HULL_PLATE] = ModuleDNA(
            type = ModuleType.HULL_PLATE,
            category = ModuleCategory.SECONDARY,
            integrity = 5f // 5 HP per quadrant advantage for player
        )

        moduleBlueprints[ModuleType.ENGINE_IMPULSE] = ModuleDNA(
            type = ModuleType.ENGINE_IMPULSE,
            category = ModuleCategory.POWER,
            powerDraw = 5f
        )

        moduleBlueprints[ModuleType.POWER_CORE] = ModuleDNA(
            type = ModuleType.POWER_CORE,
            category = ModuleCategory.POWER,
            powerDraw = 0f
        )

        // 3. The Unit (T1 Pilot Chassis) - 5 HP Core
        unitBlueprints[1] = UnitDNA(
            tier = 1,
            hp = 5f,
            speed = 15f,
            texture = "ship_t1",
            aiProfile = AiProfile.DIRECT_CONTROL,
            slots = listOf(
                SlotDNA(Vector2(0f, 0f), listOf(ModuleType.POWER_CORE), ModuleType.POWER_CORE, 0),
                SlotDNA(Vector2(0f, -0.3f), listOf(ModuleType.ENGINE_IMPULSE), ModuleType.ENGINE_IMPULSE, 0),
                SlotDNA(Vector2(0f, 0.3f), listOf(ModuleType.KINETIC_CANNON), ModuleType.KINETIC_CANNON, 1),
                SlotDNA(Vector2(0f, 0f), listOf(ModuleType.SHIELD_GEN), ModuleType.SHIELD_GEN, 0),
                SlotDNA(Vector2(0f, 0f), listOf(ModuleType.HULL_PLATE), ModuleType.HULL_PLATE, 0)
            )
        )
    }

    fun getUnit(tier: Int): UnitDNA = unitBlueprints[tier] ?: throw IllegalArgumentException("No Unit DNA for Tier $tier")
    fun getModule(type: ModuleType): ModuleDNA = moduleBlueprints[type] ?: throw IllegalArgumentException("No Module DNA for $type")
}
