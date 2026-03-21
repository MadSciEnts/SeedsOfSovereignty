package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.assets.TextureRegistry
import ktx.ashley.mapperFor

/**
 * Visual representation of an entity.
 * Stability: Reset logic uses TextureRegistry.voidTexture to prevent NPEs in libGDX
 * internal dimension queries during entity pooling.
 */
class SpriteComponent : Component, Pool.Poolable {
    // Initialized with void to prevent first-frame NPEs
    val sprite = Sprite(TextureRegistry.voidTexture)

    override fun reset() {
        // TRUTH ON DISK FIX: Point to synchronous 1x1 texture instead of null region.
        // This satisfies libGDX's internal .getWidth() calls during pooling.
        sprite.setRegion(TextureRegion(TextureRegistry.voidTexture))
        sprite.setColor(1f, 1f, 1f, 1f)
        sprite.setScale(1f, 1f)
        sprite.rotation = 0f
        sprite.setOriginCenter()
    }

    companion object {
        val mapper = mapperFor<SpriteComponent>()
    }
}
