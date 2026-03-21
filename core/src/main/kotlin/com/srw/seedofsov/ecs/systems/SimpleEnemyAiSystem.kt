package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.EnemyComponent
import com.srw.seedofsov.ecs.components.PlayerComponent
import com.srw.seedofsov.ecs.components.ShipComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.model.ModuleCategory
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.pow

/**
 * Basic AI for stationary enemies.
 * Truth on Disk: Implements range-limited targeting (10x radius).
 */
class SimpleEnemyAiSystem : IteratingSystem(
    allOf(EnemyComponent::class, ShipComponent::class, TransformComponent::class).get(),
    5 // Priority 5: Runs before WeaponSystem
) {
    private val playerFamily = allOf(PlayerComponent::class, TransformComponent::class).get()
    private val aimVec = Vector2()
    private val tempVec = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val ship = entity[ShipComponent.mapper] ?: return

        // 1. Locate Player
        val player = engine.getEntitiesFor(playerFamily).firstOrNull() ?: return
        val playerTransform = player[TransformComponent.mapper] ?: return

        // 2. Check Range (Requirement: 10x enemy ship radius)
        val radius = (transform.size.x + transform.size.y) / 4f
        val engagementRange = radius * 10f

        val distToPlayer = tempVec.set(playerTransform.position.x, playerTransform.position.y)
            .dst(transform.position.x, transform.position.y)

        if (distToPlayer > engagementRange) {
            // Out of range, clear aim
            val weaponSlot = ship.getSlot(ModuleCategory.WEAPON_PRIMARY)
            weaponSlot?.getChannel("weapon_aim")?.valueVector2?.set(0f, 0f)
            return
        }

        // 3. Calculate direction to player
        aimVec.set(playerTransform.position.x - transform.position.x,
                   playerTransform.position.y - transform.position.y).nor()

        // 4. Command the modular weapon slot
        val weaponSlot = ship.getSlot(ModuleCategory.WEAPON_PRIMARY)
        weaponSlot?.getChannel("weapon_aim")?.valueVector2?.set(aimVec)
    }
}
