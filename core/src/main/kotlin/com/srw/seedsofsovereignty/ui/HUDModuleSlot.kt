package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.srw.seedsofsovereignty.component.SlotType
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * A modular HUD slot primitive (v0.1.10).
 * Layers a technical frame, a projectile specimen, and an interaction layer.
 */
class HUDModuleSlot(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    val slotType: SlotType
) : Stack() {

    private val icon: ProjectileIcon = ProjectileIcon(drawer, skin)
    private val interactionButton: Button = Button(skin, "scifi") // Using base style for invisible interaction

    init {
        // 1. Technical Background Frame
        val frameImage = Image(skin.getDrawable("specimen-frame"))
        frameImage.color = when(slotType) {
            SlotType.WEAPON -> Color.CYAN
            SlotType.DRONE -> Color(1f, 0.7f, 0f, 1f) // Amber
            else -> Color.GRAY
        }
        add(frameImage)

        // 2. The Specimen Icon
        icon.iconScale = 1.8f
        add(icon)

        // 3. Interaction Overlay
        interactionButton.color.a = 0f // Invisible until needed
        add(interactionButton)
    }

    /**
     * Updates the internal DNA of the slot icon.
     */
    fun updateDNA(tier: Int, color: Color, isEmpty: Boolean = false) {
        icon.tier = tier
        icon.projectileColor = color
        icon.isEmpty = isEmpty
    }

    /**
     * Used by the Flying Icon celebration to find the docking target.
     */
    fun getIconCenter(): ProjectileIcon = icon
}
