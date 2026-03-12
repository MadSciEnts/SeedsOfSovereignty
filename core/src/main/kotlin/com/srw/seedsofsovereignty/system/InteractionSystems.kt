package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Unified system for Vacuum collection and Tractor beams (v2.7.5).
 * FIXES:
 * 1. [GSF 0.1] - Scaled collection radii and pull forces.
 */
class SalvageSystem : IteratingSystem(family { all(SalvageComponent, PositionComponent, VelocityComponent) }) {
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, EvolutionComponent) } }

    // GSF 0.1: Reduced by factor of 100 (squared distance)
    private val pullRadiusSq = 625f // 25u squared (was 250u)
    private val collectRadiusSq = 16f // 4u squared (was 40u)

    override fun onTickEntity(entity: Entity) {
        val salvage = entity[SalvageComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v

        if (salvage.isCollected) return

        if (salvage.collectTimer > 0f) {
            salvage.collectTimer -= deltaTime
            return
        }

        playerFamily.firstOrNull()?.let { player ->
            val playerPos = player[PositionComponent].pos
            val distSq = pos.dst2(playerPos)

            if (distSq < pullRadiusSq) {
                // GSF 0.1: Pull force reduced from 1200 to 120
                val pull = Vector2(playerPos).sub(pos).nor().scl(120f * deltaTime)
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
