package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class WeaponSystem : IteratingSystem(family { all(WeaponComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val weapon = entity[WeaponComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val angleRad = entity[OrientationComponent].angleRad

        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = evo?.currentTier ?: 1

        if (weapon.cooldown > 0f) {
            weapon.cooldown -= deltaTime
        }

        if (weapon.isFiring && weapon.cooldown <= 0f) {
            weapon.cooldown = weapon.fireRate

            for (i in 0 until weapon.barrels) {
                if (weapon.barrels == 2 && i != weapon.currentBarrel) continue

                world.entity { e ->
                    e += PositionComponent().apply {
                        this.pos.set(pos)
                        if (weapon.barrels >= 2) {
                            val offset = if (i == 0) -10f else if (i == 1) 10f else 0f
                            val ox = MathUtils.cos(angleRad + MathUtils.PI/2f) * offset
                            val oy = MathUtils.sin(angleRad + MathUtils.PI/2f) * offset
                            this.pos.add(ox, oy)
                        }
                    }
                    e += VelocityComponent().apply {
                        this.v.set(1000f, 0f).setAngleRad(angleRad).add(vel)
                    }
                    e += ProjectileComponent(
                        tier = tier,
                        damage = when(tier) {
                            1 -> 10f
                            2 -> 25f
                            3 -> 15f
                            4 -> 40f
                            else -> 10f
                        }
                    )
                    e += LifeSpanComponent(remaining = 1.5f)
                }
            }

            if (weapon.barrels > 1) {
                weapon.currentBarrel = (weapon.currentBarrel + 1) % weapon.barrels
            }
        }
    }
}

class CombatSystem : IteratingSystem(family { all(ProjectileComponent, PositionComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projComp = entity[ProjectileComponent]

        enemyFamily.forEach { enemy ->
            val enemyPos = enemy[PositionComponent].pos
            if (projPos.dst2(enemyPos) < 1600f) {
                val enemyComp = enemy[EnemyComponent]
                enemyComp.health -= projComp.damage
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
    }
}

class DroneSystem : IteratingSystem(family { all(DroneComponent, PositionComponent, OrientationComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }

    override fun onTickEntity(entity: Entity) {
        val drone = entity[DroneComponent]
        val pos = entity[PositionComponent].pos
        val orientation = entity[OrientationComponent]

        drone.orbitAngle += drone.orbitSpeed * deltaTime
        val ownerPos = drone.owner[PositionComponent].pos
        pos.set(
            ownerPos.x + MathUtils.cos(drone.orbitAngle) * drone.orbitRadius,
            ownerPos.y + MathUtils.sin(drone.orbitAngle) * drone.orbitRadius
        )

        var nearestEnemy: Entity? = null
        var minDistSq = Float.MAX_VALUE

        enemyFamily.forEach { enemy ->
            val ePos = enemy[PositionComponent].pos
            val d2 = pos.dst2(ePos)
            if (d2 < minDistSq) {
                minDistSq = d2
                nearestEnemy = enemy
            }
        }

        nearestEnemy?.let { enemy ->
            val ePos = enemy[PositionComponent].pos
            orientation.angleRad = Vector2(ePos).sub(pos).angleRad()

            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = true
            }
        } ?: run {
            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = false
            }
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
