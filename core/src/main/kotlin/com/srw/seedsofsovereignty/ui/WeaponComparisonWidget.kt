package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import com.srw.seedsofsovereignty.util.ProjectileDrawer
import space.earlygrey.shapedrawer.ShapeDrawer

class ProjectileIcon(private val drawer: ShapeDrawer, var tier: Int) : Widget() {
    override fun draw(batch: Batch, parentAlpha: Float) {
        val centerX = x + width / 2f
        val centerY = y + height / 2f
        ProjectileDrawer.draw(drawer, centerX, centerY, 1.57f, tier) // Upwards
    }
}

class CapabilityBar(private val drawer: ShapeDrawer, var current: Float, var offered: Float, var max: Float) : Widget() {
    override fun draw(batch: Batch, parentAlpha: Float) {
        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x, y, width, height)

        val currentW = (current / max).coerceIn(0f, 1f) * width
        val offeredW = (offered / max).coerceIn(0f, 1f) * width

        if (offeredW > currentW) {
            drawer.setColor(Color.LIME)
            drawer.filledRectangle(x, y, offeredW, height)
            drawer.setColor(Color.CYAN)
            drawer.filledRectangle(x, y, currentW, height)
        } else {
            drawer.setColor(Color.CYAN)
            drawer.filledRectangle(x, y, currentW, height)
            drawer.setColor(Color.FIREBRICK)
            drawer.filledRectangle(x + offeredW, y, currentW - offeredW, height)
        }

        drawer.setColor(Color.WHITE)
        drawer.rectangle(x, y, width, height, 1f)
    }
}

class WeaponComparisonWidget(private val skin: Skin, private val drawer: ShapeDrawer) : Table(skin) {
    private val currentIcon = ProjectileIcon(drawer, 1)
    private val offeredIcon = ProjectileIcon(drawer, 1)

    private val dmgBar = CapabilityBar(drawer, 0f, 0f, 200f)
    private val rateBar = CapabilityBar(drawer, 0f, 0f, 1f)

    init {
        val titleLabel = Label("MODULE COMPARISON", skin)
        add(titleLabel).colspan(2).padBottom(20f).row()

        add(Label("CURRENT", skin)).pad(10f)
        add(Label("STATION", skin)).pad(10f).row()

        add(currentIcon).size(64f).pad(10f)
        add(offeredIcon).size(64f).pad(10f).row()

        add(Label("DAMAGE", skin)).colspan(2).padTop(10f).row()
        add(dmgBar).size(300f, 15f).colspan(2).padBottom(10f).row()

        add(Label("FIRE RATE (Lower is better)", skin)).colspan(2).padTop(10f).row()
        add(rateBar).size(300f, 15f).colspan(2).padBottom(10f).row()
    }

    fun update(current: WeaponModuleComponent, offered: WeaponModuleComponent) {
        currentIcon.tier = current.projectileTier
        offeredIcon.tier = offered.projectileTier

        // Use a relative max based on the higher tier + buffer
        val maxDmg = (maxOf(current.damage, offered.damage) * 1.2f).coerceAtLeast(50f)
        dmgBar.current = current.damage
        dmgBar.offered = offered.damage
        dmgBar.max = maxDmg

        // For rate, 1.0s is slow, 0.1s is fast.
        rateBar.current = 1f - current.fireRate.coerceIn(0f, 1f)
        rateBar.offered = 1f - offered.fireRate.coerceIn(0f, 1f)
        rateBar.max = 1f
    }
}
