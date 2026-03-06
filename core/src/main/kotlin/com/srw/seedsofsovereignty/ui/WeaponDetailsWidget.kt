package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer
import java.util.Locale

class StatBar(private val drawer: ShapeDrawer, var value: Float = 0f, var max: Float = 100f, var barColor: Color = Color.CYAN) : Widget() {
    override fun draw(batch: Batch, parentAlpha: Float) {
        drawer.setColor(0.1f, 0.1f, 0.1f, 0.5f * parentAlpha)
        drawer.filledRectangle(x, y, width, height)

        val ratio = (value / max).coerceIn(0f, 1f)
        drawer.setColor(barColor.r, barColor.g, barColor.b, barColor.a * parentAlpha)
        drawer.filledRectangle(x, y, width * ratio, height)

        drawer.setColor(1f, 1f, 1f, 0.3f * parentAlpha)
        drawer.rectangle(x, y, width, height, 1f)
    }
}

class WeaponDetailsWidget(private val skin: Skin, private val drawer: ShapeDrawer, title: String) : Table(skin) {
    // Replaced anonymous widget with the shared ProjectileIcon for easier scaling
    private val icon = ProjectileIcon(drawer, 1, 4.5f)

    private val nameLabel = Label("N/A", skin, "title").apply {
        setAlignment(com.badlogic.gdx.utils.Align.center)
    }
    private val descLabel = Label("", skin).apply {
        wrap = true
        setAlignment(com.badlogic.gdx.utils.Align.center)
    }

    private val dmgBar = StatBar(drawer, barColor = Color.ORANGE)
    private val rateBar = StatBar(drawer, barColor = Color.LIME)
    private val speedBar = StatBar(drawer, barColor = Color.SKY)

    private val dmgValueLabel = Label("0", skin)
    private val rateValueLabel = Label("0", skin)
    private val speedValueLabel = Label("0", skin)

    init {
        background = skin.getDrawable("panel")
        pad(15f)
        // Fixed width to maintain symmetry and prevent layout jitter
        defaults().width(320f)

        add(Label(title, skin)).padBottom(10f).row()
        add(icon).size(160f).padBottom(10f).row() // Increased size for the larger icon

        add(nameLabel).width(300f).height(45f).padBottom(10f).row()

        val statsTable = Table(skin)
        statsTable.defaults().width(300f)

        val labelW = 50f; val barW = 180f; val valW = 50f

        val dmgRow = Table(skin)
        dmgRow.add(Label("PWR", skin)).left().width(labelW)
        dmgRow.add(dmgBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        dmgRow.add(dmgValueLabel).width(valW).right()
        statsTable.add(dmgRow).row()

        val rateRow = Table(skin)
        rateRow.add(Label("RAT", skin)).left().width(labelW)
        rateRow.add(rateBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        rateRow.add(rateValueLabel).width(valW).right()
        statsTable.add(rateRow).row()

        val speedRow = Table(skin)
        speedRow.add(Label("VEL", skin)).left().width(labelW)
        speedRow.add(speedBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        speedRow.add(speedValueLabel).width(valW).right()
        statsTable.add(speedRow).row()

        add(statsTable).padBottom(15f).row()
        add(descLabel).width(300f).height(70f).top().row()
    }

    fun update(module: WeaponModuleComponent?, maxDmg: Float = 200f) {
        if (module == null) {
            nameLabel.setText("---")
            descLabel.setText("Awaiting analysis...")
            icon.tier = 1
            dmgBar.value = 0f
            rateBar.value = 0f
            speedBar.value = 0f
            dmgValueLabel.setText("-")
            rateValueLabel.setText("-")
            speedValueLabel.setText("-")
            return
        }

        icon.tier = module.projectileTier

        nameLabel.setText(module.name.uppercase())
        descLabel.setText(module.description)

        dmgBar.value = module.damage
        dmgBar.max = maxDmg
        dmgValueLabel.setText(module.damage.toInt().toString())

        val rateVal = (1.0f - module.fireRate).coerceIn(0.1f, 1.0f)
        rateBar.value = rateVal
        rateBar.max = 1.0f
        rateValueLabel.setText(String.format(Locale.ROOT, "%.1f", rateVal * 10f))

        speedBar.value = module.projectileSpeed
        speedBar.max = 2000f
        speedValueLabel.setText((module.projectileSpeed / 100f).toInt().toString())
    }
}
