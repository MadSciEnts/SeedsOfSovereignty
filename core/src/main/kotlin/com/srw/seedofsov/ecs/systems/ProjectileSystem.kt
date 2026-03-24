package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.srw.seedofsov.ecs.components.ProjectileComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages the lifecycle of active projectiles.
 * Truth on Disk: Ensures projectiles expire after their intended lifetime.
 */
class ProjectileSystem : IteratingSystem(allOf(ProjectileComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val proj = entity[ProjectileComponent.mapper] ?: return

        proj.lifetime -= deltaTime
        if (proj.lifetime <= 0f) {
            engine.removeEntity(entity)
        }
    }
}
