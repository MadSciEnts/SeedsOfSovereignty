package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.Batch
import com.srw.seedofsov.ecs.components.SpriteComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class RenderSystem(
    private val batch: Batch
) : SortedIteratingSystem(
    allOf(TransformComponent::class, SpriteComponent::class).get(),
    compareBy { it[TransformComponent.mapper]?.position?.z ?: 0f }
) {
    override fun update(deltaTime: Float) {
        forceSort() // Ensure Z-order is updated if positions changed
        batch.use {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val spriteComponent = entity[SpriteComponent.mapper] ?: return

        spriteComponent.sprite.run {
            rotation = transform.rotation
            setBounds(
                transform.position.x,
                transform.position.y,
                transform.size.x,
                transform.size.y
            )
            draw(batch)
        }
    }
}
