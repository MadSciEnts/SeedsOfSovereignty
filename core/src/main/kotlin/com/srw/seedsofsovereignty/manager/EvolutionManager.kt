package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ModuleGenerator

/**
 * Manages ship progression and hardware synchronization.
 */
class EvolutionManager {

    fun cheatEvolve(world: World) {
        val family = world.family { all(PlayerComponent, EvolutionComponent) }
        if (family.numEntities > 0) {
            val player = family.first()
            with(world) {
                val evo = player[EvolutionComponent]
                evo.essenceCollected = evo.essenceRequired
            }
        }
    }

    /**
     * Applies the mandatory hardware baseline for a new Tier.
     */
    fun applyTierBaseline(world: World, player: Entity, tier: Int) {
        with(world) {
            val evo = player[EvolutionComponent]
            val weapon = player[WeaponComponent]
            val module = player[WeaponModuleComponent]

            evo.currentTier = tier
            evo.subTier = 1

            weapon.barrels = 1
            weapon.currentBarrel = 0
            weapon.barrelSequence.clear()
            weapon.barrelSequence.add(0)

            val stockWeapon = ModuleGenerator.generateWeapon(tier, Rarity.COMMON).apply {
                name = "STOCK T$tier"
                description = "Standard issue hardware for your new chassis."
            }
            module.name = stockWeapon.name
            module.description = stockWeapon.description
            module.projectileTier = stockWeapon.projectileTier
            module.rarity = stockWeapon.rarity
            module.fireRate = stockWeapon.fireRate
            module.projectileSpeed = stockWeapon.projectileSpeed
            module.projectileColor = stockWeapon.projectileColor
            module.effects.clear()
            module.effects.addAll(stockWeapon.effects)

            syncDroneSlots(world, player, tier)

            if (player.has(ModuleSlotComponent)) {
                val slotComp = player[ModuleSlotComponent]
                slotComp.slots.find { it.type == SlotType.WEAPON }?.let { ws ->
                    ws.equippedName = module.name
                    ws.equippedTier = module.projectileTier
                }
            }
        }
    }

    private fun syncDroneSlots(world: World, player: Entity, tier: Int) {
        with(world) {
            val slotComp = if (player.has(ModuleSlotComponent)) player[ModuleSlotComponent] else {
                player.configure { it += ModuleSlotComponent() }
                player[ModuleSlotComponent]
            }

            val targetDroneSlots = when {
                tier >= 13 -> 4
                tier >= 11 -> 3
                tier >= 9 -> 2
                tier >= 5 -> 1
                else -> 0
            }

            val currentDroneSlots = slotComp.slots.count { it.type == SlotType.DRONE }

            for (i in currentDroneSlots until targetDroneSlots) {
                val newSlotIndex = i
                val sEntity = world.entity { e ->
                    e += DroneLauncherComponent(slotIndex = newSlotIndex)
                    e += ModuleGenerator.generateDroneModule(tier, Rarity.COMMON)
                }

                val dModule = sEntity[DroneModuleComponent]
                // Fix: Ensure the slotEntity reference is stored in the topology
                slotComp.slots.add(ModuleSlot(
                    type = SlotType.DRONE,
                    tier = tier,
                    equippedName = dModule.name,
                    equippedTier = tier,
                    slotEntity = sEntity
                ))
            }
        }
    }
}
