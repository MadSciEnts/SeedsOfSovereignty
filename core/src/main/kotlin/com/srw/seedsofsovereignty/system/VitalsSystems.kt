package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class VitalsSystem : IteratingSystem(family { all(HealthComponent).any(ShieldComponent) }) {
    override fun onTickEntity(entity: Entity) {
        // Shield Recharge Logic
        if (entity.has(ShieldComponent)) {
            val shield = entity[ShieldComponent]
            if (shield.active && shield.current < shield.max) {
                shield.current = (shield.current + shield.rechargeRate * deltaTime).coerceAtMost(shield.max)
            }
        }

        // Death Logic
        val health = entity[HealthComponent]
        if (health.current <= 0f) {
            if (entity.has(PlayerComponent)) {
                health.current = health.max
            } else {
                world -= entity
            }
        }
    }
}

class CollisionDamageSystem : IteratingSystem(family { all(PlayerComponent, PositionComponent, HealthComponent) }) {
    private val enemies = family { all(EnemyComponent, PositionComponent) }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos

        enemies.forEach { enemy ->
            val enemyPos = enemy[PositionComponent].pos
            if (playerPos.dst2(enemyPos) < 900f) {
                applyDamage(entity, 10f, playerPos.x, playerPos.y)
                world -= enemy
            }
        }
    }

    private fun applyDamage(target: Entity, damage: Float, x: Float, y: Float) {
        var remainingDamage = damage

        // Spawn Spark Particles
        world.entity {
            it += PositionComponent().apply { pos.set(x, y) }
            it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = Color.ORANGE, size = 5f)
            it += LifeSpanComponent(remaining = 0.3f)
        }

        if (target.has(ShieldComponent)) {
            val shield = target[ShieldComponent]
            if (shield.active && shield.current > 0) {
                // Spawn Shield Ripple (Using GLOW as proxy for ripple in this build)
                world.entity {
                    it += PositionComponent().apply { pos.set(x, y) }
                    it += ParticleComponent(type = ParticleComponent.Type.GLOW, color = Color.CYAN, size = 40f)
                    it += LifeSpanComponent(remaining = 0.4f)
                }

                val shieldDamage = remainingDamage.coerceAtMost(shield.current)
                shield.current -= shieldDamage
                remainingDamage -= shieldDamage
            }
        }

        if (remainingDamage > 0) {
            val health = target[HealthComponent]
            health.current -= remainingDamage
        }
    }
}
