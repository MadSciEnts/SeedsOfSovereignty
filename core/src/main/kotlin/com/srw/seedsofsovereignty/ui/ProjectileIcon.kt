package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.util.ProjectileDrawer
import space.earlygrey.shapedrawer.ShapeDrawer

class ProjectileIcon(private val drawer: ShapeDrawer, var tier: Int = 1, var iconScale: Float = 1f) : Widget() {

    private fun getNormalizationScale(tier: Int): Float {
        return when (tier) {
            1 -> 1.6f  // Base 8f -> ~13f
            2 -> 1.1f  // Base 12f -> ~13f
            3 -> 1.0f  // Base 13f
            4 -> 1.1f  // Base 12f
            5 -> 1.3f  // Base 10f
            6 -> 1.1f  // Base 12f
            7 -> 0.7f  // Base 20f -> ~14f
            8 -> 0.55f // Base 25f -> ~14f
            9 -> 0.4f  // Base 35f -> ~14f
            else -> 1.0f
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val centerX = x + width / 2f
        val centerY = y + height / 2f

        // Apply the normalization factor so that iconScale 1.0 is consistent across all tiers
        val finalScale = iconScale * getNormalizationScale(tier)

        // 1.57f is roughly PI/2 (upwards)
        ProjectileDrawer.draw(drawer, centerX, centerY, 1.57f, tier, finalScale)
    }
}
