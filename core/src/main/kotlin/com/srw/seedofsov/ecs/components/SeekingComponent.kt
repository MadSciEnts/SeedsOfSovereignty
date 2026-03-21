package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.CoordinateSpace
import ktx.ashley.mapperFor

/**
 * Component for drones or projectiles that track a target.
 * Truth on Disk: Supports velocity inheritance via ownerEntity to prevent 'left behind' behavior.
 */
class SeekingComponent : Component, Pool.Poolable {
    var target: Entity? = null
    var ownerEntity: Entity? = null // Used specifically for velocity inheritance

    val targetPos = Vector2()
    var targetSpace = CoordinateSpace.OBJECT

    var turnRate = 180f
    var arrivalRadius = 0.5f
    var velocityInheritance = 0.8f

    override fun reset() {
        target = null
        ownerEntity = null
        targetPos.set(0f, 0f)
        targetSpace = CoordinateSpace.OBJECT
        turnRate = 180f
        arrivalRadius = 0.5f
        velocityInheritance = 0.8f
    }

    companion object {
        val mapper = mapperFor<SeekingComponent>()
    }
}
