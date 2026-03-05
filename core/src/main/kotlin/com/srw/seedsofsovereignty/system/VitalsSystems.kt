package com.srw.seedsofsovereignty.system

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
                // Handle Player Death (Reset health for now to keep game running)
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
        val playerHealth = entity[HealthComponent]
        val playerShield = if (entity.has(ShieldComponent)) entity[ShieldComponent] else null

        enemies.forEach { enemy ->
            val enemyPos = enemy[PositionComponent].pos
            // Simple circular collision check (30u radius)
            if (playerPos.dst2(enemyPos) < 900f) {
                applyDamage(entity, 10f) // 10 damage on collision
                world -= enemy // Enemy destroyed on impact
            }
        }
    }

    private fun applyDamage(target: Entity, damage: Float) {
        var remainingDamage = damage

        // Apply to shield first
        if (target.has(ShieldComponent)) {
            val shield = target[ShieldComponent]
            if (shield.active && shield.current > 0) {
                val shieldDamage = remainingDamage.coerceAtMost(shield.current)
                shield.current -= shieldDamage
                remainingDamage -= shieldDamage
            }
        }

        // Apply remaining to health
        if (remainingDamage > 0) {
            val health = target[HealthComponent]
            health.current -= remainingDamage
        }
    }
}
