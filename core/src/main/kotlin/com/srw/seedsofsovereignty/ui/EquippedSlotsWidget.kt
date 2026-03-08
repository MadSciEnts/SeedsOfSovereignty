package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * HUD Widget that displays the player's currently equipped modules (Weapons, Drones).
 * Optimized for layout stability and screen-resolution accuracy (v0.1.8).
 */
class EquippedSlotsWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : HorizontalGroup() {

    init {
        space(12f)
        pad(5f)
        fill() // Fill parent width
    }

    /**
     * Rebuilds or updates the technical icons based on ship topology.
     */
    fun update(player: Entity) {
        with(world) {
            if (!player.has(ModuleSlotComponent)) return
            val slots = player[ModuleSlotComponent].slots

            if (children.size == slots.size) {
                for (i in 0 until slots.size) {
                    val icon = getChild(i) as? ProjectileIcon ?: continue
                    updateIconData(icon, slots[i])
                }
                return
            }

            clearChildren()
            for (slot in slots) {
                val icon = ProjectileIcon(drawer, skin).apply {
                    iconScale = 1.8f
                    // Task Fix: Explicitly set HUD resting size before adding to stage
                    setSize(60f, 60f)
                }
                updateIconData(icon, slot)
                addActor(icon)
            }

            // Task Fix: Force immediate layout so coordinates are available for animations
            invalidateHierarchy()
            validate()
            pack()
        }
    }

    private fun updateIconData(icon: ProjectileIcon, slot: ModuleSlot) {
        val slotEntity = slot.slotEntity
        with(world) {
            if (slotEntity != null && world.contains(slotEntity)) {
                if (slotEntity.has(WeaponModuleComponent)) {
                    val weapon = slotEntity[WeaponModuleComponent]
                    icon.tier = weapon.projectileTier
                    icon.projectileColor = weapon.projectileColor
                } else if (slotEntity.has(DroneModuleComponent)) {
                    val drone = slotEntity[DroneModuleComponent]
                    icon.tier = drone.weaponDNA.projectileTier
                    icon.projectileColor = drone.weaponDNA.projectileColor
                }
            } else {
                icon.tier = if (slot.equippedTier > 0) slot.equippedTier else 1
                icon.projectileColor = when(slot.type) {
                    SlotType.WEAPON -> Color.YELLOW
                    SlotType.DRONE -> Color.ORANGE
                    else -> Color.GRAY
                }
            }
        }
    }
}
