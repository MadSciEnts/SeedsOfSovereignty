package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator
import com.srw.seedsofsovereignty.system.EvolutionSystem

/**
 * SHIP FACTORY: Responsible for assembling entities based on Faction and Tier.
 */
class ShipFactory {

    fun createPlayer(world: World, tier: Int = 1): Entity {
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += EvolutionComponent(tier = tier, subTier = SubTier.A)
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent(acceleration = 15f, friction = 1.5f)
            entity += RadarComponent(range = 400f)
            entity += VisibleOnRadarComponent(color = Color.CYAN)
            entity += TextureRegionComponent(scale = 1.0f)
            entity += CompositeVisualComponent()
            entity += FactionComponent(FactionType.PLAYER)

            // HUD Summary Components: Added to ensure TacticalRenderSystem (Nautilus) works
            entity += ArcedDefenseComponent()
            entity += PowerCoreComponent()
        }

        createModule(world, ship, ModuleCategory.POWER, ModuleType.POWER_CORE_T1, Vector2(0f, 0f), FactionType.PLAYER)
        createModule(world, ship, ModuleCategory.SECONDARY, ModuleType.HULL_COMPOSITE_T1, Vector2(0f, 0f), FactionType.PLAYER)
        createModule(world, ship, ModuleCategory.SHIELDS, ModuleType.SHIELD_GEN_T1, Vector2(0f, 0f), FactionType.PLAYER)
        createModule(world, ship, ModuleCategory.POWER, ModuleType.ENGINE_IMPULSE_T1, Vector2(0f, -0.3f), FactionType.PLAYER)
        createModule(world, ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.3f), FactionType.PLAYER)

        world.system<EvolutionSystem>().syncCapabilities(ship)
        return ship
    }

    fun createEnemy(world: World, playerTier: Int, isBoss: Boolean = false): Entity {
        val enemyTier = if (isBoss) playerTier + 2 else playerTier

        val ship = world.entity { entity ->
            entity += EvolutionComponent(tier = enemyTier, subTier = SubTier.A)
            entity += PositionComponent(Vector2())
            entity += VelocityComponent(acceleration = 12f, friction = 1.2f)
            entity += VisibleOnRadarComponent(color = if (isBoss) Color.GOLD else Color.RED)
            entity += TextureRegionComponent(scale = 1.0f)
            entity += CompositeVisualComponent()
            entity += FactionComponent(FactionType.ENEMY)
        }

        createModule(world, ship, ModuleCategory.POWER, ModuleType.POWER_CORE_T1, Vector2(0f, 0f), FactionType.ENEMY)
        createModule(world, ship, ModuleCategory.POWER, ModuleType.ENGINE_IMPULSE_T1, Vector2(0f, -0.3f), FactionType.ENEMY)
        createModule(world, ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.3f), FactionType.ENEMY)

        world.system<EvolutionSystem>().syncCapabilities(ship)
        return ship
    }

    fun createStation(world: World, faction: FactionType, tier: Int, pos: Vector2): Entity {
        val station = world.entity { entity ->
            entity += EvolutionComponent(tier = tier, subTier = SubTier.A)
            entity += PositionComponent(Vector2(pos))
            entity += VelocityComponent(acceleration = 0f, friction = 5.0f)
            entity += VisibleOnRadarComponent(color = if (faction == FactionType.ENEMY) Color.ORANGE else Color.GREEN)
            entity += TextureRegionComponent(scale = 5.0f)
            entity += CompositeVisualComponent()
            entity += FactionComponent(faction)
            entity += StationComponent(maxDefenders = 6, spawnInterval = 5f)
        }

        createModule(world, station, ModuleCategory.POWER, ModuleType.POWER_CORE_T1, Vector2(0f, 0f), faction)
        createModule(world, station, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0.5f, 0.5f), faction)
        createModule(world, station, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(-0.5f, -0.5f), faction)

        world.system<EvolutionSystem>().syncCapabilities(station)
        return station
    }

    fun createModule(world: World, parent: Entity, category: ModuleCategory, moduleType: ModuleType, offset: Vector2, faction: FactionType): Entity {
        val definition = ModuleRegistry.get(moduleType)

        val module = world.entity { m ->
            m += PositionComponent()
            m += AttachmentComponent(parent = parent, offset = offset)
            m += ModuleComponent(category = category)

            val channels = ModuleChannelsComponent(type = moduleType)
            definition.controls.forEach { channels.inputs[it.channelKey] = InputChannelData() }
            definition.outputs.forEach { channels.outputs[it.channelKey] = OutputChannelData() }
            m += channels

            m += TextureRegionComponent(
                region = ShipTextureGenerator.generateModuleCard(12345L, if (faction == FactionType.ENEMY) Color.RED else Color.GRAY),
                scale = 0.3f,
                zIndex = 5
            )

            when (moduleType) {
                ModuleType.ENGINE_IMPULSE_T1 -> {
                    val isEnemy = faction == FactionType.ENEMY
                    val trailColor = if (isEnemy) Color(1f, 0.3f, 0.1f, 0.8f) else Color(0.2f, 0.6f, 1f, 0.8f)

                    // Hierarchical Trails: Enemies have half the fidelity and duration
                    createTrail(
                        world, m, Vector2(0f, -0.1f), trailColor,
                        points = if (isEnemy) 18 else 36,
                        life = if (isEnemy) 0.6f else 1.2f
                    )
                }
                ModuleType.KINETIC_CANNON -> {
                    val baseRate = 0.5f
                    val rate = if (faction == FactionType.ENEMY) baseRate * 10f else baseRate
                    m += WeaponLauncherComponent(fireRate = rate, barrelOffsets = mutableListOf(Vector2(0f, 0f)))
                    m += ProjectilePayloadComponent(blueprint = ProjectileBlueprint(
                        type = ProjectileType.KINETIC, baseDamage = 10f, speed = 16f,
                        color = if (faction == FactionType.ENEMY) Color.ORANGE else Color.YELLOW, tier = 1))
                    m += BarrelVisualComponent(color = if (faction == FactionType.ENEMY) Color.RED else Color.WHITE, length = 0.3f, width = 0.05f)
                }
                ModuleType.SHIELD_GEN_T1 -> {
                    m += ArcedDefenseComponent(
                        shieldFore = 50f, shieldAft = 50f, shieldPort = 50f, shieldStbd = 50f,
                        hullFore = 0f, hullAft = 0f, hullPort = 0f, hullStbd = 0f
                    )
                }
                ModuleType.HULL_COMPOSITE_T1 -> {
                    m += ArcedDefenseComponent(
                        shieldFore = 0f, shieldAft = 0f, shieldPort = 0f, shieldStbd = 0f,
                        hullFore = 100f, hullAft = 100f, hullPort = 100f, hullStbd = 100f
                    )
                }
                else -> {}
            }
        }
        return module
    }

    private fun createTrail(world: World, parent: Entity, offset: Vector2, color: Color, points: Int, life: Float) {
        world.entity { e ->
            e += PositionComponent()
            e += AttachmentComponent(parent = parent, offset = offset)
            e += TrailComponent(
                maxPoints = points,
                points = Array(points) { Vector2() },
                jitters = Array(points) { Vector2() },
                ages = FloatArray(points),
                color = color.cpy(),
                width = 0.25f, // Thinner base width
                pointLifetime = life,
                turbulenceIntensity = 0.12f
            )
        }
    }
}
