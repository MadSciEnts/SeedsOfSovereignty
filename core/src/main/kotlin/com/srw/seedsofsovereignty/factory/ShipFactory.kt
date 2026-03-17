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
 * Stateless factory: Takes World as a parameter to avoid circular dependencies.
 */
class ShipFactory {

    /**
     * Creates the Player Ship.
     */
    fun createPlayer(world: World, tier: Int = 1): Entity {
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += EvolutionComponent(tier = tier, subTier = SubTier.A)
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent(acceleration = 15f, friction = 1.5f)
            entity += PowerCoreComponent()
            entity += ArcedDefenseComponent()
            entity += RadarComponent()
            entity += TextureRegionComponent(scale = 1.0f)
            entity += CompositeVisualComponent()
            entity += TrailComponent()
            entity += FactionComponent(FactionType.PLAYER)
        }

        // Default Primary Module
        createModule(world, ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.3f), FactionType.PLAYER)

        world.system<EvolutionSystem>().syncCapabilities(ship)
        return ship
    }

    /**
     * Creates an Enemy Ship with handicapped combat stats.
     */
    fun createEnemy(world: World, playerTier: Int, isBoss: Boolean = false): Entity {
        val enemyTier = if (isBoss) playerTier + 2 else playerTier

        val ship = world.entity { entity ->
            entity += EvolutionComponent(tier = enemyTier, subTier = SubTier.A)
            entity += PositionComponent(Vector2(5f, 5f))
            entity += VelocityComponent(acceleration = 10f, friction = 1.2f)
            entity += PowerCoreComponent()
            entity += TextureRegionComponent(scale = 1.0f)
            entity += CompositeVisualComponent()
            entity += FactionComponent(FactionType.ENEMY)

            if (isBoss) entity += RadarComponent()
        }

        createModule(world, ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.3f), FactionType.ENEMY)

        world.system<EvolutionSystem>().syncCapabilities(ship)
        return ship
    }

    /**
     * Creates a Neutral Station or Ship.
     */
    fun createNeutral(world: World, tier: Int): Entity {
        val ship = world.entity { entity ->
            entity += EvolutionComponent(tier = tier, subTier = SubTier.A)
            entity += PositionComponent(Vector2(-10f, -10f))
            entity += VelocityComponent(acceleration = 0f, friction = 2.0f)
            entity += TextureRegionComponent(scale = 2.0f)
            entity += CompositeVisualComponent()
            entity += FactionComponent(FactionType.NEUTRAL)
        }
        world.system<EvolutionSystem>().syncCapabilities(ship)
        return ship
    }

    fun createModule(world: World, parent: Entity, category: ModuleCategory, moduleType: ModuleType, offset: Vector2, faction: FactionType): Entity {
        val definition = ModuleRegistry.get(moduleType)

        return world.entity { module ->
            module += PositionComponent()
            module += AttachmentComponent(parent = parent, offset = offset)
            module += ModuleComponent(category = category)

            val channels = ModuleChannelsComponent(type = moduleType)
            definition.controls.forEach { channels.inputs[it.channelKey] = InputChannelData() }
            definition.outputs.forEach { channels.outputs[it.channelKey] = OutputChannelData() }
            module += channels

            module += TextureRegionComponent(
                region = ShipTextureGenerator.generateModuleCard(12345L, if (faction == FactionType.ENEMY) Color.RED else Color.GRAY),
                scale = 0.3f,
                zIndex = 5
            )

            when (moduleType) {
                ModuleType.KINETIC_CANNON -> {
                    val baseRate = 0.5f
                    val rate = if (faction == FactionType.ENEMY) baseRate * 10f else baseRate

                    module += WeaponLauncherComponent(
                        fireRate = rate,
                        barrelOffsets = mutableListOf(Vector2(0f, 0f))
                    )

                    module += ProjectilePayloadComponent(
                        blueprint = ProjectileBlueprint(
                            type = ProjectileType.KINETIC,
                            damageType = DamageType.PHYSICAL,
                            baseDamage = 10f,
                            speed = 8f,
                            color = if (faction == FactionType.ENEMY) Color.ORANGE else Color.YELLOW,
                            tier = 1
                        )
                    )

                    module += BarrelVisualComponent(
                        color = if (faction == FactionType.ENEMY) Color.RED else Color.WHITE,
                        length = 0.3f, width = 0.05f
                    )
                }
                else -> {}
            }
        }
    }
}
