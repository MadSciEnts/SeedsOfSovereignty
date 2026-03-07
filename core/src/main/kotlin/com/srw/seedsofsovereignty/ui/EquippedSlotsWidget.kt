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
 * Optimized for compact HUD display with high-visibility icons.
 */
class EquippedSlotsWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : HorizontalGroup() {

    init {
        space(8f) // Tighter spacing for HUD
        pad(5f)
    }

    /**
     * Rebuilds the icons based on the player's current ModuleSlotComponent.
     */
    fun update(player: Entity) {
        clearChildren()

        with(world) {
            if (!player.has(ModuleSlotComponent)) return
            val slots = player[ModuleSlotComponent].slots

            for (slot in slots) {
                // We use a specialized icon scale and smaller frame bounds for the HUD
                val icon = ProjectileIcon(drawer, skin).apply {
                    // Increase the internal projectile scale relative to the frame
                    iconScale = 1.4f

                    val slotEntity = slot.slotEntity
                    if (slotEntity != null && world.contains(slotEntity)) {
                        if (slotEntity.has(WeaponModuleComponent)) {
                            val weapon = slotEntity[WeaponModuleComponent]
                            tier = weapon.projectileTier
                            projectileColor = weapon.projectileColor
                        } else if (slotEntity.has(DroneModuleComponent)) {
                            val drone = slotEntity[DroneModuleComponent]
                            tier = drone.weaponDNA.projectileTier
                            projectileColor = drone.weaponDNA.projectileColor
                        }
                    } else {
                        tier = if (slot.equippedTier > 0) slot.equippedTier else 1
                        projectileColor = when(slot.type) {
                            SlotType.WEAPON -> Color.YELLOW
                            SlotType.DRONE -> Color.ORANGE
                            else -> Color.GRAY
                        }
                    }
                }
                addActor(icon)
                // Reduced size for HUD compact look (48x48)
                icon.setSize(48f, 48f)
            }
        }
    }
}
