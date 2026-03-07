package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.EvolutionManager

class CollectionSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, EvolutionComponent) }) {
    private val essenceFamily by lazy { world.family { all(EssenceComponent, PositionComponent, VelocityComponent) } }
    private val pullRadiusSq = 40000f
    private val collectRadiusSq = 900f

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val evolution = entity[EvolutionComponent]

        essenceFamily.forEach { essence ->
            val essencePos = essence[PositionComponent].pos
            val vel = essence[VelocityComponent].v
            val distSq = playerPos.dst2(essencePos)

            if (distSq < pullRadiusSq) {
                val pullForce = com.badlogic.gdx.math.Vector2(playerPos).sub(essencePos).nor().scl(800f * deltaTime)
                vel.add(pullForce)

                if (distSq < collectRadiusSq) {
                    val essComp = essence[EssenceComponent]
                    if (!essComp.isCollected) {
                        essComp.isCollected = true
                        evolution.essenceCollected += essComp.amount
                        world -= essence
                    }
                }
            }
        }
    }
}

class EvolutionSystem(
    private val evolutionManager: EvolutionManager
) : IteratingSystem(family { all(PlayerComponent, EvolutionComponent, WeaponComponent, VelocityComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val evo = entity[EvolutionComponent]
        val weapon = entity[WeaponComponent]

        if (!entity.has(WeaponModuleComponent)) entity.configure { it += WeaponModuleComponent() }

        // Ensure choice component exists for other interactions (like docking/loot)
        if (!entity.has(ChoiceComponent)) entity.configure { it += ChoiceComponent() }

        val choice = entity[ChoiceComponent]

        // SEAMLESS EVOLUTION: No longer blocked by choice modal
        if (evo.essenceCollected >= evo.essenceRequired) {
            evo.subTier++
            evo.essenceCollected = 0
            evo.essenceRequired = (evo.essenceRequired * 1.5f).toInt()

            // Handle Major Tier Threshold
            if (evo.subTier == 4) {
                val nextTier = evo.currentTier + 1
                if (nextTier <= 48) {
                    // Apply the actual hardware change
                    evolutionManager.applyTierBaseline(world, entity, nextTier)

                    // TRIGGER CELEBRATION (v0.1.7)
                    // We add the component which will be picked up by the EvolutionCelebrationSystem
                    entity.configure {
                        it += EvolutionCelebrationComponent(
                            newTier = nextTier,
                            newSubTier = 1,
                            newWeaponDNA = entity[WeaponModuleComponent].copy()
                        )
                    }
                } else {
                    evo.subTier = 3 // Max progression for this build
                }
            } else {
                // Physical barrel increase (1 to 3) for sub-tier upgrades
                weapon.barrels = evo.subTier.coerceIn(1, 3)
                generateCenterOutSequence(weapon)
            }
        }
    }

    private fun generateCenterOutSequence(weapon: WeaponComponent) {
        weapon.barrelSequence.clear()
        when (weapon.barrels) {
            1 -> weapon.barrelSequence.add(0)
            2 -> weapon.barrelSequence.addAll(listOf(0, 1))
            3 -> weapon.barrelSequence.addAll(listOf(1, 0, 2))
            else -> { for (i in 0 until weapon.barrels) weapon.barrelSequence.add(i) }
        }
        weapon.currentBarrel = 0
    }
}
