package com.srw.seedofsov.ui.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import java.util.*

/**
 * Encapsulates the procedural logic for a "whispy" ribbon trail.
 * Truth on Disk: Decouples outward push from noise scale. Guarded against NaN collapse.
 */
class RibbonPath(
    var maxSegments: Int = 40,
    var baseWidth: Float = 10f,
    var color: Color = Color.SKY.cpy(),
    var targetColor: Color = Color(0.1f, 0.2f, 0.4f, 0f),
    var dissipationRate: Float = 1.0f,
    var noiseScale: Float = 1.0f,
    val origin: Vector2 = Vector2()
) {
    data class Segment(
        val position: Vector2,
        val initialDir: Vector2,
        var life: Float = 1.0f,
        val seed: Float = MathUtils.random(1000f)
    )

    val segments = LinkedList<Segment>()
    var clipBounds: Rectangle? = null
    var magneticTarget: Vector2? = null
    var magneticStrength: Float = 0f

    var basePush: Float = 100f
    var ageAcceleration: Float = 400f

    private var accumulatedTime = 0f
    private val tempVec = Vector2()

    fun update(deltaTime: Float) {
        accumulatedTime += deltaTime
        val iter = segments.iterator()
        while (iter.hasNext()) {
            val segment = iter.next()

            val ageFactor = 1.0f - segment.life
            val pushMagnitude = basePush + (ageFactor * ageAcceleration)

            // Truth on Disk: Guarded steering to prevent NaN pollution
            magneticTarget?.let { target ->
                tempVec.set(target).sub(segment.position)
                if (tempVec.len2() > 0.001f) {
                    tempVec.nor()
                    segment.initialDir.lerp(tempVec, magneticStrength * deltaTime * (1.0f + ageFactor))
                }
            }

            val dist = segment.position.dst(origin)
            val noiseFreq = 0.04f
            val noiseTime = accumulatedTime * 8f
            val turbulenceX = MathUtils.sin(dist * noiseFreq + noiseTime + segment.seed) * 30f * noiseScale
            val turbulenceY = MathUtils.cos(dist * noiseFreq * 1.2f + noiseTime * 0.8f + segment.seed) * 30f * noiseScale

            segment.position.add(
                (segment.initialDir.x * pushMagnitude + turbulenceX) * deltaTime,
                (segment.initialDir.y * pushMagnitude + turbulenceY) * deltaTime
            )

            segment.life -= dissipationRate * deltaTime

            val isOob = clipBounds?.let { !it.contains(segment.position) } ?: false
            if (segment.life <= 0f || isOob) iter.remove()
        }
    }

    fun emit(vx: Float, vy: Float) {
        tempVec.set(vx, vy)
        if (tempVec.len2() < 0.001f) tempVec.set(0f, 1f) else tempVec.nor()

        segments.addFirst(Segment(Vector2(origin), Vector2(tempVec)))
        if (segments.size > maxSegments) segments.removeLast()
    }

    fun clear() {
        segments.clear()
    }
}
