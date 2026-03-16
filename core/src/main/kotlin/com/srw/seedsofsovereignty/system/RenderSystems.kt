package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

class WorldRenderSystem(
    private val batch: Batch = inject(),
    private val drawer: ShapeDrawer = inject()
) : IteratingSystem(
    family { all(PositionComponent, ShapeComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val shape = entity[ShapeComponent]
        drawer.setColor(shape.color)
        when (shape.type) {
            ShapeType.TRIANGLE -> {
                drawer.filledTriangle(
                    pos.x, pos.y + shape.size,
                    pos.x - shape.size / 2f, pos.y - shape.size / 2f,
                    pos.x + shape.size / 2f, pos.y - shape.size / 2f
                )
            }
            ShapeType.SQUARE -> drawer.filledRectangle(pos.x - shape.size / 2f, pos.y - shape.size / 2f, shape.size, shape.size)
            ShapeType.CIRCLE -> drawer.filledCircle(pos.x, pos.y, shape.size / 2f)
            ShapeType.HEXAGON -> {}
        }
        if (entity has BarrelVisualComponent && entity has WeaponModuleComponent) {
            val barrel = entity[BarrelVisualComponent]
            val weapon = entity[WeaponModuleComponent]
            drawer.setColor(barrel.color)
            weapon.barrelOffsets.forEach { offset ->
                drawer.filledRectangle(pos.x + offset.x - barrel.width / 2f, pos.y + offset.y, barrel.width, barrel.length)
            }
        }
    }
}

class TrailRenderSystem(
    private val drawer: ShapeDrawer = inject()
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
