package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical Command Deck (v0.2.0).
 * Orchestrates the interactive loadout bar (Top) and the morphing inputs (Bottom).
 */
class CommandMatrixWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: com.github.quillraven.fleks.World,
    private val onSelectionChanged: (Int) -> Unit
) : Table(skin) {

    private val loadoutBar = Table(skin)
    private val inputArea = Table(skin)

    private var selectedIndex: Int = 0
    private var currentSlotIdentities = mutableListOf<String>()

    init {
        bottom().right()

        // 1. Module Loadout Bar (Top)
        add(loadoutBar).expandX().fillX().padBottom(20f).center().row()

        // 2. Morphing Input Area (Bottom)
        add(inputArea).size(250f, 250f).center()
    }

    /**
     * Synchronizes the Command Matrix visuals with the ship's hardware.
     */
    fun syncLoadout(slots: List<ModuleSlot>, activeIndex: Int) {
        val newIdentities = slots.map { it.slotEntity?.toString() ?: it.equippedName }

        if (currentSlotIdentities != newIdentities) {
            currentSlotIdentities.clear()
            currentSlotIdentities.addAll(newIdentities)
            rebuildLayout(slots)
        }

        selectedIndex = activeIndex

        for (i in 0 until loadoutBar.children.size) {
            val btn = loadoutBar.getChild(i) as? HUDModuleSlotButton ?: continue
            btn.setFocused(i == selectedIndex)

            val slot = slots[i]
            val slotEntity = slot.slotEntity
            with(world) {
                if (slotEntity != null && world.contains(slotEntity)) {
                    if (slotEntity.has(WeaponModuleComponent)) {
                        val w = slotEntity[WeaponModuleComponent]
                        btn.updateDNA(w.projectileTier, w.projectileColor)
                    } else if (slotEntity.has(DroneModuleComponent)) {
                        val d = slotEntity[DroneModuleComponent]
                        btn.updateDNA(d.weaponDNA.projectileTier, d.weaponDNA.projectileColor)
                    }
                } else {
                    btn.updateDNA(slot.equippedTier, com.badlogic.gdx.graphics.Color.YELLOW, false)
                }
            }
        }
    }

    private fun rebuildLayout(slots: List<ModuleSlot>) {
        loadoutBar.clear()
        slots.forEachIndexed { index, slot ->
            var controlType = ControlType.BUTTON
            val slotEntity = slot.slotEntity
            with(world) {
                if (slotEntity != null && world.contains(slotEntity)) {
                    if (slotEntity.has(WeaponModuleComponent)) controlType = slotEntity[WeaponModuleComponent].controlType
                    if (slotEntity.has(DroneModuleComponent)) controlType = ControlType.TACTICAL_FOCUS_STICK
                }
            }

            val btn = HUDModuleSlotButton(drawer, skin, controlType)
            btn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onSelectionChanged(index)
                }
            })

            loadoutBar.add(btn).size(64f, 64f).pad(0f, 5f, 0f, 5f)
        }
        loadoutBar.invalidateHierarchy()
        loadoutBar.validate()
    }

    /**
     * Morphs the input area based on the selected module's Control DNA.
     * Ensures only the required control is visible and active.
     */
    fun updateInputDisplay(controlType: ControlType, directionalStick: Actor, tacticalStick: Actor, fireButton: Actor) {
        // Explicitly manage visibility to prevent overlapping input capture
        directionalStick.isVisible = false
        tacticalStick.isVisible = false
        fireButton.isVisible = false

        inputArea.clear()

        when (controlType) {
            ControlType.BUTTON -> {
                fireButton.isVisible = true
                inputArea.add(fireButton).size(220f, 220f).center()
            }
            ControlType.DIRECTIONAL_STICK -> {
                directionalStick.isVisible = true
                inputArea.add(directionalStick).size(250f, 250f).center()
            }
            ControlType.TACTICAL_FOCUS_STICK -> {
                tacticalStick.isVisible = true
                inputArea.add(tacticalStick).size(250f, 250f).center()
            }
        }

        inputArea.invalidateHierarchy()
        inputArea.validate()
    }

    fun getSlotButton(index: Int): HUDModuleSlotButton? {
        if (index < 0 || index >= loadoutBar.children.size) return null
        return loadoutBar.getChild(index) as? HUDModuleSlotButton
    }
}
