package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.srw.seedofsov.assets.TextureRegistry
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.CoordinateSpace
import com.srw.seedofsov.model.EffectType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

/**
 * Renders all entities and handles layered rendering for weapon platforms and effects.
 * Truth on Disk: Corrected Reticle rendering to use absolute world positions from BaySystem.
 */
class RenderSystem(
    private val batch: Batch,
    private val viewport: Viewport
) : SortedIteratingSystem(
    allOf(TransformComponent::class, SpriteComponent::class).get(),
    compareBy { it[TransformComponent.mapper]?.position?.z ?: 0f },
    50
) {
    private val tempVec = Vector2()

    override fun update(deltaTime: Float) {
        forceSort()
        viewport.apply()
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return
        val spriteComponent = entity[SpriteComponent.mapper] ?: return
        val sprite = spriteComponent.sprite

        val tex = sprite.texture
        if (tex == null || tex == TextureRegistry.voidTexture) return

        val effect = entity[EffectComponent.mapper]

        // --- 1. Pre-Render Logic ---
        val originalAlpha = sprite.color.a
        val originalColor = Color(sprite.color)

        if (effect != null || entity[PlayerComponent.mapper] != null) {
            val flicker = if (MathUtils.random() > 0.95f) 0.7f else 1.0f
            sprite.setAlpha(originalAlpha * flicker)
        }

        // --- 2. Draw Main Sprite ---
        var usedAdditive = false
        when (effect?.type) {
            EffectType.MUZZLE_FLASH, EffectType.SHIELD_IMPACT -> {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
                usedAdditive = true
                val pulse = if (effect.maxLifetime > 0) effect.timer / effect.maxLifetime else 1f
                sprite.setScale(pulse * 1.2f)
                if (effect.type == EffectType.SHIELD_IMPACT) sprite.color = Color.CYAN
            }
            EffectType.DEBRIS -> {
                sprite.color = Color.ORANGE
                sprite.setAlpha(originalAlpha * (if (effect.maxLifetime > 0) effect.timer / effect.maxLifetime else 1f))
            }
            else -> {}
        }

        batch.draw(
            sprite,
            transform.position.x - transform.size.x / 2f,
            transform.position.y - transform.size.y / 2f,
            transform.size.x / 2f,
            transform.size.y / 2f,
            transform.size.x,
            transform.size.y,
            sprite.scaleX,
            sprite.scaleY,
            transform.rotation
        )

        if (usedAdditive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        }
        sprite.color = originalColor
        sprite.setAlpha(originalAlpha)

        // --- 3. Draw Tactical Reticles & Platforms ---
        val ship = entity[ShipComponent.mapper]
        if (ship != null) {
            ship.slots.forEach { slot ->
                val module = slot.equippedModule

                // A. Draw Swarm Reticle
                if (module is BayModuleComponent && !module.commandPosition.isZero(0.15f)) {
                    // worldTargetPos is already the final world coordinate resolved in BaySystem
                    val reticleRotation = if (module.commandSpace == CoordinateSpace.OBJECT) transform.rotation else 0f
                    drawReticle(module.worldTargetPos, reticleRotation)
                }

                // B. Draw Weapon Barrels
                if (module is WeaponModuleComponent && !module.isSecondary) {
                    val platform = entity[WeaponPlatformComponent.mapper]
                    val barrelTexture = platform?.barrelTexture
                    if (barrelTexture != null && barrelTexture != TextureRegistry.voidTexture) {
                        val barrels = module.getBarrelOffsets()
                        val bWidth = module.barrelWidth
                        val bHeight = module.barrelHeight

                        barrels.forEach { offset ->
                            tempVec.set(offset).rotateDeg(module.platformRotation)
                            batch.draw(
                                barrelTexture,
                                transform.position.x + tempVec.x - bWidth / 2f,
                                transform.position.y + tempVec.y - bHeight / 2f,
                                bWidth / 2f, bHeight / 2f, bWidth, bHeight,
                                1f, 1f, module.platformRotation,
                                0, 0, barrelTexture.width, barrelTexture.height,
                                false, false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawReticle(pos: Vector2, rotation: Float) {
        val size = 1.0f
        val thickness = 0.05f
        val pixel = TextureRegistry.voidTexture

        batch.color = Color.CYAN.cpy().apply { a = 0.5f }

        // Rotated crosshair centered on absolute world position
        batch.draw(pixel, pos.x - size/2f, pos.y - thickness/2f, size/2f, thickness/2f, size, thickness, 1f, 1f, rotation, 0, 0, 1, 1, false, false)
        batch.draw(pixel, pos.x - thickness/2f, pos.y - size/2f, thickness/2f, size/2f, thickness, size, 1f, 1f, rotation, 0, 0, 1, 1, false, false)

        batch.color = Color.WHITE
    }
}
