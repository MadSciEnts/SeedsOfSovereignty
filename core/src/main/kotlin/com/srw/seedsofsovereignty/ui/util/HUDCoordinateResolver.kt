package com.srw.seedsofsovereignty.ui.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.HUDAnchor
import com.srw.seedsofsovereignty.component.HUDElementComponent

/**
 * HUD Coordinate Resolver (v1.0.0)
 * ROLE: Central utility for hierarchical AR positioning.
 */
object HUDCoordinateResolver {
    private val outVec = Vector2()

    fun getAnchorPosition(anchor: HUDAnchor, viewport: Viewport): Vector2 {
        val w = viewport.worldWidth; val h = viewport.worldHeight
        return when (anchor) {
            HUDAnchor.TOP_LEFT -> outVec.set(0f, h)
            HUDAnchor.TOP_RIGHT -> outVec.set(w, h)
            HUDAnchor.BOTTOM_LEFT -> outVec.set(0f, 0f)
            HUDAnchor.BOTTOM_RIGHT -> outVec.set(w, 0f)
            HUDAnchor.CENTER -> outVec.set(w / 2f, h / 2f)
            HUDAnchor.TOP_CENTER -> outVec.set(w / 2f, h)
            HUDAnchor.BOTTOM_CENTER -> outVec.set(w / 2f, 0f)
            HUDAnchor.LEFT_CENTER -> outVec.set(0f, h / 2f)
            HUDAnchor.RIGHT_CENTER -> outVec.set(w, h / 2f)
        }
    }

    fun getAbsolutePosition(entity: Entity, world: World, viewport: Viewport, result: Vector2) {
        result.set(0f, 0f)
        accumulatePosition(entity, world, viewport, result)
    }

    private fun accumulatePosition(entity: Entity, world: World, viewport: Viewport, result: Vector2) {
        with(world) {
            val hud = entity[HUDElementComponent]
            val pId = hud.parentId
            if (pId != null && pId in world) {
                accumulatePosition(pId, world, viewport, result)
                val p = pId[HUDElementComponent]
                result.add(p.offsetX + p.localX, p.offsetY + p.localY)
            } else {
                val anchorPos = getAnchorPosition(hud.anchor, viewport)
                result.set(anchorPos.x, anchorPos.y)
            }
        }
    }
}
