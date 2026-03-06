package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.component.EffectType
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer

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
    private val currentIcon = ProjectileIcon(drawer, 1, 2.5f)
    private val offeredIcon = ProjectileIcon(drawer, 1, 2.5f)

    private val dmgBar = CapabilityBar(drawer, 0f, 0f, 200f)
    private val rateBar = CapabilityBar(drawer, 0f, 0f, 1f)

    init {
        background = skin.getDrawable("panel")
        pad(20f)

        val titleLabel = Label("TACTICAL MODULE ANALYSIS", skin, "title")
        add(titleLabel).colspan(2).padBottom(30f).row()

        add(Label("EQUIPPED", skin)).pad(10f)
        add(Label("STATION OFFER", skin)).pad(10f).row()

        add(currentIcon).size(80f).pad(10f)
        add(offeredIcon).size(80f).pad(10f).row()

        add(Label("DESTRUCTIVE POWER", skin)).colspan(2).padTop(20f).left().row()
        add(dmgBar).size(400f, 20f).colspan(2).padBottom(15f).row()

        add(Label("CYCLE SPEED (COOLDOWN)", skin)).colspan(2).padTop(10f).left().row()
        add(rateBar).size(400f, 20f).colspan(2).padBottom(20f).row()
    }

    fun update(current: WeaponModuleComponent, offered: WeaponModuleComponent) {
        currentIcon.tier = current.projectileTier
        offeredIcon.tier = offered.projectileTier
        currentIcon.color = current.projectileColor
        offeredIcon.color = offered.projectileColor

        // Sum the Hull damage for comparison power
        val currentDmg = current.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()
        val offeredDmg = offered.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()

        val maxDmg = (maxOf(currentDmg, offeredDmg) * 1.2f).coerceAtLeast(50f)
        dmgBar.current = currentDmg
        dmgBar.offered = offeredDmg
        dmgBar.max = maxDmg

        rateBar.current = 1f - current.fireRate.coerceIn(0f, 1f)
        rateBar.offered = 1f - offered.fireRate.coerceIn(0f, 1f)
        rateBar.max = 1f
    }
}
