package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.badlogic.gdx.math.MathUtils
import com.srw.seedsofsovereignty.util.ModuleGenerator

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

class EvolutionSystem : IteratingSystem(family { all(PlayerComponent, EvolutionComponent, WeaponComponent, VelocityComponent) }) {
    private val droneFamily by lazy { world.family { all(DroneComponent) } }

    override fun onTickEntity(entity: Entity) {
        val evo = entity[EvolutionComponent]
        val weapon = entity[WeaponComponent]
        val vel = entity[VelocityComponent]

        if (!entity.has(WeaponModuleComponent)) {
            entity.configure { it += WeaponModuleComponent() }
        }

        if (!entity.has(ChoiceComponent)) {
            entity.configure { it += ChoiceComponent() }
        }

        val choice = entity[ChoiceComponent]

        // Progress check: Only allow collection/subtier logic if NOT currently deciding on a major evolution
        if (!choice.isAwaitingChoice && evo.essenceCollected >= evo.essenceRequired) {
            evo.subTier++
            evo.essenceCollected = 0
            evo.essenceRequired = (evo.essenceRequired * 1.5f).toInt()

            // Handle Major Tier Threshold
            if (evo.subTier == 5) {
                val nextTier = evo.currentTier + 1
                if (nextTier <= 9) {
                    choice.pendingModule = ModuleGenerator.generateWeapon(nextTier, Rarity.COMMON).apply {
                        name = "STOCK T$nextTier"
                        description = "Standard issue hardware for your new chassis."
                    }
                    choice.isAwaitingChoice = true
                    choice.choiceType = ChoiceComponent.ChoiceType.EVOLUTION_UPGRADE
                } else {
                    evo.subTier = 4 // Max Tier Reached for this epoch
                }
            } else {
                // Regular sub-tier physical upgrade
                weapon.barrels = evo.subTier.coerceIn(1, 4)
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
            4 -> weapon.barrelSequence.addAll(listOf(1, 2, 0, 3))
            else -> { for (i in 0 until weapon.barrels) weapon.barrelSequence.add(i) }
        }
        weapon.currentBarrel = 0
    }
}
