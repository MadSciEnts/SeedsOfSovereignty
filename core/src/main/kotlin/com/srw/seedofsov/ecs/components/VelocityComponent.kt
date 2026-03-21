package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class VelocityComponent : Component, Pool.Poolable {
    val velocity = Vector2()
    var speed = 5f // Base units per second for Epoch 1

    override fun reset() {
        velocity.set(0f, 0f)
        speed = 5f
    }

    companion object {
        val mapper = mapperFor<VelocityComponent>()
    }
}
