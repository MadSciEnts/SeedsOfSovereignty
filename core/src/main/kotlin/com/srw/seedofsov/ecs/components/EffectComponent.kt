package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.EffectType
import com.srw.seedofsov.model.VisualMetadata
import ktx.ashley.mapperFor

/**
 * Component for transient visual effects.
 * Truth on Disk: Supports procedural animation via scale and alpha drift.
 */
class EffectComponent : Component, Pool.Poolable {
    var type = EffectType.MUZZLE_FLASH
    var timer = 0f
    var maxLifetime = 1f
    var metadata = VisualMetadata()

    // Animation Parameters
    var scaleVelocity = 0f // Units per second to grow/shrink
    var autoFade = true    // Automatically fade alpha to 0 over lifetime

    // Parenting Support
    var parentTransform: TransformComponent? = null
    val relativeOffset = Vector2()

    override fun reset() {
        type = EffectType.MUZZLE_FLASH
        timer = 0f
        maxLifetime = 1f
        metadata = VisualMetadata()
        scaleVelocity = 0f
        autoFade = true
        parentTransform = null
        relativeOffset.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<EffectComponent>()
    }
}
