package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class SpriteComponent : Component, Pool.Poolable {
    val sprite = Sprite()

    override fun reset() {
        sprite.setRegion(null as com.badlogic.gdx.graphics.g2d.TextureRegion?)
        sprite.setColor(1f, 1f, 1f, 1f)
    }

    companion object {
        val mapper = mapperFor<SpriteComponent>()
    }
}
