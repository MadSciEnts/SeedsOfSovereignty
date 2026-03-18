package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color
import kotlin.math.max
import kotlin.math.pow

/**
 * EVOLUTION SYSTEM: Handles sub-tier advancements and structural changes.
 * Adheres to Fleks 2.x context-based syntax and the "Swarm Tactical" scaling model.
 */
class EvolutionSystem : IteratingSystem(
    family { all(EvolutionComponent, PlayerComponent) }
) {
    override fun onTickEntity(entity: Entity) {}

    /**
     * Advances the player to the next sub-tier.
     */
    fun evolvePlayer() {
        world.run {
            family { all(PlayerComponent, EvolutionComponent) }.forEach { entity ->
                val evo = entity[EvolutionComponent]

                val nextSub = when (evo.subTier) {
                    SubTier.A -> SubTier.B
                    SubTier.B -> SubTier.C
                    SubTier.C -> {
                        evo.tier++
                        SubTier.A
                    }
                }
                evo.subTier = nextSub

                regenerateShipVisuals(entity, evo.tier)
                updateShipCapabilities(entity, evo)
            }
        }
    }

    /**
     * Public method to synchronize ship properties manually (Bootstrap).
     * Now correctly triggers visual regeneration.
     */
    fun syncCapabilities(entity: Entity) {
        world.run {
            if (entity.has(EvolutionComponent)) {
                val evo = entity[EvolutionComponent]
                regenerateShipVisuals(entity, evo.tier)
                updateShipCapabilities(entity, evo)
            }
        }
    }

    private fun regenerateShipVisuals(entity: Entity, tier: Int) {
        world.run {
            val hue = (tier * 40f) % 360f
            val tierColor = Color(0f, 0f, 0f, 1f).fromHsv(hue, 0.8f, 1.0f)
            val seed = 12345L + tier

            val faction = if (entity.has(FactionComponent)) entity[FactionComponent].type else FactionType.PLAYER
            val isStation = entity.has(StationComponent)

            val layers = ShipTextureGenerator.generateLayered(seed, tier, tierColor, faction, isStation)

            if (!entity.has(CompositeVisualComponent)) {
                entity.configure { it += CompositeVisualComponent() }
            }

            val composite = entity[CompositeVisualComponent]
            composite.layers.clear()

            layers.forEach { data ->
                composite.layers.add(VisualLayer(
                    region = data.region,
                    color = data.color.cpy(),
                    effect = data.effect, // CORRECTED: Use effect instead of non-existent pulse
                    zIndex = data.zIndex,
                    offsetScale = 1.0f
                ))
            }

            if (!entity.has(TextureRegionComponent)) {
                entity.configure { it += TextureRegionComponent() }
            }
            entity[TextureRegionComponent].region = layers.last().region
        }
    }

    private fun updateShipCapabilities(entity: Entity, evo: EvolutionComponent) {
        world.run {
            val tInEpoch = ((evo.tier - 1) % 12).toFloat() + 1f
            val epochIndex = ((evo.tier - 1) / 12).toDouble()

            val magnitude = 3.0.pow(epochIndex).toFloat()
            val growth = (tInEpoch - 1f) * 0.1f
            val taper = max(0f, tInEpoch - 10f) * 0.2f

            val baseTierScale = (1.0f + growth - taper) * magnitude
            val subTierBonus = evo.subTier.ordinal.toFloat() * 0.02f * magnitude

            // Adjust scale based on entity type (Stations are massive)
            val typeMultiplier = if (entity.has(StationComponent)) 5.0f else 1.0f
            val finalScale = (baseTierScale + subTierBonus) * typeMultiplier

            if (entity.has(TextureRegionComponent)) {
                entity[TextureRegionComponent].scale = finalScale
            }

            // Synchronize Weapons/Modules using modular Launcher component
            family { all(AttachmentComponent, WeaponLauncherComponent) }.forEach { module ->
                val attach = module[AttachmentComponent]
                if (attach.parent == entity) {
                    val launcher = module[WeaponLauncherComponent]
                    launcher.barrelOffsets.clear()

                    val count = evo.subTier.ordinal + 1
                    val step = 0.2f * finalScale
                    val start = -(count - 1) * step / 2f

                    repeat(count) { i ->
                        launcher.barrelOffsets.add(Vector2(start + i * step, 0f))
                    }
                }
            }
        }
    }
}
