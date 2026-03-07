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

class WeaponComparisonWidget(private val skin: Skin, private val drawer: ShapeDrawer) : Table(skin) {
    private val currentIcon = ProjectileIcon(drawer, skin, 1, 2.5f)
    private val offeredIcon = ProjectileIcon(drawer, skin, 1, 2.5f)

    private val dmgBar = StatBar(drawer, skin, barColor = Color.ORANGE)
    private val rateBar = StatBar(drawer, skin, barColor = Color.LIME)

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
        currentIcon.projectileColor = current.projectileColor
        offeredIcon.projectileColor = offered.projectileColor

        val currentDmg = current.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()
        val offeredDmg = offered.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()

        val maxDmg = (maxOf(currentDmg, offeredDmg) * 1.2f).coerceAtLeast(50f)
        dmgBar.value = currentDmg
        dmgBar.max = maxDmg

        rateBar.value = (1f - current.fireRate.coerceIn(0f, 1f)) * 100f
        rateBar.max = 100f
    }
}
