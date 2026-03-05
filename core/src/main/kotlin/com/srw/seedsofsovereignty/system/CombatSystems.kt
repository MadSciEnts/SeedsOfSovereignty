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

        val isEnemy = entity.has(EnemyComponent)
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = if (isEnemy) (if (entity[EnemyComponent].type == "Reaper") 2 else 1) else (evo?.currentTier ?: 1)

        if (weapon.cooldown > 0f) {
            weapon.cooldown -= deltaTime
        }

        // Verified: Now correctly processes isFiring for BOTH player and enemies
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
                        // Slower projectiles for enemies
                        val bulletSpeed = if (isEnemy) 600f else 1000f
                        this.v.set(bulletSpeed, 0f).setAngleRad(angleRad).add(vel)
                    }
                    e += ProjectileComponent(
                        tier = tier,
                        damage = if (isEnemy) 5f else (if (tier >= 2) 25f else 10f),
                        ownerIsEnemy = isEnemy
                    )
                    e += LifeSpanComponent(remaining = if (isEnemy) 2.5f else 1.5f)
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
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, HealthComponent) } }

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projComp = entity[ProjectileComponent]

        if (!projComp.ownerIsEnemy) {
            // Player Projectile hitting Enemy
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
        } else {
            // Enemy Projectile hitting Player
            playerFamily.firstOrNull()?.let { player ->
                val playerPos = player[PositionComponent].pos
                if (projPos.dst2(playerPos) < 900f) {
                    applyDamageToPlayer(player, projComp.damage)
                    world -= entity
                }
            }
        }
    }

    private fun applyDamageToPlayer(player: Entity, damage: Float) {
        var remainingDamage = damage
        if (player.has(ShieldComponent)) {
            val shield = player[ShieldComponent]
            if (shield.active && shield.current > 0) {
                val shieldDamage = remainingDamage.coerceAtMost(shield.current)
                shield.current -= shieldDamage
                remainingDamage -= shieldDamage
            }
        }
        if (remainingDamage > 0) {
            player[HealthComponent].current -= remainingDamage
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
            val enemyComp = enemy[EnemyComponent]
            if (enemyComp.state == EnemyComponent.State.RETREATING) return@forEach

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

class EnemyAISystem : IteratingSystem(family { all(EnemyComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent) } }
    private val stationFamily by lazy { world.family { all(StationComponent, PositionComponent) } }
    private val steerForce = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]
        val enemy = entity[EnemyComponent]
        val orientation = entity[OrientationComponent]

        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val docking = if (player.has(DockingComponent)) player[DockingComponent] else null

        var nearStation = docking?.isDocked == true
        if (!nearStation) {
            stationFamily.forEach { station ->
                if (pos.dst2(station[PositionComponent].pos) < 250000f) nearStation = true
            }
        }

        if (nearStation) {
            enemy.state = EnemyComponent.State.RETREATING
            steerForce.set(pos).sub(playerPos).nor().scl(vel.acceleration * 2f * deltaTime)
            vel.v.add(steerForce)
            vel.v.scl(0.95f)
            if (pos.dst2(playerPos) > 2250000f) world -= entity
        } else {
            enemy.state = EnemyComponent.State.HUNTING
            val distSq = pos.dst2(playerPos)

            // Look at player
            orientation.angleRad = Vector2(playerPos).sub(pos).angleRad()

            // Firing logic: Enabled for T2+ Reapers
            if (entity.has(WeaponComponent)) {
                entity[WeaponComponent].isFiring = distSq < 400000f // 630u range
            }

            if (distSq < 90000f) { // 300u range
                val toPlayer = Vector2(playerPos).sub(pos).nor()
                val tangent = Vector2(-toPlayer.y, toPlayer.x).scl(vel.acceleration * deltaTime)
                vel.v.add(tangent)
            } else {
                steerForce.set(playerPos).sub(pos).nor().scl(vel.acceleration * deltaTime)
                vel.v.add(steerForce)
            }
        }
    }
}
