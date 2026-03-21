package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.srw.seedofsov.ecs.components.PlayerComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Ensures the camera precisely follows the player entity.
 * Priority 30: Ensures it runs after physics integration but BEFORE rendering.
 */
class CameraSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class).get(), 30) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return

        // Update camera to the current frame's position
        camera.position.set(transform.position.x, transform.position.y, 0f)
        camera.update()
    }
}
