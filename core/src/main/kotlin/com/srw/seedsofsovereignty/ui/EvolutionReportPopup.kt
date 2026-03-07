package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import com.srw.seedsofsovereignty.component.DroneModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * A professional technical briefing modal shown upon Major Tier Evolution.
 * Optimized for vertical space and mobile aspect ratios.
 */
class EvolutionReportPopup(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val newTier: Int,
    private val newWeapon: WeaponModuleComponent,
    private val newDrone: DroneModuleComponent?,
    private val onConfirm: () -> Unit
) : Table(skin) {

    init {
        background = skin.getDrawable("panel")
        pad(20f) // Reduced padding

        // 1. Header
        val headerTable = Table(skin)
        val header = Label("NEW VESSEL CONFIGURATION UNLOCKED", skin, "title").apply { color = Color.GOLD }
        val subHeader = Label("TIER $newTier VANGUARD CLASS CHASSIS ACTIVE", skin).apply { color = Color.LIGHT_GRAY }
        headerTable.add(header).row()
        headerTable.add(subHeader).row()
        add(headerTable).padBottom(15f).row()

        // 2. Scrollable Content Area
        val content = Table(skin)

        // Ship Preview (Scaled down for Task: Fix screen height issues)
        val preview = ShipPreviewWidget(drawer, tier = newTier, subTier = 1)
        content.add(preview).size(220f).padBottom(20f).row()

        // Technical Breakdown
        val techTable = Table(skin)
        techTable.defaults().pad(10f).top()

        val wColumn = Table(skin)
        wColumn.add(Label("PRIMARY ARMAMENT", skin, "medium")).row()
        val wIcon = ProjectileIcon(drawer, skin, newWeapon.projectileTier, 2f, newWeapon.projectileColor)
        wColumn.add(wIcon).size(100f).padTop(5f).row()
        wColumn.add(Label(newWeapon.name, skin, "small")).row()
        techTable.add(wColumn).width(280f)

        if (newDrone != null) {
            val dColumn = Table(skin)
            dColumn.add(Label("DRONE SUPPORT", skin, "medium")).row()
            val dIcon = ProjectileIcon(drawer, skin, newDrone.weaponDNA.projectileTier, 2f, newDrone.weaponDNA.projectileColor)
            dColumn.add(dIcon).size(100f).padTop(5f).row()
            dColumn.add(Label(newDrone.name, skin, "small")).row()
            techTable.add(dColumn).width(280f)
        }
        content.add(techTable).fillX().padBottom(20f).row()

        // Briefing Summary
        val summary = Label("All systems calibrated. Hardware topology synchronized. Hull integrity at 100%. Proceed with tactical objectives.", skin).apply {
            wrap = true
            setAlignment(Align.center)
        }
        content.add(summary).width(580f).padBottom(20f).row()

        val scroll = ScrollPane(content, skin)
        scroll.setScrollingDisabled(true, false)
        add(scroll).expand().fill().padBottom(20f).row()

        // 3. Action Footer
        val confirmBtn = UIButtonFactory.createButton(skin, "COMMENCE OPERATIONS", UIButtonFactory.ButtonType.MARKET) {
            onConfirm()
        }
        add(confirmBtn).size(400f, 80f).center()

        pack()
    }
}
