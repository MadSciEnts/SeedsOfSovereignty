package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

class ShipAnalysisTable(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val world: World,
    private val onAction: (Action) -> Unit
) : Table(skin) {

    enum class Action { REPAIR, UNDOCK, GOTO_WEAPONS, GOTO_DRONES }

    private val blueprintWidget = ShipPreviewWidget(drawer)
    private val statsTable = Table(skin)
    private val slotsTable = Table(skin)

    init {
        pad(20f)
        refresh()
    }

    fun refresh() {
        clearChildren()
        statsTable.clear()
        slotsTable.clear()

        // LEFT COLUMN: CORE ANALYSIS + COMMAND CONSOLE
        statsTable.add(Label("CORE SYSTEMS ANALYSIS", skin, "title")).padBottom(20f).row()

        val player = world.family { all(PlayerComponent, HealthComponent, ShieldComponent, EvolutionComponent) }.firstOrNull()
        if (player != null) {
            with(world) {
                val hp = player[HealthComponent]
                val sp = player[ShieldComponent]
                val evo = player[EvolutionComponent]

                addStatRow("STRUCTURAL", hp.current, hp.max, Color.FIREBRICK)
                addStatRow("DEFLECTOR", sp.current, sp.max, Color.SKY)
                addStatRow("EVO-LINK", evo.essenceCollected.toFloat(), evo.essenceRequired.toFloat(), Color.GOLD)
            }
        }

        // Command Buttons
        statsTable.add(Label("STATION SERVICES", skin, "title")).padTop(30f).padBottom(15f).row()
        val repairBtn = UIButtonFactory.createButton(skin, "REPAIR HULL", UIButtonFactory.ButtonType.GENERIC) {
            onAction(Action.REPAIR)
        }
        val undockBtn = UIButtonFactory.createButton(skin, "INITIATE UNDOCK", UIButtonFactory.ButtonType.GENERIC) {
            onAction(Action.UNDOCK)
        }
        statsTable.add(repairBtn).size(320f, 70f).padBottom(10f).row()
        statsTable.add(undockBtn).size(320f, 70f).row()

        // RIGHT COLUMN: HARDWARE TOPOLOGY
        slotsTable.add(Label("HARDWARE TOPOLOGY", skin, "title")).padBottom(20f).row()

        if (player != null) {
            with(world) {
                val playerEvo = player[EvolutionComponent]
                if (player.has(ModuleSlotComponent)) {
                    player[ModuleSlotComponent].slots.forEach { slot ->
                        // Task Fix: Do not show drone slots until player reaches Tier 5 (Rule #8)
                        if (slot.type == SlotType.DRONE && playerEvo.currentTier < 5) return@forEach

                        val slotAction = when (slot.type) {
                            SlotType.WEAPON -> Action.GOTO_WEAPONS
                            SlotType.DRONE -> Action.GOTO_DRONES
                            else -> Action.GOTO_WEAPONS
                        }

                        // Determine Icon based on equipped module DNA
                        val icon = if (slot.type == SlotType.WEAPON) {
                            val module = player[WeaponModuleComponent]
                            ProjectileIcon(drawer, skin, module.projectileTier, 1.5f, module.projectileColor)
                        } else if (slot.type == SlotType.DRONE && slot.slotEntity != null) {
                            val dEntity = slot.slotEntity!!
                            if (dEntity.has(DroneModuleComponent)) {
                                val dModule = dEntity[DroneModuleComponent]
                                ProjectileIcon(drawer, skin, dModule.weaponDNA.projectileTier, 1.5f, dModule.weaponDNA.projectileColor)
                            } else null
                        } else null

                        val slotButton = UIButtonFactory.createButton(skin, "T${slot.tier} ${slot.type} - ${slot.equippedName}", UIButtonFactory.ButtonType.MODULE, icon) {
                            onAction(slotAction)
                        }
                        slotsTable.add(slotButton).width(320f).height(75f).padBottom(10f).row()
                    }
                } else {
                    slotsTable.add(Label("NO MODULE SLOTS DETECTED", skin)).row()
                }
            }
        }

        add(statsTable).expand().left().top()
        add(blueprintWidget).size(450f).expand().center()
        add(slotsTable).expand().right().top()
    }

    private fun addStatRow(name: String, cur: Float, max: Float, color: Color) {
        val row = Table(skin)
        row.add(Label(name, skin, "medium")).width(120f).left()
        val bar = StatBar(drawer, skin, cur, max, color)
        row.add(bar).size(180f, 22f).padLeft(10f)
        statsTable.add(row).padBottom(15f).row()
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        super.drawChildren(batch, parentAlpha)
        drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.3f * parentAlpha)
        drawer.rectangle(blueprintWidget.x, blueprintWidget.y, blueprintWidget.width, blueprintWidget.height, 1f)
        val scanY = blueprintWidget.y + (System.currentTimeMillis() % 2000 / 2000f) * blueprintWidget.height
        drawer.line(blueprintWidget.x, scanY, blueprintWidget.x + blueprintWidget.width, scanY, 2f)
    }
}
