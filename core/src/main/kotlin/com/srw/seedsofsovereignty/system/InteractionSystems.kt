package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Unified system for Vacuum collection and Tractor beams (v2.4.4).
 * Implements Rule #10: The Salvage Pipeline.
 *
 * CLEANUP: Purged duplicate SensorSystem and StationSystem.
 */
class SalvageSystem : IteratingSystem(family { all(SalvageComponent, PositionComponent, VelocityComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, EvolutionComponent) } }
    private val pullRadiusSq = 62500f
    private val collectRadiusSq = 1600f // 40u - slightly larger for better collection feel

    override fun onTickEntity(entity: Entity) {
        val salvage = entity[SalvageComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v

        if (salvage.isCollected) return

        // 1. Task Fix: Process Grace Period (Ensures item is rendered before collection)
        if (salvage.collectTimer > 0f) {
            salvage.collectTimer -= deltaTime
            return
        }

        // 2. Locate Player for Vacuum Pull
        playerFamily.firstOrNull()?.let { player ->
            val playerPos = player[PositionComponent].pos
            val distSq = pos.dst2(playerPos)

            if (distSq < pullRadiusSq) {
                val pull = Vector2(playerPos).sub(pos).nor().scl(1200f * deltaTime)
                vel.add(pull)

                if (distSq < collectRadiusSq) {
                    salvage.isCollected = true
                    resolveReward(player, salvage)
                    world -= entity
                }
            }
        }
    }

    private fun resolveReward(player: Entity, salvage: SalvageComponent) {
        with(world) {
            when (salvage.type) {
                SalvageType.ESSENCE -> {
                    val evo = player[EvolutionComponent]
                    evo.essenceCollected += salvage.amount
                }
                SalvageType.SCRAP -> {
                    // Resource management will be implemented in v0.2.0
                }
                else -> {}
            }
        }
    }
}
