package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Tag component to identify the player entity.
 */
class PlayerComponent : Component, Pool.Poolable {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
