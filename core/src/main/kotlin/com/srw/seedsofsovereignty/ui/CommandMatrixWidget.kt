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
 * Technical Specification: CommandMatrixWidget (v0.6.6)
 * ROLE: The "Physical" control deck containing the module loadout and tactical instrument.
 *
 * ARCHITECTURAL STANDARDS:
 * 1. [WIDE DECK] - Hard-coded 550f width provides ergonomic "Runway" for satellite controls.
 * 2. [RTL ALIGNMENT] - Modules are added in reverse order (N-1 downTo 0) to ensure
 *    Slot 1 is always the far-right interactive point.
 * 3. [DELEGATION] - All signal logic is offloaded to [TacticalInputController].
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
        // [WIDE DECK SPEC] - DO NOT REDUCE. Required for 250px joystick + satellite spacing.
        setSize(550f, 330f)
        setOrigin(Align.bottomRight)

        addActor(loadoutBar)
        // RTL Anchoring pass
        loadoutBar.right()
        loadoutBar.setSize(550f, 70f)
        loadoutBar.setPosition(0f, 255f)

        addActor(clusterInterpreter)
        clusterInterpreter.setSize(550f, 250f)
        clusterInterpreter.setPosition(0f, 0f)
    }

    /**
     * State Synchronization: Updates visual module icons and focus states.
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
            // Index Mapping: Reverses the Table index back to the logical slot index.
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

    /**
     * UI Assembly: Builds the physical module bar.
     * Note: Uses [asReversed] logic to anchor Slot 1 to the right.
     */
    private fun rebuildLayout(slots: List<ModuleSlot>) {
        loadoutBar.clear()
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
     * Instrument Refresh: Rebuilds the tactical surface for the selected module.
     * Delegates schema processing and binding to the [inputController].
     */
    fun updateTacticalSurface(schema: TacticalSchema, slotIndex: Int) {
        val processedSchema = inputController.processSchema(schema, slotIndex)

        clusterInterpreter.renderSchema(processedSchema, inputController.actionCallback) { channel, callback ->
            inputController.bindInstrument(channel, callback)
        }

        // Re-enforce stable 550x330 footprint after refresh.
        setSize(550f, 330f)
        loadoutBar.right()
        loadoutBar.setSize(550f, 70f)
        loadoutBar.setPosition(0f, 255f)

        clusterInterpreter.setSize(550f, 250f)
        clusterInterpreter.setPosition(0f, 0f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        // AGGREGATOR BRIDGE: Pass signals from Hub to UI observers in the cluster.
        inputController.updateSignals { key, signal ->
            clusterInterpreter.onSignalReceived(key, signal)
        }
    }

    fun getSlotButton(index: Int): HUDModuleSlotButton? {
        if (index < 0 || index >= loadoutBar.children.size) return null
        return loadoutBar.getChild((loadoutBar.children.size - 1) - index) as? HUDModuleSlotButton
    }
}
