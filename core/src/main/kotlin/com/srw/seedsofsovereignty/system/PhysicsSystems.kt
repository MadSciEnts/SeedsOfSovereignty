package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * MOVE SYSTEM: Handles position and rotation alignment.
 */
class MoveSystem : IteratingSystem(
    family { all(PositionComponent, VelocityComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        if (vel.maxSpeed > 0 && vel.v.len2() > vel.maxSpeed * vel.maxSpeed) {
            vel.v.setLength(vel.maxSpeed)
        }

        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        // ROTATION ALIGNMENT
        if (vel.v.len2() > 0.1f) {
            val targetAngle = vel.v.angleDeg() - 90f
            val alpha = if (vel.friction == 0f) 1.0f else 0.40f

            if (entity.has(TextureRegionComponent)) {
                entity[TextureRegionComponent].rotation = lerpAngle(entity[TextureRegionComponent].rotation, targetAngle, alpha)
            }
        }

        vel.v.scl(1f - (vel.friction * deltaTime))
    }

    private fun lerpAngle(from: Float, to: Float, alpha: Float): Float {
        if (alpha >= 1.0f) return to
        var diff = (to - from) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        return from + diff * alpha
    }
}

/**
 * PROJECTILE SYSTEM: Manages independent projectile lifecycles.
 */
class ProjectileSystem : IteratingSystem(
    family { all(ProjectileComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val projectile = entity[ProjectileComponent]
        projectile.lifeTime -= deltaTime
        if (projectile.lifeTime <= 0f) entity.remove()
    }
}
