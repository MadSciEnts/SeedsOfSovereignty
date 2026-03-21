package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Core component for physical properties and state.
 * Stores linear and angular velocity to be integrated by the PhysicsSystem.
 */
class PhysicsComponent : Component, Pool.Poolable {
    val velocity = Vector2()
    var angularVelocity = 0f

    var mass = 1f
    var friction = 0.5f // Linear damping
    var angularDamping = 0.5f

    override fun reset() {
        velocity.set(0f, 0f)
        angularVelocity = 0f
        mass = 1f
        friction = 0.5f
        angularDamping = 0.5f
    }

    companion object {
        val mapper = mapperFor<PhysicsComponent>()
    }
}
