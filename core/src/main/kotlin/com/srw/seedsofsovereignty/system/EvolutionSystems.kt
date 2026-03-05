package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.badlogic.gdx.math.MathUtils

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

        if (evo.essenceCollected >= evo.essenceRequired) {
            evo.subTier++
            evo.essenceCollected = 0
            evo.essenceRequired = (evo.essenceRequired * 1.5f).toInt()

            when (evo.currentTier) {
                1 -> {
                    when (evo.subTier) {
                        2 -> weapon.fireRate = 0.5f
                        3 -> { weapon.fireRate = 0.33f; weapon.barrels = 2 }
                        4 -> { weapon.fireRate = 0.25f; weapon.barrels = 3 }
                        5 -> { evo.currentTier = 2; evo.subTier = 1; weapon.fireRate = 0.2f; weapon.barrels = 1; vel.acceleration += 150f }
                    }
                }
                2 -> {
                    when (evo.subTier) {
                        2 -> weapon.fireRate = 0.15f
                        3 -> { weapon.fireRate = 0.12f; weapon.barrels = 2 }
                        4 -> { weapon.fireRate = 0.1f; weapon.barrels = 4 }
                        5 -> { evo.currentTier = 3; evo.subTier = 1; weapon.barrels = 1; vel.acceleration += 100f }
                    }
                }
                3 -> {
                    val droneCount = droneFamily.numEntities
                    if (droneCount < evo.subTier) {
                        for (i in 0 until (evo.subTier - droneCount)) {
                            world.entity { d ->
                                d += PositionComponent()
                                d += OrientationComponent()
                                d += DroneComponent(owner = entity, orbitAngle = i * MathUtils.PI2 / 4f, orbitRadius = 60f + i * 10f)
                                d += WeaponComponent(fireRate = 0.5f)
                                d += VelocityComponent()
                            }
                        }
                    }

                    if (evo.subTier >= 5) {
                        evo.currentTier = 4
                        evo.subTier = 1
                        weapon.fireRate = 0.1f
                        weapon.barrels = 1
                    }
                }
                4 -> {
                    when (evo.subTier) {
                        2 -> weapon.fireRate = 0.08f
                        3 -> { weapon.fireRate = 0.06f; weapon.barrels = 2 }
                        4 -> { weapon.fireRate = 0.05f; weapon.barrels = 3 }
                        5 -> {
                            evo.currentTier = 5
                            evo.subTier = 1
                            weapon.fireRate = 0.15f
                            weapon.barrels = 2
                            vel.acceleration += 50f
                            if (entity.has(ShieldComponent)) {
                                val shield = entity[ShieldComponent]
                                shield.max = 200f
                                shield.current = shield.max
                                shield.rechargeRate = 15f
                            }
                        }
                    }
                }
                5 -> {
                    when (evo.subTier) {
                        2 -> { if (entity.has(ShieldComponent)) entity[ShieldComponent].max += 50f }
                        3 -> { weapon.fireRate = 0.12f; weapon.barrels = 3 }
                        4 -> { if (entity.has(ShieldComponent)) entity[ShieldComponent].rechargeRate += 5f }
                        5 -> {
                            evo.currentTier = 6
                            evo.subTier = 1
                            weapon.fireRate = 0.1f
                            weapon.barrels = 4
                            vel.acceleration += 25f
                        }
                    }
                }
                6 -> {
                    // Sentinel+ Progression
                    when (evo.subTier) {
                        2 -> { weapon.fireRate = 0.08f }
                        3 -> {
                            // Add a support drone even at this tier
                            world.entity { d ->
                                d += PositionComponent()
                                d += OrientationComponent()
                                d += DroneComponent(owner = entity, orbitAngle = MathUtils.PI, orbitRadius = 80f)
                                d += WeaponComponent(fireRate = 0.4f)
                                d += VelocityComponent()
                            }
                        }
                        4 -> { weapon.fireRate = 0.06f }
                        5 -> {
                            evo.currentTier = 7
                            evo.subTier = 1
                            // Next major evolution transition
                        }
                    }
                }
            }
        }
    }
}
