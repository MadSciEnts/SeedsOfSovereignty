package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.blueprint.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator

/**
 * UNIVERSAL FACTORY: The recursive engine for expressing DNA as ECS Entities.
 */
object UniversalFactory {

    /**
     * Spawns a Unit (Ship, Drone, Fighter) from its DNA.
     */
    fun spawnUnit(world: World, dna: UnitDNA, faction: FactionType, position: Vector2 = Vector2()): Entity {
        val multiplier = if (faction == FactionType.PLAYER) 5f else 1f

        val unit = world.entity { entity ->
            entity += PositionComponent(Vector2(position))
            // Base Chassis: Damping/Weight. Performance provided by Engine modules.
            entity += VelocityComponent(v = Vector2(), acceleration = 0f, friction = 2.5f, maxSpeed = 5f)
            entity += FactionComponent(faction)

            val baseHp = dna.hp * multiplier
            entity += ArcedDefenseComponent(
                hullFore = baseHp, hullAft = baseHp, hullPort = baseHp, hullStbd = baseHp
            )

            // Visual DNA
            val factionColor = if (faction == FactionType.PLAYER) Color.CYAN else Color.RED
            val visual = CompositeVisualComponent()
            ShipTextureGenerator.generateLayered(dna.texture.hashCode().toLong(), dna.tier, factionColor, faction).forEach { layer ->
                visual.layers.add(VisualLayer(layer.region, layer.color, 1.0f, 0f, layer.zIndex, layer.effect))
            }
            entity += visual
            entity += TextureRegionComponent(scale = 1.0f)

            if (faction == FactionType.PLAYER) entity += PlayerComponent()
        }

        // --- RECURSIVE SLOT ASSEMBLY ---
        dna.slots.forEach { slotDef ->
            world.entity { slotEntity ->
                val type = slotDef.compatibleTypes.firstOrNull() ?: ModuleType.POWER_CORE
                slotEntity += SlotComponent(slotType = SlotType.CORE, offset = Vector2(slotDef.position))
                slotEntity += AttachmentComponent(parent = unit)
            }

            slotDef.defaultModule?.let { moduleType ->
                val moduleDNA = BlueprintRegistry.getModule(moduleType)
                spawnModule(world, unit, moduleDNA, slotDef.position, faction, multiplier)
            }
        }

        return unit
    }

    /**
     * Spawns a Module and handles its recursive "Expressions".
     */
    fun spawnModule(world: World, parent: Entity, dna: ModuleDNA, offset: Vector2, faction: FactionType, statMultiplier: Float = 1f): Entity {
        val module = world.entity { m ->
            m += PositionComponent()
            m += AttachmentComponent(parent = parent, offset = Vector2(offset))
            m += ModuleComponent(category = dna.category)

            // Standardized Channel Initialization: Only create channels defined in the Registry
            val typeDef = ModuleRegistry.get(dna.type)
            val channels = ModuleChannelsComponent(type = dna.type)

            typeDef.controls.forEach { channels.inputs[it.channelKey] = InputChannelData() }
            typeDef.outputs.forEach { channels.outputs[it.channelKey] = OutputChannelData() }

            m += channels

            m += TextureRegionComponent(
                region = ShipTextureGenerator.generateModuleCard(dna.type.hashCode().toLong(), if (faction == FactionType.ENEMY) Color.RED else Color.GRAY),
                scale = 0.3f,
                zIndex = 5
            )

            if (dna.integrity > 0) {
                val hp = dna.integrity * statMultiplier
                m += ArcedDefenseComponent(hullFore = hp, hullAft = hp, hullPort = hp, hullStbd = hp)
            }

            if (dna.shieldCapacity > 0) {
                val cap = dna.shieldCapacity * statMultiplier
                m += ArcedDefenseComponent(shieldFore = cap, shieldAft = cap, shieldPort = cap, shieldStbd = cap)
            }

            // WEAPON EXPRESSION
            val spawnDNA = dna.spawnDNA
            if (spawnDNA is ProjectileDNA) {
                m += WeaponLauncherComponent(fireRate = 0.5f, barrelOffsets = mutableListOf(Vector2(0f, 0f)))
                m += ProjectilePayloadComponent(blueprint = ProjectileBlueprint(
                    type = ProjectileType.KINETIC, baseDamage = spawnDNA.damage, speed = spawnDNA.speed, tier = 1
                ))
            }
        }

        // --- ENGINE SPECIALIZATION ---
        if (dna.type == ModuleType.ENGINE_IMPULSE) {
            world.run {
                if (parent.has(VelocityComponent)) {
                    val vel = parent[VelocityComponent]
                    // Engine determines the performance profile
                    vel.acceleration = 25f
                    vel.maxSpeed = 10f
                }
            }
            spawnTrail(world, module, if (faction == FactionType.PLAYER) Color.CYAN else Color.ORANGE)
        }

        if (dna.spawnDNA is UnitDNA) {
            spawnUnit(world, dna.spawnDNA as UnitDNA, faction)
        }

        return module
    }

    private fun spawnTrail(world: World, parent: Entity, color: Color) {
        world.entity { e ->
            e += PositionComponent()
            e += AttachmentComponent(parent = parent, offset = Vector2(0f, -0.1f))
            e += TrailComponent(
                maxPoints = 36,
                points = Array(36) { Vector2() },
                jitters = Array(36) { Vector2() },
                ages = FloatArray(36),
                color = color.cpy(),
                width = 0.25f,
                pointLifetime = 1.2f
            )
        }
    }

    /**
     * Spawns a Space Station using DNA logic.
     */
    fun spawnStation(world: World, dna: UnitDNA, faction: FactionType, position: Vector2): Entity {
        val station = world.entity { entity ->
            entity += PositionComponent(Vector2(position))
            entity += VelocityComponent(v = Vector2(), acceleration = 0f, friction = 10f, maxSpeed = 0f)
            entity += FactionComponent(faction)
            entity += StationComponent(maxDefenders = 6, spawnInterval = 5f)

            val factionColor = if (faction == FactionType.PLAYER) Color.LIME else Color.ORANGE
            val visual = CompositeVisualComponent()
            ShipTextureGenerator.generateLayered(dna.texture.hashCode().toLong(), dna.tier, factionColor, faction, isStation = true).forEach { layer ->
                visual.layers.add(VisualLayer(layer.region, layer.color, 1f, 0f, layer.zIndex, layer.effect))
            }
            entity += visual
            entity += TextureRegionComponent(scale = 5.0f)
        }

        dna.slots.forEach { slotDef ->
            slotDef.defaultModule?.let { moduleType ->
                val moduleDNA = BlueprintRegistry.getModule(moduleType)
                spawnModule(world, station, moduleDNA, slotDef.position, faction, 1f)
            }
        }

        return station
    }
}
