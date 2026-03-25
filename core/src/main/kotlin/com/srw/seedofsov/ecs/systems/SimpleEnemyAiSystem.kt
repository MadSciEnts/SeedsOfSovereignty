package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.ModuleCategory
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Advanced AI for autonomous platforms and enemies.
 * Truth on Disk: Implements First-Order Linear Lead targeting factoring in target velocity and projectile speed.
 */
class SimpleEnemyAiSystem : IteratingSystem(
    allOf(EnemyComponent::class, ShipComponent::class, TransformComponent::class, PhysicsComponent::class).get(),
    5
) {
    private val playerFamily = allOf(PlayerComponent::class, TransformComponent::class, PhysicsComponent::class).get()
    private val aimVec = Vector2()
    private val tempVec = Vector2()
    private val targetFuturePos = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val ship = entity[ShipComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return

        // 1. Locate Target (Player)
        val player = engine.getEntitiesFor(playerFamily).firstOrNull() ?: return
        val playerTransform = player[TransformComponent.mapper] ?: return
        val playerPhysics = player[PhysicsComponent.mapper] ?: return

        val distToTarget = tempVec.set(playerTransform.position.x, playerTransform.position.y)
            .dst(transform.position.x, transform.position.y)

        if (distToTarget > engagementRange) {
            weaponSlot.getChannel("weapon_aim").valueVector2.set(0f, 0f)
            return
        }

        // 4. Predictive Lead Calculation
        // Time to impact = Distance / Projectile Speed
        val timeToImpact = distToTarget / projectileSpeed

        // Relative velocity lead
        tempVec.set(playerPhysics.velocity).sub(physics.velocity).scl(timeToImpact)
        targetFuturePos.set(playerTransform.position.x, playerTransform.position.y).add(tempVec)

        // 5. Command Aim
        aimVec.set(targetFuturePos.x - transform.position.x,
                   targetFuturePos.y - transform.position.y).nor()

        weaponSlot.getChannel("weapon_aim").valueVector2.set(aimVec)
    }
}
