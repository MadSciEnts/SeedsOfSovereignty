package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.srw.seedsofsovereignty.component.EffectType
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Re-designed for Scifi Minimalism.
 * High-efficiency layout with large, legible data-bars.
 */
class WeaponDetailsWidget(private val skin: Skin, private val drawer: ShapeDrawer, val title: String) : Table(skin) {
    private val icon = ProjectileIcon(drawer, skin, 1, 3.5f)
    private val nameLabel = Label("N/A", skin, "title").apply { setAlignment(com.badlogic.gdx.utils.Align.left) }
    private val rarityLabel = Label("", skin).apply { color = Color.GRAY }

    private val dmgBar = StatBar(drawer, skin, barColor = Color.ORANGE)
    private val rateBar = StatBar(drawer, skin, barColor = Color.LIME)
    private val speedBar = StatBar(drawer, skin, barColor = Color.SKY)

    private val effectsTable = Table(skin)

    init {
        pad(10f)
        setupCompactLayout()
    }

    private fun setupCompactLayout() {
        clearChildren()

        val header = Table(skin)
        header.add(icon).size(80f).padRight(15f)

        val nameStack = Table(skin)
        nameStack.add(nameLabel).left().row()
        nameStack.add(rarityLabel).left()
        header.add(nameStack).expandX().fillX()

        add(header).expandX().fillX().padBottom(15f).row()

        val stats = Table(skin)
        stats.defaults().padBottom(10f)

        addStatWithLabel(stats, "PWR", dmgBar)
        addStatWithLabel(stats, "RAT", rateBar)
        addStatWithLabel(stats, "VEL", speedBar)

        add(stats).expandX().fillX().row()
        add(effectsTable).expandX().fillX().padTop(10f)
    }

    private fun addStatWithLabel(target: Table, label: String, bar: StatBar) {
        val row = Table(skin)
        val l = Label(label, skin, "medium")
        row.add(l).width(45f).left()
        row.add(bar).size(140f, 16f).padLeft(15f).left()
        target.add(row).expandX().fillX().row()
    }

    fun update(module: WeaponModuleComponent?) {
        effectsTable.clear()
        if (module == null) return

        icon.tier = module.projectileTier
        icon.projectileColor = module.projectileColor
        nameLabel.setText(module.name.uppercase())
        rarityLabel.setText(module.rarity.name)
        rarityLabel.color = getRarityColor(module.rarity)

        val hullDmg = module.effects.filter { it.type == EffectType.HULL_DAMAGE }.sumOf { it.potency.toDouble() }.toFloat()
        dmgBar.value = hullDmg
        dmgBar.max = 200f

        rateBar.value = (1.0f - module.fireRate).coerceIn(0.1f, 1.0f) * 100f
        rateBar.max = 100f

        speedBar.value = module.projectileSpeed
        speedBar.max = 2000f

        module.effects.forEach { effect ->
            val txt = when(effect.type) {
                EffectType.SHIELD_DAMAGE -> "ION"
                EffectType.SHIELD_PIERCE -> "PIERCE"
                EffectType.SPEED_MULTIPLIER -> "SNARE"
                EffectType.COOLDOWN_MULTIPLIER -> "JAM"
                else -> null
            }
            if (txt != null) {
                val tag = Label(txt, skin, "medium")
                tag.color = Color.CYAN
                effectsTable.add(tag).padRight(10f)
            }
        }
    }

    private fun getRarityColor(rarity: com.srw.seedsofsovereignty.component.Rarity): Color {
        return when(rarity) {
            com.srw.seedsofsovereignty.component.Rarity.COMMON -> Color.WHITE
            com.srw.seedsofsovereignty.component.Rarity.UNCOMMON -> Color.LIME
            com.srw.seedsofsovereignty.component.Rarity.RARE -> Color.SKY
            com.srw.seedsofsovereignty.component.Rarity.EPIC -> Color.VIOLET
            com.srw.seedsofsovereignty.component.Rarity.LEGENDARY -> Color.ORANGE
        }
    }
}
