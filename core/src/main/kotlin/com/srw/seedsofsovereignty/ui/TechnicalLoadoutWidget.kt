package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical HUD Orchestrator (v0.2.0).
 * Manages the persistent lifecycle of HUDModuleSlots.
 */
class TechnicalLoadoutWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : Table(skin) {

    private val slotMap = mutableMapOf<Int, HUDModuleSlot>()

    init {
        left().top()
        defaults().padRight(15f).size(64f, 64f)
    }

    /**
     * Synchronizes the HUD visuals with the ship's hardware topology.
     */
    fun update(player: Entity) {
        with(world) {
            if (!player.has(ModuleSlotComponent)) return
            val slots = player[ModuleSlotComponent].slots

            if (slotMap.size != slots.size) {
                rebuildLayout(slots)
            }

            slots.forEachIndexed { index, slot ->
                val hudSlot = slotMap[index] ?: return@forEachIndexed
                syncSlotData(player, hudSlot, slot)
            }
        }
    }

    private fun rebuildLayout(slots: List<ModuleSlot>) {
        clearChildren()
        slotMap.clear()

        slots.forEachIndexed { index, slot ->
            val hudSlot = HUDModuleSlot(drawer, skin, slot.type)
            slotMap[index] = hudSlot
            add(hudSlot)
        }

        // Force immediate validation so coordinates are available for animations
        invalidateHierarchy()
        validate()
        pack()
    }

    private fun syncSlotData(player: Entity, hudSlot: HUDModuleSlot, slot: ModuleSlot) {
        val slotEntity = slot.slotEntity
        with(world) {
            when {
                // Modern Architecture: Data is on a child entity
                slotEntity != null && world.contains(slotEntity) -> {
                    if (slotEntity.has(WeaponModuleComponent)) {
                        val w = slotEntity[WeaponModuleComponent]
                        hudSlot.updateDNA(w.projectileTier, w.projectileColor)
                    } else if (slotEntity.has(DroneModuleComponent)) {
                        val d = slotEntity[DroneModuleComponent]
                        hudSlot.updateDNA(d.weaponDNA.projectileTier, d.weaponDNA.projectileColor)
                    }
                }
                // Legacy/Stock Fallback: Data is directly on the ship hull (T1-T4)
                slot.type == SlotType.WEAPON && player.has(WeaponModuleComponent) -> {
                    val w = player[WeaponModuleComponent]
                    hudSlot.updateDNA(w.projectileTier, w.projectileColor)
                }
                else -> {
                    hudSlot.updateDNA(slot.equippedTier, Color.DARK_GRAY, true)
                }
            }
        }
    }

    fun getSlot(index: Int): HUDModuleSlot? = slotMap[index]
}
