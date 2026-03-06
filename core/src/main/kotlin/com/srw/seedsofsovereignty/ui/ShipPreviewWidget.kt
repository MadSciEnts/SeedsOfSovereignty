package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import space.earlygrey.shapedrawer.ShapeDrawer

class ShipPreviewWidget(
    private val drawer: ShapeDrawer,
    var tier: Int = 1,
    var subTier: Int = 1
) : Widget() {
    override fun draw(batch: Batch, parentAlpha: Float) {
        val centerX = x + width / 2f
        val centerY = y + height / 2f
        // Draw ship pointing up (PI/2) for preview
        ProceduralShipDrawer.drawPlayerShip(
            drawer, centerX, centerY,
            1.57f, 1.57f,
            tier, subTier
        )
    }
}
