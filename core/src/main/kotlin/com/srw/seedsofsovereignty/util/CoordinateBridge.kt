package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * ARCHITECTURAL WARNING: LEAD ARCHITECT MANDATE (v2.6.0)
 * ROLE: The Immutable Spatial Anchor for Diegetic AR.
 *
 * This object is the SOLE SOURCE OF TRUTH for World-to-UI projection.
 * DO NOT ALTER, REFACTOR, OR OPTIMIZE without T1 HIGH TABLE CONSENSUS.
 */
object CoordinateBridge {

    /**
     * Translates a world position into UI-space units relative to the screen center.
     * Logic: (uiCenter) + (worldDifference / shipZoom)
     */
    fun projectWorldToUI(
        worldPos: Vector2,
        playerPos: Vector2,
        shipCamera: OrthographicCamera,
        uiViewport: Viewport,
        out: Vector2
    ): Vector2 {
        val zoom = shipCamera.zoom

        // 1. Calculate relative difference in world units
        val dx = (worldPos.x - playerPos.x) / zoom
        val dy = (worldPos.y - playerPos.y) / zoom

        // 2. Apply to UI center (Bottom-Left Origin Standard)
        val cx = uiViewport.worldWidth / 2f
        val cy = uiViewport.worldHeight / 2f

        return out.set(cx + dx, cy + dy)
    }

    /**
     * Translates a world position directly into UI-Offset units for HUD components.
     * Use this for updating HUDElementComponent.offsetX/Y.
     */
    fun getDiegeticOffset(
        worldPos: Vector2,
        playerPos: Vector2,
        shipCamera: OrthographicCamera,
        out: Vector2
    ): Vector2 {
        val zoom = shipCamera.zoom
        return out.set(
            (worldPos.x - playerPos.x) / zoom,
            (worldPos.y - playerPos.y) / zoom
        )
    }
}
