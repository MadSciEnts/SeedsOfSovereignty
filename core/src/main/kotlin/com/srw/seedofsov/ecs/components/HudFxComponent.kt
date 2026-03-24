package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.HudFxType
import ktx.ashley.mapperFor

/**
 * Component for HUD-specific visual effects managed by systems.
 * Truth on Disk: Decouples UI logic from high-fidelity rendering systems.
 */
class HudFxComponent : Component, Pool.Poolable {
    var type = HudFxType.NONE

    // Origin in stage coordinates
    val screenPosition = Vector2()

    // Interaction target in stage coordinates
    val targetPosition = Vector2()

    // Bounds for clipping FX (Stage coordinates)
    val clipBounds = Rectangle()

    // Directional parameters (e.g., 4-quadrant weights)
    val weights = floatArrayOf(0.25f, 0.25f, 0.25f, 0.25f)

    // Target entity (e.g. the ship being monitored)
    var targetEntityId: Int? = null

    override fun reset() {
        type = HudFxType.NONE
        screenPosition.set(0f, 0f)
        targetPosition.set(0f, 0f)
        clipBounds.set(0f, 0f, 0f, 0f)
        for (i in 0..3) weights[i] = 0.25f
        targetEntityId = null
    }

    companion object {
        val mapper = mapperFor<HudFxComponent>()
    }
}
