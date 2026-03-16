package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.TrailComponent

/**
 * TRAIL SYSTEM: Manages engine ribbons and applies world-position noise.
 */
class TrailSystem : IteratingSystem(
    family { all(TrailComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        val currentPos = entity[PositionComponent].pos

        // 1. Age existing points and apply turbulence
        for (i in 0 until trail.maxPoints) {
            if (trail.ages[i] < trail.pointLifetime) {
                trail.ages[i] += deltaTime

                // Turbulence based on world position noise (not sin waves)
                // We use a deterministic hash of the point's world coordinates
                val p = trail.points[i]
                val noiseX = fastNoise(p.x * 0.2f, p.y * 0.2f, 0f)
                val noiseY = fastNoise(p.x * 0.2f, p.y * 0.2f, 1.5f)

                p.x += noiseX * trail.turbulenceIntensity * deltaTime
                p.y += noiseY * trail.turbulenceIntensity * deltaTime
            }
        }

        // 2. Drop new points at engine exhaust
        trail.dropTimer += deltaTime
        if (trail.dropTimer >= trail.dropInterval) {
            trail.dropTimer = 0f

            // Update circular buffer
            val idx = trail.nextIndex
            trail.points[idx].set(currentPos)
            trail.ages[idx] = 0f

            trail.nextIndex = (trail.nextIndex + 1) % trail.maxPoints
            if (trail.activeCount < trail.maxPoints) trail.activeCount++
        }
    }

    /**
     * Fast, deterministic pseudo-random noise based on world coordinates.
     * Avoids simple periodic artifacts of sin/cos.
     */
    private fun fastNoise(x: Float, y: Float, seed: Float): Float {
        val h = (x.toRawBits().toLong() xor (y.toRawBits().toLong() shl 16) xor seed.toRawBits().toLong())
        // Simple 64-bit mixer
        var xh = h
        xh = xh xor (xh ushr 33)
        xh *= -0xae502812aa7333L
        xh = xh xor (xh ushr 33)
        xh *= -0x3b3146010f6d7dL
        xh = xh xor (xh ushr 33)
        return (xh.toFloat() / Long.MAX_VALUE)
    }
}
