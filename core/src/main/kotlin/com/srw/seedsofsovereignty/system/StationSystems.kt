package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ShipFactory

/**
 * STATION SYSTEM: Handles autonomous station behaviors like defender spawning.
 */
class StationSystem(
    private val shipFactory: ShipFactory = inject()
) : IteratingSystem(
    family { all(StationComponent, PositionComponent, FactionComponent, EvolutionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val station = entity[StationComponent]
        val pos = entity[PositionComponent].pos
        val faction = entity[FactionComponent].type
        val tier = entity[EvolutionComponent].tier

        // 1. Spawning Logic
        station.spawnTimer += deltaTime
        if (station.spawnTimer >= station.spawnInterval) {
            station.spawnTimer = 0f

            // Count existing defenders owned by this station (Future: add OwnershipComponent)
            // For now, count all same-faction entities within a range
            val defenderCount = world.family { all(FactionComponent, PositionComponent).none(StationComponent) }.filter {
                it[FactionComponent].type == faction && it[PositionComponent].pos.dst2(pos) < 2500f // 50 unit radius
            }.size

            if (defenderCount < station.maxDefenders) {
                val spawnPos = pos.cpy().add(MathUtils.random(-2f, 2f), MathUtils.random(-2f, 2f))

                // Use factory to create a defender based on station tier/faction
                if (faction == FactionType.ENEMY) {
                    val defender = shipFactory.createEnemy(world, tier, isBoss = false)
                    defender.configure { it[PositionComponent].pos.set(spawnPos) }
                } else if (faction == FactionType.PLAYER) {
                    // Logic for friendly station defenders if needed
                }
            }
        }
    }
}
