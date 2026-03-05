package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import kotlin.math.sqrt

class WeaponSystem : IteratingSystem(family { all(WeaponComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val mountOffsetsBuffer = mutableListOf<Vector2>()

    override fun onTickEntity(entity: Entity) {
        val weapon = entity[WeaponComponent]
        val pos = entity[PositionComponent].pos
        val hullAngle = entity[OrientationComponent].angleRad
        val aimAngle = weapon.aimAngleRad // USE INDEPENDENT AIM

        val isEnemy = entity.has(EnemyComponent)
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = if (isEnemy) (if (entity[EnemyComponent].type == "Reaper") 2 else 1) else (evo?.currentTier ?: 1)
        val subTier = evo?.subTier ?: 1

        if (weapon.cooldown > 0f) {
            weapon.cooldown -= deltaTime
        }

        if (weapon.isFiring && weapon.cooldown <= 0f) {
            weapon.cooldown = weapon.fireRate

            // Get Mount Positions based on Hull DNA
            val shipSize = ProceduralShipDrawer.getShipSize(tier, subTier)
            ProceduralShipDrawer.getMountOffsets(tier, subTier, shipSize, hullAngle, mountOffsetsBuffer)

            mountOffsetsBuffer.forEach { mountOffset ->
                val firePosX = pos.x + mountOffset.x
                val firePosY = pos.y + mountOffset.y

                for (i in 0 until weapon.barrels) {
                    // Staggered fire for twin barrels
                    if (weapon.barrels == 2 && i != weapon.currentBarrel) continue

                    world.entity { e ->
                        e += PositionComponent().apply {
                            this.pos.set(firePosX, firePosY)

                            // Spread logic relative to AIM direction
                            val spreadWidth = if (tier >= 8) 30f else if (tier >= 6) 40f else 20f
                            val lateralOffset = when (weapon.barrels) {
                                2 -> if (i == 0) -10f else 10f
                                3 -> (i - 1) * (spreadWidth / 2f)
                                4 -> (i - 1.5f) * (spreadWidth / 3f)
                                else -> 0f
                            }

                            val ox = MathUtils.cos(aimAngle + MathUtils.PI/2f) * lateralOffset
                            val oy = MathUtils.sin(aimAngle + MathUtils.PI/2f) * lateralOffset
                            this.pos.add(ox, oy)
                        }
                        e += VelocityComponent().apply {
                            val bulletSpeed = if (isEnemy) 600f else 1000f
                            this.v.set(bulletSpeed, 0f).setAngleRad(aimAngle) // PROJECTILE FOLLOWS AIM
                        }
                        e += ProjectileComponent(
                            tier = tier,
                            damage = if (isEnemy) 5f else (if (tier >= 6) 40f else if (tier >= 2) 25f else 10f),
                            ownerIsEnemy = isEnemy
                        )
                        e += LifeSpanComponent(remaining = if (isEnemy) 2.5f else 1.5f)
                    }
                }
            }

            if (weapon.barrels > 1 && weapon.barrels == 2) {
                weapon.currentBarrel = (weapon.currentBarrel + 1) % weapon.barrels
            }
        }
    }
}

class CombatSystem : IteratingSystem(family { all(ProjectileComponent, PositionComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, HealthComponent) } }
    private val droneFamily by lazy { world.family { all(DroneComponent, PositionComponent, HealthComponent) } }

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projComp = entity[ProjectileComponent]

        if (!projComp.ownerIsEnemy) {
            enemyFamily.forEach { enemy ->
                val enemyPos = enemy[PositionComponent].pos
                if (projPos.dst2(enemyPos) < 1600f) {
                    val enemyComp = enemy[EnemyComponent]
                    enemyComp.health -= projComp.damage
                    spawnSparkBurst(projPos.x, projPos.y, com.badlogic.gdx.graphics.Color.WHITE, 8f)
                    world -= entity

                    if (enemyComp.health <= 0f) {
                        val ePos = Vector2(enemyPos)
                        world.entity { e ->
                            e += PositionComponent().apply { pos.set(ePos) }
                            e += VelocityComponent().apply { friction = 0.5f }
                            e += EssenceComponent()
                            e += LifeSpanComponent(remaining = 15f)
                        }
                        world -= enemy
                    }
                    return@forEach
                }
            }
        } else {
            droneFamily.forEach { drone ->
                val dronePos = drone[PositionComponent].pos
                if (projPos.dst2(dronePos) < 600f) {
                    applyDamage(drone, projComp.damage, projPos.x, projPos.y)
                    world -= entity
                    return@forEach
                }
            }

            playerFamily.firstOrNull()?.let { player ->
                val playerPos = player[PositionComponent].pos
                if (projPos.dst2(playerPos) < 900f) {
                    applyDamage(player, projComp.damage, projPos.x, projPos.y)
                    world -= entity
                }
            }
        }
    }

    private fun spawnSparkBurst(x: Float, y: Float, color: com.badlogic.gdx.graphics.Color, baseSize: Float) {
        for (i in 0 until 5) {
            world.entity {
                it += PositionComponent().apply { pos.set(x, y) }
                it += VelocityComponent().apply {
                    v.set(MathUtils.random(-200f, 200f), MathUtils.random(-200f, 200f))
                    friction = 2f
                }
                it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = color, size = baseSize * MathUtils.random(0.5f, 1.5f))
                it += LifeSpanComponent(remaining = MathUtils.random(0.2f, 0.4f))
            }
        }
    }

    private fun applyDamage(target: Entity, damage: Float, x: Float, y: Float) {
        var remainingDamage = damage
        if (target.has(ShieldComponent)) {
            val shield = target[ShieldComponent]
            if (shield.active && shield.current > 0) {
                world.entity {
                    it += PositionComponent().apply { pos.set(x, y) }
                    it += ParticleComponent(type = ParticleComponent.Type.SHIELD_RIPPLE, color = com.badlogic.gdx.graphics.Color.CYAN, size = 40f)
                    it += LifeSpanComponent(remaining = 0.4f)
                }
                val shieldDamage = remainingDamage.coerceAtMost(shield.current)
                shield.current -= shieldDamage
                remainingDamage -= shieldDamage
            }
        }
        if (remainingDamage > 0 && target.has(HealthComponent)) {
            target[HealthComponent].current -= remainingDamage
            spawnSparkBurst(x, y, if (target.has(PlayerComponent)) com.badlogic.gdx.graphics.Color.ORANGE else com.badlogic.gdx.graphics.Color.GOLD, 10f)
        }
    }
}

class LifeSpanSystem : IteratingSystem(family { all(LifeSpanComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val life = entity[LifeSpanComponent]
        life.remaining -= deltaTime
        if (life.remaining <= 0f) {
            world -= entity
        }
    }
}
