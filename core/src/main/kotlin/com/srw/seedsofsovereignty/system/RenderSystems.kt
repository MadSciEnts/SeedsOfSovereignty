package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

class WorldRenderSystem(
    private val batch: Batch = inject("batch"),
    private val drawer: ShapeDrawer = inject("drawer")
) : IteratingSystem(
    family { all(PositionComponent, ShapeComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val shape = entity[ShapeComponent]

        // Face direction of motion
        if (entity has VelocityComponent) {
            val vel = entity[VelocityComponent].v
            if (vel.len2() > 0.1f) {
                shape.rotation = vel.angleDeg() - 90f // Base triangle faces UP
            }
        }

        drawer.setColor(shape.color)

        when (shape.type) {
            ShapeType.TRIANGLE -> {
                // Draw rotated triangle
                val r = shape.size
                val angle = shape.rotation

                // Front
                val x1 = pos.x + com.badlogic.gdx.math.MathUtils.cosDeg(angle + 90f) * r
                val y1 = pos.y + com.badlogic.gdx.math.MathUtils.sinDeg(angle + 90f) * r
                // Left
                val x2 = pos.x + com.badlogic.gdx.math.MathUtils.cosDeg(angle + 220f) * r * 0.7f
                val y2 = pos.y + com.badlogic.gdx.math.MathUtils.sinDeg(angle + 220f) * r * 0.7f
                // Right
                val x3 = pos.x + com.badlogic.gdx.math.MathUtils.cosDeg(angle - 40f) * r * 0.7f
                val y3 = pos.y + com.badlogic.gdx.math.MathUtils.sinDeg(angle - 40f) * r * 0.7f

                drawer.filledTriangle(x1, y1, x2, y2, x3, y3)
            }
            ShapeType.SQUARE -> drawer.filledRectangle(pos.x - shape.size / 2f, pos.y - shape.size / 2f, shape.size, shape.size)
            ShapeType.CIRCLE -> drawer.filledCircle(pos.x, pos.y, shape.size / 2f)
            ShapeType.HEXAGON -> {}
        }

        // Draw barrels rotated with ship
        if (entity has BarrelVisualComponent && entity has WeaponModuleComponent) {
            val barrel = entity[BarrelVisualComponent]
            val weapon = entity[WeaponModuleComponent]
            drawer.setColor(barrel.color)

            weapon.barrelOffsets.forEach { offset ->
                // Rotate offset based on ship rotation
                tempVec.set(offset).rotateDeg(shape.rotation)
                val bx = pos.x + tempVec.x
                val by = pos.y + tempVec.y

                drawer.line(bx, by,
                    bx + com.badlogic.gdx.math.MathUtils.cosDeg(shape.rotation + 90f) * barrel.length,
                    by + com.badlogic.gdx.math.MathUtils.sinDeg(shape.rotation + 90f) * barrel.length,
                    barrel.width)
            }
        }
    }
}

class TrailRenderSystem(
    private val drawer: ShapeDrawer = inject("drawer")
) : IteratingSystem(
    family { all(TrailComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        if (trail.activeCount < 2) return
        for (i in 0 until trail.activeCount - 1) {
            val idx1 = (trail.nextIndex - trail.activeCount + i + trail.maxPoints) % trail.maxPoints
            val idx2 = (idx1 + 1) % trail.maxPoints
            val p1 = trail.points[idx1]
            val p2 = trail.points[idx2]
            val age1 = trail.ages[idx1]
            val age2 = trail.ages[idx2]
            val alpha1 = 1f - (age1 / trail.pointLifetime)
            val alpha2 = 1f - (age2 / trail.pointLifetime)
            if (alpha1 > 0 && alpha2 > 0) {
                val color = trail.color.cpy()
                color.a *= (alpha1 + alpha2) / 2f
                drawer.setColor(color)
                drawer.line(p1.x, p1.y, p2.x, p2.y, trail.width)
            }
        }
    }
}
