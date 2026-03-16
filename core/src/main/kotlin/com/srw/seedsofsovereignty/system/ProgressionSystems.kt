package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * EVOLUTION SYSTEM: Handles sub-tier advancements and structural changes.
 */
class EvolutionSystem : IteratingSystem(
    family { all(EvolutionComponent, PlayerComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        // Logic for handling tier-based state changes can go here.
        // For now, it's a placeholder for reactive evolution logic.
    }

    /**
     * Advances the player to the next sub-tier for testing.
     */
    fun evolvePlayer() {
        world.forEach { entity ->
            if (entity has PlayerComponent && entity has EvolutionComponent) {
                val evo = entity[EvolutionComponent]
                val currentSub = evo.subTier

                val nextSub = when (currentSub) {
                    SubTier.A -> SubTier.B
                    SubTier.B -> SubTier.C
                    SubTier.C -> SubTier.D
                    SubTier.D -> {
                        evo.tier++
                        SubTier.A
                    }
                }

                evo.subTier = nextSub

                // Trigger visual/stat updates based on new tier
                updateShipCapabilities(entity, evo)
            }
        }
    }

    private fun updateShipCapabilities(player: Entity, evo: EvolutionComponent) {
        // Example: Update the player's shape size based on Tier
        if (player has ShapeComponent) {
            player[ShapeComponent].size = 1f + (evo.tier - 1) * 0.2f
        }

        // Update all attached weapon modules to match sub-tier for testing
        world.forEach { module ->
            if (module has AttachmentComponent && module[AttachmentComponent].parent == player) {
                if (module has WeaponModuleComponent) {
                    val weapon = module[WeaponModuleComponent]
                    weapon.evolution = evo.subTier

                    // Add barrels based on sub-tier
                    weapon.barrelOffsets.clear()
                    when (evo.subTier) {
                        SubTier.A -> weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0f, 0f))
                        SubTier.B -> {
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(-0.2f, 0f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0.2f, 0f))
                        }
                        SubTier.C -> {
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(-0.3f, 0f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0f, 0.1f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0.3f, 0f))
                        }
                        SubTier.D -> {
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(-0.4f, 0f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(-0.15f, 0.1f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0.15f, 0.1f))
                            weapon.barrelOffsets.add(com.badlogic.gdx.math.Vector2(0.4f, 0f))
                        }
                    }
                }
            }
        }
    }
}
