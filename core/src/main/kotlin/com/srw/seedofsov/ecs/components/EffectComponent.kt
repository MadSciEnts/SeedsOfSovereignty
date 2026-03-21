package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.EffectType
import com.srw.seedofsov.model.VisualMetadata
import ktx.ashley.mapperFor

/**
 * Component for transient visual effects (muzzle flashes, impacts, debris).
 */
class EffectComponent : Component, Pool.Poolable {
    var type = EffectType.MUZZLE_FLASH
    var timer = 0f
    var maxLifetime = 1f
    var metadata = VisualMetadata()

    // Optional: Reference to a parent entity to follow (e.g. muzzle flash follows barrel)
    var parentId: Int? = null

    override fun reset() {
        timer = 0f
        maxLifetime = 1f
        metadata = VisualMetadata()
        parentId = null
    }

    companion object {
        val mapper = mapperFor<EffectComponent>()
    }
}
