package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.ShapeComponent

/**
 * CAMERA FOLLOW & ZOOM SYSTEM:
 * Interpolates camera position to player and adjusts zoom based on ship size.
 */
class CameraFollowSystem(
    private val camera: OrthographicCamera = inject("camera"),
    private val gameViewport: Viewport = inject("gameViewport")
) : IteratingSystem(
    family { all(PlayerComponent, PositionComponent, ShapeComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val shape = entity[ShapeComponent]

        // 1. Smooth Position Follow
        camera.position.x += (pos.x - camera.position.x) * 0.1f
        camera.position.y += (pos.y - camera.position.y) * 0.1f

        // 2. Adaptive Zoom
        // Goal: Ship size should be ~1% of the screen width for a sense of vastness.
        // Screen width in world units = gameViewport.worldWidth * camera.zoom
        // S / (W * zoom) = 0.01  => zoom = S / (W * 0.01)
        val targetZoom = shape.size / (gameViewport.worldWidth * 0.01f)

        // Smoothly interpolate zoom
        camera.zoom += (targetZoom - camera.zoom) * 0.05f
    }
}
