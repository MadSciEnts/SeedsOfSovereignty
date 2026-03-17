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

                regenerateShipVisuals(entity, evo.tier)
                updateShipCapabilities(entity, evo)
            }
        }
    }

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
            val hue = (tier * 40f) % 360f
            val tierColor = Color(0f, 0f, 0f, 1f).fromHsv(hue, 0.8f, 1.0f)
            val seed = 12345L + tier
            val layers = ShipTextureGenerator.generateLayered(seed, tier, tierColor)

            if (!player.has(CompositeVisualComponent)) {
                player.configure { it += CompositeVisualComponent() }
            }

            val composite = player.get(CompositeVisualComponent)
            composite.layers.clear()

            layers.forEach { data ->
                composite.layers.add(VisualLayer(
                    region = data.region,
                    color = data.color.cpy(),
                    pulse = data.pulse,
                    zIndex = data.zIndex,
                    offsetScale = 1.0f
                ))
            }

            if (!player.has(TextureRegionComponent)) {
                player.configure { it += TextureRegionComponent() }
            }
            player.get(TextureRegionComponent).region = layers.last().region
        }
    }

    private fun updateShipCapabilities(player: Entity, evo: EvolutionComponent) {
        world.run {
            // 1. Perspective Math (Ender's Game Swarm Scale)
            val tInEpoch = ((evo.tier - 1) % 12).toFloat() + 1f
            val epochIndex = ((evo.tier - 1) / 12).toDouble()

            /**
             * SWARM ALGORITHM:
             * Magnitude: x3 per Epoch.
             * Growth: +0.1 per tier (Tactical hierarchy).
             * Taper: Net -0.1 reduction at T11 and T12 for "Elite Efficiency".
             */
            val magnitude = 3.0.pow(epochIndex).toFloat()
            val growth = (tInEpoch - 1f) * 0.1f
            val taper = max(0f, tInEpoch - 10f) * 0.2f

            val baseTierScale = (1.0f + growth - taper) * magnitude
            val subTierBonus = evo.subTier.ordinal.toFloat() * 0.02f * magnitude
            val finalScale = baseTierScale + subTierBonus

            if (player.has(TextureRegionComponent)) {
                player.get(TextureRegionComponent).scale = finalScale
            }

            // 2. Synchronize Weapons/Modules using modular Launcher component
            family { all(AttachmentComponent, WeaponLauncherComponent) }.forEach { module ->
                val attach = module[AttachmentComponent]
                if (attach.parent == player) {
                    val launcher = module[WeaponLauncherComponent]
                    launcher.barrelOffsets.clear()

                    // Algorithmic Barrel Spacing (Zero-Condition)
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
