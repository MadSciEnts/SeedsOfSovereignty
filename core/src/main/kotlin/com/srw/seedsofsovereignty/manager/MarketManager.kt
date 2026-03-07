package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

class MarketManager(private val world: World) {

    fun swapModule(player: Entity, station: Entity, moduleIndex: Int) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(WeaponModuleComponent) || !player.has(EvolutionComponent)) return

            val market = station[MarketComponent]
            if (moduleIndex < 0 || moduleIndex >= market.weaponOffers.size) return

            val newModule = market.weaponOffers[moduleIndex]
            val playerTier = player[EvolutionComponent].currentTier

            if (newModule.projectileTier != playerTier) return

            val currentModule = player[WeaponModuleComponent]
            currentModule.name = newModule.name
            currentModule.description = newModule.description
            currentModule.projectileTier = newModule.projectileTier
            currentModule.rarity = newModule.rarity
            currentModule.projectileSpeed = newModule.projectileSpeed
            currentModule.fireRate = newModule.fireRate
            currentModule.projectileColor = newModule.projectileColor
            currentModule.effects.clear()
            currentModule.effects.addAll(newModule.effects)

            if (player.has(ModuleSlotComponent)) {
                val slotComp = player[ModuleSlotComponent]
                slotComp.slots.find { it.type == SlotType.WEAPON }?.let { weaponSlot ->
                    weaponSlot.equippedName = newModule.name
                    weaponSlot.equippedTier = newModule.projectileTier
                }
            }

            market.weaponOffers.removeAt(moduleIndex)
        }
    }

    /**
     * Swaps a drone module from station stock into a specific player drone slot.
     */
    fun swapDroneModule(player: Entity, station: Entity, moduleIndex: Int, slotIndex: Int) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(ModuleSlotComponent)) return

            val market = station[MarketComponent]
            if (moduleIndex < 0 || moduleIndex >= market.droneOffers.size) return

            val newDrone = market.droneOffers[moduleIndex]
            val slotComp = player[ModuleSlotComponent]

            // Find the specific slot entity matching the slotIndex
            val slot = slotComp.slots.filter { it.type == SlotType.DRONE }.getOrNull(slotIndex)
            val slotEntity = slot?.slotEntity ?: return

            if (!slotEntity.has(DroneModuleComponent)) return
            val currentModule = slotEntity[DroneModuleComponent]

            // Transfer Drone DNA
            currentModule.name = newDrone.name
            currentModule.category = newDrone.category
            currentModule.maxHealth = newDrone.maxHealth
            currentModule.maxDrones = newDrone.maxDrones
            currentModule.rarity = newDrone.rarity

            // Transfer internal weapon DNA
            val currentWeapon = currentModule.weaponDNA
            val newWeapon = newDrone.weaponDNA
            currentWeapon.name = newWeapon.name
            currentWeapon.projectileTier = newWeapon.projectileTier
            currentWeapon.projectileColor = newWeapon.projectileColor
            currentWeapon.effects.clear()
            currentWeapon.effects.addAll(newWeapon.effects)

            // Update topology for UI
            slot.equippedName = newDrone.name
            slot.equippedTier = newDrone.weaponDNA.projectileTier

            // Remove from station
            market.droneOffers.removeAt(moduleIndex)
        }
    }

    fun scrapModule(player: Entity, station: Entity, moduleIndex: Int) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(EvolutionComponent)) return

            val market = station[MarketComponent]
            if (moduleIndex < 0 || moduleIndex >= market.weaponOffers.size) return

            val module = market.weaponOffers[moduleIndex]
            val rarityMultiplier = when(module.rarity) {
                Rarity.COMMON -> 1
                Rarity.UNCOMMON -> 2
                Rarity.RARE -> 4
                Rarity.EPIC -> 8
                Rarity.LEGENDARY -> 16
            }
            val scrapValue = (module.projectileTier * 10 * rarityMultiplier).coerceAtLeast(20)

            player[EvolutionComponent].essenceCollected += scrapValue
            market.weaponOffers.removeAt(moduleIndex)
        }
    }
}
