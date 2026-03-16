package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * SHIP FACTORY: Responsible for assembling ship entities and their modules.
 * Centralizes "Ship DNA" for the 48-tier evolution.
 */
class ShipFactory(private val world: World) {

    fun createPlayerTier1(): Entity {
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += EvolutionComponent(tier = 1, subTier = SubTier.A)
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent(acceleration = 15f, friction = 1.5f)
            entity += PowerCoreComponent()
            entity += ArcedDefenseComponent()
            entity += RadarComponent()
            entity += ShapeComponent(type = ShapeType.TRIANGLE, color = Color.CYAN, size = 1f)
            entity += TrailComponent()
            entity += FactionComponent(FactionType.PLAYER)
        }

        // Initial Primary Module: Kinetic Cannon
        createModule(ship, ModuleCategory.PRIMARY, ModuleType.KINETIC_CANNON, Vector2(0f, 0.6f))

        return ship
    }

    fun createModule(parent: Entity, category: ModuleCategory, moduleType: ModuleType, offset: Vector2): Entity {
        val definition = ModuleRegistry.get(moduleType)

        return world.entity { module ->
            module += PositionComponent()
            module += AttachmentComponent(parent = parent, offset = offset)
            module += ModuleComponent(category = category)

            // Initialize Data Channels based on Module Type Definition
            val channels = ModuleChannelsComponent(type = moduleType)

            // 1. Initialize Inputs
            definition.controls.forEach { control ->
                channels.inputs[control.channelKey] = InputChannelData()
            }

            // 2. Initialize Outputs (Crucial for preventing UI crashes)
            definition.outputs.forEach { output ->
                channels.outputs[output.channelKey] = OutputChannelData()
            }

            module += channels

            // Category & Type Specific Logic
            when (moduleType) {
                ModuleType.KINETIC_CANNON -> {
                    module += WeaponModuleComponent(
                        evolution = SubTier.A,
                        barrelOffsets = mutableListOf(Vector2(0f, 0f)),
                        fireRate = 0.2f
                    )
                    module += BarrelVisualComponent(color = Color.WHITE, length = 0.4f, width = 0.1f)

                    // Attach the required Payload Data
                    module += ProjectilePayloadComponent(
                        blueprint = ProjectileBlueprint(
                            type = ProjectileType.KINETIC,
                            damage = 10f,
                            speed = 15f,
                            color = Color.YELLOW
                        )
                    )
                }
                else -> {
                    // Logic for other module types
                }
            }
        }
    }
}
