package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.ImpactFXComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.TrailComponent

/**
 * TRAIL SYSTEM: Manages engine ribbons.
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

        for (i in 0 until trail.maxPoints) {
            if (trail.ages[i] < trail.pointLifetime) {
                trail.ages[i] += deltaTime
            }
        }

        trail.dropTimer += deltaTime
        if (trail.dropTimer >= trail.dropInterval) {
            trail.dropTimer = 0f
            val idx = trail.nextIndex
            trail.points[idx].set(currentPos)
            trail.ages[idx] = 0f
            trail.nextIndex = (trail.nextIndex + 1) % trail.maxPoints
            if (trail.activeCount < trail.maxPoints) trail.activeCount++
        }
    }
}

/**
 * IMPACT FX SYSTEM: Manages transient visual effects (Flares, Sparks).
 */
class ImpactFXSystem : IteratingSystem(
    family { all(ImpactFXComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val fx = entity[ImpactFXComponent]
        fx.lifeTime -= deltaTime
        if (fx.lifeTime <= 0f) entity.remove()
    }
}
