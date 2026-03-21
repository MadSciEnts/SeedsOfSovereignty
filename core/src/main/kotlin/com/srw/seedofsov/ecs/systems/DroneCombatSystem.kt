package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.assets.StyleRegistry
import com.srw.seedofsov.assets.generators.ProjectileAssetGenerator
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.CoordinateSpace
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import kotlin.math.pow

/**
 * Handles autonomous combat logic for Drones.
 * Truth on Disk: Drones now maintain formation while firing.
 */
class DroneCombatSystem : IteratingSystem(
    allOf(DroneComponent::class, TransformComponent::class, SeekingComponent::class, PhysicsComponent::class).get()
) {
    private val enemyFamily = allOf(EnemyComponent::class, TransformComponent::class).get()
    private val assetGen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)
    private val boltTexture = Texture(assetGen.generateKineticBolt())

    private val tempVec = Vector2()
    private val aimVec = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val drone = entity[DroneComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val seeking = entity[SeekingComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return

        if (drone.fireTimer > 0) {
            drone.fireTimer -= deltaTime
        }

        // 1. Calculate Non-Linear Engagement Range
        val radius = (transform.size.x + transform.size.y) / 4f
        val engagementRange = 15f * radius.pow(0.7f)

        // 2. Scan for targets
        val enemies = engine.getEntitiesFor(enemyFamily)
        var target: Entity? = null
        var minDist = engagementRange

        enemies.forEach { enemy ->
            val eTransform = enemy[TransformComponent.mapper] ?: return@forEach
            val dist = tempVec.set(eTransform.position.x, eTransform.position.y)
                .dst(transform.position.x, transform.position.y)

            if (dist < minDist) {
                minDist = dist
                target = enemy
            }
        }

        // 3. Update Combat Target (for SeekingSystem to handle rotation)
        drone.combatTarget = target

        // 4. Fire Projectiles
        target?.let { enemy ->
            val eTransform = enemy[TransformComponent.mapper] ?: return@let
            aimVec.set(eTransform.position.x - transform.position.x,
                       eTransform.position.y - transform.position.y).nor()

            if (drone.fireTimer <= 0f) {
                spawnDroneProjectile(entity, transform, physics, aimVec)
                drone.fireTimer = drone.fireRate
            }
        }
    }

    private fun spawnDroneProjectile(owner: Entity, transform: TransformComponent, physics: PhysicsComponent, direction: Vector2) {
        engine.entity {
            with<TransformComponent> {
                position.set(transform.position.x, transform.position.y, 0f)
                rotation = direction.angleDeg() - 90f
                size.set(0.15f, 0.4f)
            }
            with<PhysicsComponent> {
                // Inherit velocity and add projectile impulse
                velocity.set(physics.velocity)
                velocity.add(direction.x * 25f, direction.y * 25f)
            }
            with<ProjectileComponent> {
                damage = 5f
                sourceEntityId = owner.hashCode()
                lifetime = 1.0f
            }
            with<SpriteComponent> {
                sprite.set(Sprite(boltTexture))
                sprite.color.set(Color.YELLOW)
            }
        }
    }
}
