package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.srw.seedsofsovereignty.component.ControlType
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Interactive Loadout Tile for the ATO Command Matrix (v0.2.0).
 */
class HUDModuleSlotButton(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    val controlDNA: ControlType
) : Button(skin, "scifi") {

    private val icon: ProjectileIcon = ProjectileIcon(drawer, skin)
    private var isFocused: Boolean = false

    init {
        setTransform(true)
        icon.iconScale = 1.5f
        add(icon).size(48f).center()
    }

    /**
     * Updates the visuals of the specimen inside the tile.
     */
    fun updateDNA(tier: Int, color: Color, isEmpty: Boolean = false) {
        icon.tier = tier
        icon.projectileColor = color
        icon.isEmpty = isEmpty
    }

    fun setFocused(focused: Boolean) {
        isFocused = focused
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // Task: Visual feedback for the Active System
        if (isFocused) {
            val shimmer = 0.6f + 0.4f * com.badlogic.gdx.math.MathUtils.sin(System.currentTimeMillis() / 150f)
            drawer.setColor(1f, 0.7f, 0f, shimmer * parentAlpha) // Amber Focus
            drawer.rectangle(x - 2f, y - 2f, width + 4f, height + 4f, 2f)
        }
    }
}
