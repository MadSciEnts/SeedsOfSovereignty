package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color

/**
 * EVOLUTION SYSTEM: Handles sub-tier advancements and structural changes.
 * Adheres to Fleks 2.x context-based syntax.
 */
class EvolutionSystem : IteratingSystem(
    family { all(EvolutionComponent, PlayerComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        // Automated evolution logic could live here
    }

    /**
     * Advances the player to the next sub-tier for testing.
     * Tier structure: A -> B -> C -> [Tier + 1, A]
     */
    fun evolvePlayer() {
        world.run {
            family { all(PlayerComponent, EvolutionComponent) }.forEach { entity ->
                val evo = entity.get(EvolutionComponent)

                val nextSub = when (evo.subTier) {
                    SubTier.A -> SubTier.B
                    SubTier.B -> SubTier.C
                    SubTier.C -> {
                        evo.tier++
                        SubTier.A
                    }
                }
                evo.subTier = nextSub

                // Always regenerate visuals to show sub-tier growth/detail changes
                regenerateShipVisuals(entity, evo.tier)
                updateShipCapabilities(entity, evo)
            }
        }
    }

    /**
     * Public method to synchronize ship properties manually (Bootstrap).
     */
    fun syncCapabilities(player: Entity) {
        world.run {
            if (player.has(EvolutionComponent)) {
                val evo = player.get(EvolutionComponent)
                regenerateShipVisuals(player, evo.tier)
                updateShipCapabilities(player, evo)
            }
        }
    }

    private fun regenerateShipVisuals(player: Entity, tier: Int) {
        world.run {
            // Tier Color Shift: Cycle hues per tier
            val hue = (tier * 40f) % 360f
            // BUG FIX: Color().fromHsv() does NOT set alpha. Initialize with alpha = 1.0f.
            val tierColor = Color(0f, 0f, 0f, 1f).fromHsv(hue, 0.8f, 1.0f)

            // Tier Geometry Shift: Seed based on tier
            val seed = 12345L + tier
            val layers = ShipTextureGenerator.generateLayered(seed, tier, tierColor)

            // Ensure entity has CompositeVisualComponent
            if (!player.has(CompositeVisualComponent)) {
                player.configure { it += CompositeVisualComponent() }
            }

            val composite = player.get(CompositeVisualComponent)
            composite.layers.clear()

            layers.forEach { data ->
                // USE NAMED PARAMETERS to avoid positional argument mismatch
                composite.layers.add(VisualLayer(
                    region = data.region,
                    color = data.color.cpy(), // Use cpy to be safe
                    pulse = data.pulse,
                    zIndex = data.zIndex,
                    offsetScale = 1.0f // Explicitly set default
                ))
            }

            // Keep TextureRegionComponent for base scale/rotation data
            if (!player.has(TextureRegionComponent)) {
                player.configure { it += TextureRegionComponent() }
            }
            // Update the legacy region as well
            player.get(TextureRegionComponent).region = layers.last().region
        }
    }

    private fun updateShipCapabilities(player: Entity, evo: EvolutionComponent) {
        world.run {
            val tierGrowth = (evo.tier - 1) * 0.2f
            val subTierBonus = when(evo.subTier) {
                SubTier.A -> 0.0f
                SubTier.B -> 0.05f
                SubTier.C -> 0.1f
            }

            // finalScale is the absolute width in World Units
            val finalScale = 1.0f + tierGrowth + subTierBonus

            if (player.has(TextureRegionComponent)) {
                player.get(TextureRegionComponent).scale = finalScale
            }

            // Update attached Primary Weapon modules
            family { all(AttachmentComponent, WeaponModuleComponent) }.forEach { module ->
                val attach = module.get(AttachmentComponent)
                if (attach.parent == player) {
                    val weapon = module.get(WeaponModuleComponent)
                    weapon.evolution = evo.subTier
                    weapon.barrelIndex = 0
                    weapon.barrelOffsets.clear()

                    // Barrel offsets scale linearly with hull expansion
                    when (evo.subTier) {
                        SubTier.A -> weapon.barrelOffsets.add(Vector2(0f, 0f))
                        SubTier.B -> {
                            weapon.barrelOffsets.add(Vector2(-0.15f * finalScale, 0f))
                            weapon.barrelOffsets.add(Vector2(0.15f * finalScale, 0f))
                        }
                        SubTier.C -> {
                            weapon.barrelOffsets.add(Vector2(-0.25f * finalScale, 0f))
                            weapon.barrelOffsets.add(Vector2(0f, 0f))
                            weapon.barrelOffsets.add(Vector2(0.25f * finalScale, 0f))
                        }
                    }
                }
            }
        }
    }
}
