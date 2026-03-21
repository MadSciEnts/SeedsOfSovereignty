package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Stores raw input data from the Command Matrix.
 */
class InputComponent : Component, Pool.Poolable {
    // Raw axis data from -1 to +1
    val impulseAxes = Vector2()

    override fun reset() {
        impulseAxes.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<InputComponent>()
    }
}
