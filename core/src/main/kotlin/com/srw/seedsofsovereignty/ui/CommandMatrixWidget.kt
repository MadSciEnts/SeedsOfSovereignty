package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.github.quillraven.fleks.Entity
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical Command Deck (v0.3.32).
 * Refactored into a Fixed-Size Hardware Instrument (WidgetGroup).
 * v0.4.0: Updated for Bidirectional Instrument DNA using TacticalHubComponent.
 * v0.4.2 (Phase 9.6): Stabilized input listeners with Container unwrapping logic.
 * v0.4.3 (Phase 9.6): Delegated input logic to TacticalInputController.
 * v0.5.6 (Phase 9.9): Wide Deck Lockdown - Expanded to 550px.
 * v0.5.7: Entity ID Binding. Passes slotEntity to input controller for crosstalk isolation.
 * Phase 9.9.2: Slot-Based Suffixing. Passes slotIndex to input controller.
 */
class CommandMatrixWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: com.github.quillraven.fleks.World,
    private val onSelectionChanged: (Int) -> Unit
) : WidgetGroup() {

    private val loadoutBar = Table(skin)
    private val clusterInterpreter = TacticalClusterWidget(skin)
    private val inputController = TacticalInputController(world, skin)

    private var selectedIndex: Int = 0
    private var currentSlotIdentities = mutableListOf<String>()

    init {
        // Phase 9.9: Wide Deck Expansion.
        setSize(550f, 330f)

        // Set origin SECOND.
        setOrigin(Align.bottomRight)

        addActor(loadoutBar)
        // Phase 9.3, Step 1: RTL Layout Fix - Initial Anchoring
        loadoutBar.right()
        loadoutBar.setSize(550f, 70f)
        loadoutBar.setPosition(0f, 255f)

        addActor(clusterInterpreter)
        // v0.3.32: Absolute cluster anchor.
        clusterInterpreter.setSize(550f, 250f)
        clusterInterpreter.setPosition(0f, 0f)
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
            // Phase 9.3, Step 1: Map child index back to slot index (reversed addition)
            val slotIndex = (loadoutBar.children.size - 1) - i
            btn.setFocused(slotIndex == selectedIndex)

            val slot = slots[slotIndex]
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
                    btn.updateDNA(slot.equippedTier, Color.YELLOW, false)
                }
            }
        }
    }

    private fun rebuildLayout(slots: List<ModuleSlot>) {
        loadoutBar.clear()
        // Phase 9.3, Step 1: RTL Layout Fix - Reverse Iteration
        // Adding buttons in reverse order (N-1 downTo 0) ensures Slot 1 (index 0) is added LAST (far right).
        for (i in slots.size - 1 downTo 0) {
            val slot = slots[i]
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
                    onSelectionChanged(i)
                }
            })

            loadoutBar.add(btn).size(64f, 64f).pad(0f, 5f, 0f, 5f)
        }
        loadoutBar.invalidateHierarchy()
        loadoutBar.validate()
    }

    /**
     * Updates the tactical surface and binds UI events to the ECS Action Hub.
     */
    fun updateTacticalSurface(schema: TacticalSchema, slotIndex: Int) {
        val processedSchema = inputController.processSchema(schema, slotIndex)

        clusterInterpreter.renderSchema(processedSchema, inputController.actionCallback) { channel, callback ->
            inputController.bindInstrument(channel, callback)
        }

        // Phase 9.9: Re-enforce stable 550x330 footprint.
        setSize(550f, 330f)
        // Phase 9.3, Step 1: RTL Layout Fix - Re-enforce Anchoring
        loadoutBar.right()
        loadoutBar.setSize(550f, 70f)
        loadoutBar.setPosition(0f, 255f)

        clusterInterpreter.setSize(550f, 250f)
        clusterInterpreter.setPosition(0f, 0f)
    }

    override fun act(delta: Float) {
        super.act(delta)
    }

    fun getSlotButton(index: Int): HUDModuleSlotButton? {
        if (index < 0 || index >= loadoutBar.children.size) return null
        // Phase 9.3, Step 1: Map external index to internal reversed child index
        return loadoutBar.getChild((loadoutBar.children.size - 1) - index) as? HUDModuleSlotButton
    }
}
