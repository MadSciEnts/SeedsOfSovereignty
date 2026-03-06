package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

class MarketManager(private val world: World) {

    /**
     * Swaps the player's equipped module with a station offer.
     * Enforces Rule #1: Tier Gating and ensures UI Slot Sync.
     */
    fun swapModule(player: Entity, station: Entity, moduleIndex: Int) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(WeaponModuleComponent) || !player.has(EvolutionComponent)) return

            val market = station[MarketComponent]
            if (moduleIndex < 0 || moduleIndex >= market.weaponOffers.size) return

            val newModule = market.weaponOffers[moduleIndex]
            val playerTier = player[EvolutionComponent].currentTier

            if (newModule.projectileTier != playerTier) return

            val currentModule = player[WeaponModuleComponent]

            // 1. Transfer Data-Driven DNA to the active component
            currentModule.name = newModule.name
            currentModule.description = newModule.description
            currentModule.projectileTier = newModule.projectileTier
            currentModule.rarity = newModule.rarity
            currentModule.projectileSpeed = newModule.projectileSpeed
            currentModule.fireRate = newModule.fireRate
            currentModule.projectileColor = newModule.projectileColor
            currentModule.effects.clear()
            currentModule.effects.addAll(newModule.effects)

            // 2. SYNC HARDWARE TOPOLOGY: Update the Slot Entry so ShipAnalysis view is accurate
            if (player.has(ModuleSlotComponent)) {
                val slotComp = player[ModuleSlotComponent]
                // Find the first weapon slot and update its display data
                slotComp.slots.find { it.type == SlotType.WEAPON }?.let { weaponSlot ->
                    weaponSlot.equippedName = newModule.name
                    weaponSlot.equippedTier = newModule.projectileTier
                }
            }

            // Remove from station inventory
            market.weaponOffers.removeAt(moduleIndex)
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
