package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Handles durability and centralized reward ejection (v0.1.9).
 */
class VitalsSystem : IteratingSystem(family { all(HealthComponent) }) {
    override fun onTickEntity(entity: Entity) {
        if (entity.has(ShieldComponent)) {
            val shield = entity[ShieldComponent]
            if (shield.active && shield.current < shield.max) {
                shield.current = (shield.current + shield.rechargeRate * deltaTime).coerceAtMost(shield.max)
            }
        }

        val health = entity[HealthComponent]
        if (health.current <= 0f) {
            if (entity.has(PlayerComponent)) {
                health.current = health.max
            } else {
                // Unified Reward Hook
                if (entity.has(EnemyComponent)) {
                    ejectSalvage(entity[PositionComponent].pos, entity[EnemyComponent])
                }
                world -= entity
            }
        }
    }

    private fun ejectSalvage(pos: Vector2, enemy: EnemyComponent) {
        val dropCount = if (enemy.type == "Apex") 3 else 1
        for (i in 0 until dropCount) {
            world.entity { s ->
                s += PositionComponent().apply { this.pos.set(pos) }
                s += VelocityComponent().apply {
                    val angle = com.badlogic.gdx.math.MathUtils.random(com.badlogic.gdx.math.MathUtils.PI2)
                    val speed = com.badlogic.gdx.math.MathUtils.random(50f, 150f)
                    this.v.set(speed, 0f).setAngleRad(angle)
                    this.friction = 1.2f
                }
                s += SalvageComponent(
                    type = SalvageType.ESSENCE,
                    rarity = Rarity.COMMON,
                    amount = 1
                )
                s += VisibleComponent()
                s += LifeSpanComponent(remaining = 30f)
            }
        }
    }
}

class CollisionDamageSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, HealthComponent) }) {
    private val enemies = family { all(EnemyComponent, PositionComponent, HealthComponent) }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        enemies.forEach { enemy ->
            val enemyPos = enemy[PositionComponent].pos
            if (playerPos.dst2(enemyPos) < 1200f) {
                applyDamage(entity, 15f, playerPos.x, playerPos.y)
                enemy[HealthComponent].current = 0f
            }
        }
    }

    private fun applyDamage(target: Entity, damage: Float, x: Float, y: Float) {
        var remainingDamage = damage
        world.entity {
            it += PositionComponent().apply { pos.set(x, y) }
            it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = Color.ORANGE, size = 6f)
            it += LifeSpanComponent(remaining = 0.3f)
        }
        if (target.has(ShieldComponent)) {
            val shield = target[ShieldComponent]
            if (shield.active && shield.current > 0) {
                world.entity {
                    it += PositionComponent().apply { pos.set(x, y) }
                    it += ParticleComponent(type = ParticleComponent.Type.GLOW, color = Color.CYAN, size = 45f)
                    it += LifeSpanComponent(remaining = 0.4f)
                }
                val shieldDamage = remainingDamage.coerceAtMost(shield.current)
                shield.current -= shieldDamage
                remainingDamage -= shieldDamage
            }
        }
        if (remainingDamage > 0) {
            target[HealthComponent].current -= remainingDamage
        }
    }
}
