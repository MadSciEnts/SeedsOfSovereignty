package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.CoordinateSpace
import ktx.ashley.mapperFor
import java.util.*

/**
 * Data structure for an individual trail segment.
 */
data class TrailSegment(
    val position: Vector2,
    val drift: Vector2 = Vector2(), // Established at birth
    var life: Float = 1.0f
)

/**
 * Stores the path and state of a ribbon trail.
 * Truth on Disk: Supports distance-based emission, coordinate space targeting, and noise scaling.
 */
class TrailComponent : Component, Pool.Poolable {
    val segments = LinkedList<TrailSegment>()
    var maxSegments = 36
    var baseWidth = 0.25f

    // Rendering Context
    var space = CoordinateSpace.WORLD

    // Color Interpolation
    var color = Color(0.2f, 0.6f, 1.0f, 0.4f)
    var targetColor = Color(0.1f, 0.2f, 0.4f, 0.0f)

    // Noise and Turbulence
    var noiseSeed = 0f
    var noiseScale = 1.0f // Multiplier for per-frame waver logic

    // Emission Logic
    val lastEmissionPos = Vector2()
    var emissionDistance = 0.1f
    var emissionDuration = 0f
    var driftVariance = 0f

    // Temporal decay
    var dissipationRate = 0.5f

    // Lifecycle for transient effects
    var isTransient = false
    var isEmitting = true

    override fun reset() {
        segments.clear()
        maxSegments = 36
        baseWidth = 0.25f
        space = CoordinateSpace.WORLD
        color.set(0.2f, 0.6f, 1.0f, 0.4f)
        targetColor.set(0.1f, 0.2f, 0.4f, 0.0f)
        noiseSeed = 0f
        noiseScale = 1.0f
        lastEmissionPos.set(0f, 0f)
        emissionDistance = 0.1f
        emissionDuration = 0f
        driftVariance = 0f
        dissipationRate = 0.5f
        isTransient = false
        isEmitting = true
    }

    companion object {
        val mapper = mapperFor<TrailComponent>()
    }
}
