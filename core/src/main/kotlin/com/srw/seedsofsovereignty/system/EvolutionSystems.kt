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

        if (!entity.has(WeaponModuleComponent)) {
            entity.configure { it += WeaponModuleComponent() }
        }

        if (evo.essenceCollected >= evo.essenceRequired) {
            evo.subTier++
            evo.essenceCollected = 0
            evo.essenceRequired = (evo.essenceRequired * 1.5f).toInt()

            weapon.barrels = evo.subTier.coerceIn(1, 4)
            generateCenterOutSequence(weapon)

            // Force update to Stock Module on every evolution/sub-tier change to maintain power baseline
            if (entity.has(WeaponModuleComponent)) {
                updateStockModule(entity[WeaponModuleComponent], evo.currentTier, evo.subTier)
            }

            when (evo.currentTier) {
                1 -> {
                    if (evo.subTier == 5) {
                        evo.currentTier = 2; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 150f
                        updateStockModule(entity[WeaponModuleComponent], 2, 1)
                    }
                }
                2 -> {
                    if (evo.subTier == 5) {
                        evo.currentTier = 3; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 100f
                        updateStockModule(entity[WeaponModuleComponent], 3, 1)
                    }
                }
                3 -> {
                    val myDrones = droneFamily.filter { it[DroneComponent].owner == entity }
                    if (myDrones.size < evo.subTier) {
                        world.entity { d ->
                            d += PositionComponent()
                            d += OrientationComponent()
                            d += DroneComponent(owner = entity, orbitRadius = 60f + myDrones.size * 10f)
                            d += WeaponComponent()
                            d += WeaponModuleComponent(name = "Drone Pulse", projectileTier = 3, damage = 15f, fireRate = 0.5f)
                            d += VelocityComponent()
                            d += HealthComponent(max = 50f, current = 50f)
                            d += VisibleComponent()
                        }
                    }
                    if (evo.subTier >= 5) {
                        evo.currentTier = 4; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        updateStockModule(entity[WeaponModuleComponent], 4, 1)
                    }
                }
                4 -> {
                    if (evo.subTier == 5) {
                        evo.currentTier = 5; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 50f
                        updateStockModule(entity[WeaponModuleComponent], 5, 1)
                        if (entity.has(ShieldComponent)) {
                            val shield = entity[ShieldComponent]
                            shield.max = 200f; shield.current = shield.max; shield.rechargeRate = 15f
                        }
                    }
                }
                5 -> {
                    if (evo.subTier == 2 && entity.has(ShieldComponent)) entity[ShieldComponent].max += 50f
                    if (evo.subTier == 4 && entity.has(ShieldComponent)) entity[ShieldComponent].rechargeRate += 5f
                    if (evo.subTier == 5) {
                        evo.currentTier = 6; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 25f
                        updateStockModule(entity[WeaponModuleComponent], 6, 1)
                    }
                }
                6 -> {
                    if (evo.subTier == 3) {
                        world.entity { d ->
                            d += PositionComponent()
                            d += OrientationComponent()
                            d += DroneComponent(owner = entity, orbitRadius = 80f)
                            d += WeaponComponent()
                            d += WeaponModuleComponent(name = "Drone Heavy", projectileTier = 6, damage = 30f, fireRate = 0.4f)
                            d += VelocityComponent()
                            d += HealthComponent(max = 100f, current = 100f)
                            d += ShieldComponent(max = 50f, current = 50f, rechargeRate = 5f)
                            d += VisibleComponent()
                        }
                    }
                    if (evo.subTier == 5) {
                        evo.currentTier = 7; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 100f
                        updateStockModule(entity[WeaponModuleComponent], 7, 1)
                    }
                }
                7 -> {
                    if (evo.subTier == 3) vel.acceleration += 200f
                    if (evo.subTier == 5) {
                        evo.currentTier = 8; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration = 250f
                        updateStockModule(entity[WeaponModuleComponent], 8, 1)
                        if (entity.has(HealthComponent)) entity[HealthComponent].apply { max = 1000f; current = 1000f }
                        if (entity.has(ShieldComponent)) entity[ShieldComponent].apply { max = 1000f; current = 1000f; rechargeRate = 30f }
                    }
                }
                8 -> {
                    if (evo.subTier == 2 && entity.has(ShieldComponent)) entity[ShieldComponent].max += 500f
                    if (evo.subTier == 4 && entity.has(ShieldComponent)) entity[ShieldComponent].rechargeRate += 20f
                    if (evo.subTier == 5) {
                        evo.currentTier = 9; evo.subTier = 1; weapon.barrels = 1
                        generateCenterOutSequence(weapon)
                        vel.acceleration += 50f
                        updateStockModule(entity[WeaponModuleComponent], 9, 1)
                    }
                }
            }
        }
    }

    private fun updateStockModule(module: WeaponModuleComponent, tier: Int, subTier: Int) {
        module.projectileTier = tier
        module.name = "Stock T$tier"
        module.projectileSpeed = 1000f

        module.damage = when (tier) {
            1 -> 10f
            2 -> 25f
            3 -> 15f
            4 -> 40f
            5 -> 35f // Slightly increased
            6 -> 45f // Slightly increased
            7 -> 60f // Increased for Purple Bolt
            8 -> 100f
            9 -> 150f
            else -> 10f
        }

        module.fireRate = when (tier) {
            1 -> if (subTier >= 2) 0.5f else 0.6f
            2 -> 0.4f
            3 -> 0.35f
            4 -> 0.3f
            5 -> 0.35f // Tightened
            6 -> 0.25f // Very rapid Square Plasma
            7 -> 0.4f
            8 -> 0.6f
            9 -> 0.4f
            else -> 0.5f
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
