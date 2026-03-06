package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

class MarketManager(private val world: World) {

    fun swapModule(player: Entity, station: Entity, moduleIndex: Int) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(WeaponModuleComponent)) return

            val market = station[MarketComponent]
            if (moduleIndex < 0 || moduleIndex >= market.weaponOffers.size) return

            val newModule = market.weaponOffers[moduleIndex]
            val currentModule = player[WeaponModuleComponent]

            // Transfer DNA
            currentModule.name = newModule.name
            currentModule.projectileTier = newModule.projectileTier
            currentModule.damage = newModule.damage
            currentModule.projectileSpeed = newModule.projectileSpeed
            currentModule.fireRate = newModule.fireRate

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
            val scrapValue = (module.projectileTier * 10).coerceAtLeast(20)

            player[EvolutionComponent].essenceCollected += scrapValue

            // Remove from station inventory
            market.weaponOffers.removeAt(moduleIndex)
        }
    }
}
