package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.TrailComponent

/**
 * TRAIL SYSTEM: Manages engine ribbons.
 * Samples noise ONCE at emission and stores it for a stable, non-wobbling drift.
 */
class TrailSystem : IteratingSystem(
    family { all(TrailComponent, PositionComponent) }
) {
    private var totalTime = 0f

    override fun onTick() {
        totalTime += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        val currentPos = entity[PositionComponent].pos

        // 1. Age existing points
        for (i in 0 until trail.maxPoints) {
            if (trail.ages[i] < trail.pointLifetime) {
                trail.ages[i] += deltaTime
            }
        }

        // 2. Drop new points at exhaust location
        trail.dropTimer += deltaTime
        if (trail.dropTimer >= trail.dropInterval) {
            trail.dropTimer = 0f

            val idx = trail.nextIndex
            trail.points[idx].set(currentPos)
            trail.ages[idx] = 0f

            // SAMPLE NOISE ONCE AT BIRTH
            // Uses birth coordinates and global time to create a unique vector
            val freq = 2.0f
            val nx = currentPos.x * freq + totalTime * 0.5f
            val ny = currentPos.y * freq + totalTime * 0.5f

            trail.jitters[idx].set(
                smoothNoise(nx, ny),
                smoothNoise(ny, nx + 10f)
            )

            trail.nextIndex = (trail.nextIndex + 1) % trail.maxPoints
            if (trail.activeCount < trail.maxPoints) trail.activeCount++
        }
    }

    private fun smoothNoise(x: Float, y: Float): Float {
        val ix = MathUtils.floor(x); val iy = MathUtils.floor(y)
        val fx = x - ix; val fy = y - iy
        val ux = fx * fx * (3.0f - 2.0f * fx)
        val uy = fy * fy * (3.0f - 2.0f * fy)
        val n00 = hash(ix, iy); val n10 = hash(ix + 1, iy)
        val n01 = hash(ix, iy + 1); val n11 = hash(ix + 1, iy + 1)
        return MathUtils.lerp(MathUtils.lerp(n00, n10, ux), MathUtils.lerp(n01, n11, ux), uy)
    }

    private fun hash(x: Int, y: Int): Float {
        var h = x * 374761393 + y * 668265263
        h = (h xor (h ushr 13)) * 1274126177
        h = h xor (h ushr 16)
        return ((h and 0x7FFFFFFF).toFloat() / 2147483647f) * 2f - 1f
    }
}
