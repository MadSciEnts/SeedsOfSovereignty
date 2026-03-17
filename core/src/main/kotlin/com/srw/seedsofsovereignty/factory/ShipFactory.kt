package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator
import com.srw.seedsofsovereignty.system.EvolutionSystem

/**
 * SHIP FACTORY: Responsible for assembling ship entities and their modules.
 * Centralizes "Ship DNA" for the 48-tier evolution.
 */
class ShipFactory(private val world: World) {

    /**
     * WORLD SCALE: 1 unit = Size of the starting ship (Tier 1 A).
     */
    fun createPlayerTier1(): Entity {
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += EvolutionComponent(tier = 1, subTier = SubTier.A)
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent(acceleration = 15f, friction = 1.5f)
            entity += PowerCoreComponent()
            entity += ArcedDefenseComponent()
            entity += RadarComponent()

            // Initialize with basic texture component for scale tracking
            entity += TextureRegionComponent(scale = 1.0f)

            // Add the new Composite Visual system
            entity += CompositeVisualComponent()

            entity += TrailComponent()
            entity += FactionComponent(FactionType.PLAYER)
        }

        // Initial Primary Module
        createModule(ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.3f))

        // BOOTSTRAP: Sync visual layers and capabilities
        world.system<EvolutionSystem>().syncCapabilities(ship)

        return ship
    }

    fun createModule(parent: Entity, category: ModuleCategory, moduleType: ModuleType, offset: Vector2): Entity {
        val definition = ModuleRegistry.get(moduleType)

        return world.entity { module ->
            module += PositionComponent()
            module += AttachmentComponent(parent = parent, offset = offset)
            module += ModuleComponent(category = category)

            val channels = ModuleChannelsComponent(type = moduleType)
            definition.controls.forEach { channels.inputs[it.channelKey] = InputChannelData() }
            definition.outputs.forEach { channels.outputs[it.channelKey] = OutputChannelData() }
            module += channels

            // Add visual card for the module
            module += TextureRegionComponent(
                region = ShipTextureGenerator.generateModuleCard(12345L, Color.GRAY),
                scale = 0.3f, // Modules are smaller than the hull
                zIndex = 5    // Always on top of the hull layers
            )

            when (moduleType) {
                ModuleType.KINETIC_CANNON -> {
                    module += WeaponModuleComponent(
                        evolution = SubTier.A,
                        barrelOffsets = mutableListOf(Vector2(0f, 0f)),
                        fireRate = 0.5f
                    )
                    module += BarrelVisualComponent(color = Color.WHITE, length = 0.3f, width = 0.05f)

                    module += ProjectilePayloadComponent(
                        blueprint = ProjectileBlueprint(
                            type = ProjectileType.KINETIC,
                            damage = 10f,
                            speed = 10f,
                            color = Color.YELLOW
                        )
                    )
                }
                else -> {}
            }
        }
    }
}
