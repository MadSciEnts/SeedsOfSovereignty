package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

class EvolutionManager(private val world: World) {
    fun cheatEvolve() {
        val family = world.family { all(PlayerComponent, EvolutionComponent) }
        if (family.numEntities > 0) {
            val player = family.first()
            with(world) {
                val evo = player[EvolutionComponent]
                evo.essenceCollected = evo.essenceRequired
            }
        }
    }
}
