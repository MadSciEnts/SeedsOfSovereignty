package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.DamageType
import com.srw.seedofsov.model.EffectType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.entity
import ktx.ashley.with

/**
 * Handles projectile-to-ship collisions and applies damage to specific quadrants.
 * Priority 19: Runs after weapons fire but before physics integration.
 */
class CollisionSystem : IteratingSystem(
    allOf(TransformComponent::class, ProjectileComponent::class).get(),
    19
) {
    private val shipMapper = allOf(TransformComponent::class, ShipStatsComponent::class).get()
    private val impactVec = Vector2()

    override fun processEntity(projectile: Entity, deltaTime: Float) {
        val projTransform = projectile[TransformComponent.mapper] ?: return
        val projData = projectile[ProjectileComponent.mapper] ?: return

        // Iterate through all ships to check for hits
        engine.getEntitiesFor(shipMapper).forEach { ship ->
            // Skip the source ship to prevent self-collision on spawn
            if (ship.hashCode() == projData.sourceEntityId) return@forEach

            val shipTransform = ship[TransformComponent.mapper] ?: return@forEach
            val stats = ship[ShipStatsComponent.mapper] ?: return@forEach

            // 2D distance check using Vector3 components
            val dx = projTransform.position.x - shipTransform.position.x
            val dy = projTransform.position.y - shipTransform.position.y
            val distanceSq = dx * dx + dy * dy

            val combinedRadius = (projTransform.size.x + shipTransform.size.x) / 2f
            if (distanceSq < combinedRadius * combinedRadius) {
                handleImpact(projectile, ship, projData, projTransform, shipTransform, stats)
                engine.removeEntity(projectile)
                return
            }
        }
    }

    private fun handleImpact(
        projectile: Entity,
        ship: Entity,
        projData: ProjectileComponent,
        projTransform: TransformComponent,
        shipTransform: TransformComponent,
        stats: ShipStatsComponent
    ) {
        // 1. Calculate Quadrant Relative to Ship Rotation
        impactVec.set(projTransform.position.x - shipTransform.position.x, projTransform.position.y - shipTransform.position.y)
        impactVec.rotateDeg(-shipTransform.rotation)

        val angle = MathUtils.atan2(impactVec.y, impactVec.x) * MathUtils.radiansToDegrees
        val quadrant = when {
            angle in 0f..90f -> 0
            angle in 90f..180f -> 1
            angle in -180f..-90f -> 2
            else -> 3
        }

        // 2. Calculate Damage with Type Modifiers
        var finalDamage = projData.damage
        when (projData.damageType) {
            DamageType.KINETIC -> {
                if (stats.shieldQuadrants[quadrant] > 0) finalDamage *= 0.5f else finalDamage *= 2.0f
            }
            DamageType.ENERGY -> {
                if (stats.shieldQuadrants[quadrant] > 0) finalDamage *= 2.0f else finalDamage *= 0.5f
            }
            else -> {}
        }

        // 3. Apply Damage (Shields first, then Hull)
        if (stats.shieldQuadrants[quadrant] > 0) {
            stats.shieldQuadrants[quadrant] -= finalDamage
            spawnImpactEffect(projTransform.position.x, projTransform.position.y, EffectType.SHIELD_IMPACT)
        } else {
            stats.hullQuadrants[quadrant] -= finalDamage
            spawnImpactEffect(projTransform.position.x, projTransform.position.y, EffectType.DEBRIS)
        }
    }

    private fun spawnImpactEffect(x: Float, y: Float, effectType: EffectType) {
        engine.entity {
            with<TransformComponent> {
                position.set(x, y, 0.2f)
                size.set(0.5f, 0.5f)
            }
            with<EffectComponent> {
                type = effectType
                maxLifetime = 0.3f
                timer = 0.3f
            }
            with<SpriteComponent>()
        }
    }
}
