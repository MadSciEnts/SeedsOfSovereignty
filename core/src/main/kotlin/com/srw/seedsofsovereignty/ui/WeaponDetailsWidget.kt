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
    private val icon = ProjectileIcon(drawer, 1, 4.5f)
    private val nameLabel = Label("N/A", skin, "title").apply { setAlignment(com.badlogic.gdx.utils.Align.center) }
    private val descLabel = Label("", skin).apply { wrap = true; setAlignment(com.badlogic.gdx.utils.Align.center) }
    private val effectsTable = Table(skin)

    private val dmgBar = StatBar(drawer, barColor = Color.ORANGE)
    private val rateBar = StatBar(drawer, barColor = Color.LIME)
    private val speedBar = StatBar(drawer, barColor = Color.SKY)
    private val dmgValueLabel = Label("0", skin)
    private val rateValueLabel = Label("0", skin)
    private val speedValueLabel = Label("0", skin)

    init {
        background = skin.getDrawable("panel")
        pad(15f)
        defaults().width(320f)

        add(Label(title, skin)).padBottom(10f).row()
        add(icon).size(160f).padBottom(10f).row()
        add(nameLabel).width(300f).height(45f).padBottom(10f).row()

        val statsTable = Table(skin)
        statsTable.defaults().width(300f)
        val labelW = 50f; val barW = 180f; val valW = 50f

        statsTable.add(Label("PWR", skin)).left().width(labelW)
        statsTable.add(dmgBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        statsTable.add(dmgValueLabel).width(valW).right().row()

        statsTable.add(Label("RAT", skin)).left().width(labelW)
        statsTable.add(rateBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        statsTable.add(rateValueLabel).width(valW).right().row()

        statsTable.add(Label("VEL", skin)).left().width(labelW)
        statsTable.add(speedBar).size(barW, 14f).pad(0f, 10f, 0f, 10f)
        statsTable.add(speedValueLabel).width(valW).right().row()

        add(statsTable).padBottom(15f).row()

        add(Label("EFFECTS ANALYSIS", skin)).padBottom(5f).row()
        add(effectsTable).width(300f).height(80f).top().row()

        add(descLabel).width(300f).height(60f).top().row()
    }

    fun update(module: WeaponModuleComponent?, maxDmg: Float = 200f) {
        effectsTable.clear()
        if (module == null) {
            nameLabel.setText("---"); descLabel.setText("Awaiting analysis..."); return
        }

        icon.tier = module.projectileTier
        icon.projectileColor = module.projectileColor
        nameLabel.setText(module.name.uppercase())
        descLabel.setText(module.description)

        val hullDmg = module.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()
        dmgBar.value = hullDmg
        dmgValueLabel.setText(hullDmg.toInt().toString())

        rateBar.value = (1.0f - module.fireRate).coerceIn(0.1f, 1.0f)
        rateValueLabel.setText(String.format(Locale.ROOT, "%.1f", rateBar.value * 10f))

        speedBar.value = module.projectileSpeed
        speedValueLabel.setText((module.projectileSpeed / 100f).toInt().toString())

        // Build Effects List (Task #1)
        module.effects.forEach { effect ->
            val labelText = when(effect.type) {
                EffectType.SHIELD_DAMAGE -> "+${effect.potency.toInt()} Shield Disrupt"
                EffectType.SHIELD_PIERCE -> "Shield Piercing [100%]"
                EffectType.SPEED_MULTIPLIER -> "Snare: ${(100 - effect.potency * 100).toInt()}%"
                EffectType.COOLDOWN_MULTIPLIER -> "System Jammer"
                else -> null
            }
            if (labelText != null) {
                effectsTable.add(Label(labelText, skin)).left().row()
            }
        }
    }
}
