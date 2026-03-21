package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.srw.seedofsov.ecs.components.EnemyComponent
import com.srw.seedofsov.ecs.components.PlayerComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.ui.hud.Hud
import com.srw.seedofsov.ui.hud.RadarDisplay
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Scans the world and updates blips on the Radar Display.
 * Priority 110: Runs after camera tracking is finalized.
 */
class RadarSystem(private val hud: Hud) : IteratingSystem(
    allOf(TransformComponent::class).get(),
    110
) {
    private val playerFamily = allOf(PlayerComponent::class, TransformComponent::class).get()
    private val enemyFamily = allOf(EnemyComponent::class, TransformComponent::class).get()

    // Radar range in world units
    private val radarRange = 50f

    override fun update(deltaTime: Float) {
        val radar = hud.radar
        radar.blips.clear()
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // 1. Get Player Reference for centering
        val player = engine.getEntitiesFor(playerFamily).firstOrNull() ?: return
        val playerPos = player[TransformComponent.mapper]?.position ?: return

        val targetPos = entity[TransformComponent.mapper]?.position ?: return
        val isEnemy = entity[EnemyComponent.mapper] != null
        val isPlayer = entity[PlayerComponent.mapper] != null

        if (!isEnemy && !isPlayer) return

        // 2. Calculate relative position
        val dx = targetPos.x - playerPos.x
        val dy = targetPos.y - playerPos.y
        val dist = com.badlogic.gdx.math.Vector2.len(dx, dy)

        if (dist <= radarRange) {
            // 3. Map to -1 to 1 range for RadarDisplay
            val rx = dx / radarRange
            val ry = dy / radarRange
            hud.radar.blips.add(RadarDisplay.BlipData(rx, ry, isEnemy))
        }
    }
}
