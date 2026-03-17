package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*

/**
 * CAMERA FOLLOW & ZOOM SYSTEM:
 * Interpolates camera position to player and adjusts zoom based on ship size.
 * Adheres to Fleks 2.x context-based syntax and Zero-Allocation rules.
 */
class CameraFollowSystem(
    private val camera: OrthographicCamera = inject("camera"),
    private val gameViewport: Viewport = inject("gameViewport")
) : IteratingSystem(
    family { all(PlayerComponent, PositionComponent).any(ShapeComponent, TextureRegionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos

        // 1. Smooth Position Follow
        camera.position.x += (pos.x - camera.position.x) * 0.1f
        camera.position.y += (pos.y - camera.position.y) * 0.1f

        // 2. Adaptive Zoom
        // Goal: Ship size should be ~2.5% of the screen width for optimal balance between visibility and scale.

        // Determine "Visual Width" in World Units
        val worldWidth = when {
            entity.has(TextureRegionComponent) -> {
                /**
                 * In our dynamic normalization system, scale IS the absolute world width.
                 * Tier 1 A = 1.0f.
                 */
                entity.get(TextureRegionComponent).scale
            }
            entity.has(ShapeComponent) -> {
                // Shape size is radius, so width = size * 2
                entity.get(ShapeComponent).size * 2f
            }
            else -> 1.0f
        }

        // targetZoom = worldWidth / (viewportWidth * fraction)
        // We use 0.025f (2.5%) to pull the camera back to a usable combat perspective.
        val targetZoom = worldWidth / (gameViewport.worldWidth * 0.025f)

        // Smoothly interpolate zoom
        camera.zoom += (targetZoom - camera.zoom) * 0.05f
    }
}
