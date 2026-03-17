package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * ENGINE MOTION TRAILS (RIBBON BASED)
 */
data class TrailComponent(
    val maxPoints: Int = 36,
    // Circular buffer of pre-allocated points to avoid GC pressure
    val points: Array<Vector2> = Array(36) { Vector2() },
    val jitters: Array<Vector2> = Array(36) { Vector2() }, // Preserved noise per point
    val ages: FloatArray = FloatArray(36),
    var activeCount: Int = 0,
    var nextIndex: Int = 0,

    var dropTimer: Float = 0f,
    var dropInterval: Float = 0.01f, // How often to drop a point
    var pointLifetime: Float = 1.8f, // How long a point stays visible

    val color: Color = Color(0.2f, 0.6f, 1f, 0.8f),
    var width: Float = 0.4f,
    var turbulenceIntensity: Float = 0.15f
) : Component<TrailComponent> {
    override fun type() = TrailComponent
    companion object : ComponentType<TrailComponent>()
}
