package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

class MarketManager(private val world: World) {

    fun swapModule(player: Entity, station: Entity) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(WeaponModuleComponent)) return

            val market = station[MarketComponent]
            val newModule = market.offeredModule ?: return

            val currentModule = player[WeaponModuleComponent]

            currentModule.name = newModule.name
            currentModule.projectileTier = newModule.projectileTier
            currentModule.damage = newModule.damage
            currentModule.projectileSpeed = newModule.projectileSpeed
            currentModule.fireRate = newModule.fireRate

            market.offeredModule = null
        }
    }

    fun scrapModule(player: Entity, station: Entity) {
        with(world) {
            if (!station.has(MarketComponent) || !player.has(EvolutionComponent)) return

            val market = station[MarketComponent]
            val module = market.offeredModule ?: return

            val scrapValue = (module.projectileTier * 5).coerceAtLeast(10)
            player[EvolutionComponent].essenceCollected += scrapValue

            market.offeredModule = null
        }
    }
}
