package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical Command Deck (v0.3.32).
 * Refactored into a Fixed-Size Hardware Instrument (WidgetGroup).
 * v0.4.0: Updated for Bidirectional Instrument DNA using TacticalHubComponent.
 */
class CommandMatrixWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: com.github.quillraven.fleks.World,
    private val onSelectionChanged: (Int) -> Unit,
    private val onSpaceToggled: () -> Unit
) : WidgetGroup() {

    private val loadoutBar = Table(skin)
    private val clusterInterpreter = TacticalClusterWidget(skin)
    private val spaceToggle = TextButton("WLD", skin, "mode-toggle")

    private var selectedIndex: Int = 0
    private var currentSlotIdentities = mutableListOf<String>()

    init {
        // v0.3.32: Step 1: Compact Root. Set size FIRST.
        setSize(350f, 330f)

        // Set origin SECOND.
        setOrigin(Align.bottomRight)

        addActor(loadoutBar)
        // v0.3.32: Welded Positioning.
        loadoutBar.setSize(250f, 70f)
        loadoutBar.setPosition(width - 250f, 255f)

        addActor(clusterInterpreter)
        // v0.3.32: Absolute cluster anchor.
        clusterInterpreter.setSize(350f, 250f)
        clusterInterpreter.setPosition(0f, 0f)

        spaceToggle.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                onSpaceToggled()
            }
        })
    }

    /**
     * Updates the toggle button text to reflect the current fleet mode.
     */
    fun updateSpaceLabel(isWorld: Boolean) {
        spaceToggle.setText(if (isWorld) "WLD" else "LOC")
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
     * Updates the tactical surface and binds UI events to the ECS Action Hub.
     */
    fun updateTacticalSurface(schema: TacticalSchema) {
        // v0.5.3: Unique Drone Keys per Slot (Fixes Crosstalk)
        val processedInputs = schema.inputs.map { input ->
            if (selectedIndex > 0 && (input.actionKey.startsWith("DRONE_MOVE") || input.actionKey.startsWith("DRONE_DEPLOY"))) {
                input.copy(actionKey = "${input.actionKey}_$selectedIndex")
            } else {
                input
            }
        }
        val processedSchema = schema.copy(inputs = processedInputs)

        val actionCallback: (String, Any) -> Unit = { actionKey, value ->
            with(world) {
                val playerFamily = family { all(PlayerComponent, TacticalHubComponent) }
                playerFamily.firstOrNull()?.let { player ->
                    val hub = player[TacticalHubComponent]
                    when (value) {
                        is Boolean -> {
                            if (actionKey == "FIRE") {
                                if (value) hub.activeBitmask = hub.activeBitmask or (1L shl 0)
                                else hub.activeBitmask = hub.activeBitmask and (1L shl 0).inv()
                            } else {
                                hub.scalars[actionKey] = if (value) 1f else 0f
                            }
                        }
                        is Vector2 -> {
                            hub.vectors[actionKey]?.set(value) ?: run {
                                hub.vectors[actionKey] = Vector2(value)
                            }

                            // v0.5.3: Save Persistent Stick State
                            if (player.has(TacticalPersistenceComponent)) {
                                player[TacticalPersistenceComponent].persistentInputs[actionKey] = Vector2(value)
                            }
                        }
                        is Float -> {
                            hub.scalars[actionKey] = value
                        }
                    }
                }
            }
        }

        clusterInterpreter.renderSchema(processedSchema, actionCallback) { channel, callback ->
            val actor = HUDFactory.createTacticalInput(skin, channel, world)

            // v0.5.3: Restore Persistent Stick State
            if (channel.classification == ControlClassification.TACTICAL_STICK) {
                val container = actor as? Container<*>
                val touchpad = container?.actor as? SovereigntyTouchpad
                if (touchpad != null) {
                    val player = world.family { all(PlayerComponent, TacticalPersistenceComponent) }.firstOrNull()
                    player?.get(TacticalPersistenceComponent)?.persistentInputs?.get(channel.actionKey)?.let { savedPos ->
                        touchpad.setKnobPosition(savedPos.x, savedPos.y)
                    }
                }
            }

            when (channel.classification) {
                ControlClassification.ACTION_BUTTON -> {
                    actor.addListener(object : ClickListener() {
                        override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                            callback(channel.actionKey, true)
                            return true
                        }
                        override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                            callback(channel.actionKey, false)
                        }
                    })
                }
                ControlClassification.TOGGLE_SWITCH -> {
                    actor.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent?, target: Actor?) {
                            val btn = target as? Button ?: (target as? Container<*>)?.actor as? Button
                            btn?.let { callback(channel.actionKey, it.isChecked) }
                        }
                    })
                }
                ControlClassification.MOVEMENT_STICK, ControlClassification.TACTICAL_STICK -> {
                    actor.addListener(object : ChangeListener() {
                        private val knobPercent = Vector2()
                        override fun changed(event: ChangeEvent?, target: Actor?) {
                            val pad = target as? Touchpad ?: (target as? Container<*>)?.actor as? Touchpad
                            pad?.let {
                                knobPercent.set(it.knobPercentX, it.knobPercentY)
                                callback(channel.actionKey, knobPercent)
                            }
                        }
                    })
                }
                ControlClassification.MODIFIER_SLIDER -> {
                    actor.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent?, target: Actor?) {
                            val sld = target as? Slider ?: (target as? Container<*>)?.actor as? Slider
                            sld?.let { callback(channel.actionKey, it.value) }
                        }
                    })
                }
                else -> { /* No-op */ }
            }
            actor
        }

        // v0.3.32: Re-enforce stable 350x330 footprint.
        setSize(350f, 330f)
        loadoutBar.setSize(250f, 70f)
        loadoutBar.setPosition(width - 250f, 255f)
        clusterInterpreter.setSize(350f, 250f)
        clusterInterpreter.setPosition(0f, 0f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        // TVM v0.4.0/v0.4.1: Signal feedback loop is now handled internally by actors created in HUDFactory
    }

    fun getSlotButton(index: Int): HUDModuleSlotButton? {
        if (index < 0 || index >= loadoutBar.children.size) return null
        return loadoutBar.getChild(index) as? HUDModuleSlotButton
    }
}
