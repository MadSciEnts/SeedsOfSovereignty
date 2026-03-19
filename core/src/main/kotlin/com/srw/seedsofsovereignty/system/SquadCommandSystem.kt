package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.utils.IntMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.OrderComponent
import com.srw.seedsofsovereignty.component.SquadComponent
import com.srw.seedsofsovereignty.component.SquadRole

/**
 * SQUAD COMMAND SYSTEM: Synchronizes OrderComponent data from Squad Leaders to Members.
 * Adheres to Fleks 2.x context-based syntax and zero-allocation updates.
 * Optimized for O(N) single-pass or deterministic two-pass synchronization.
 */
class SquadCommandSystem : IteratingSystem(family { all(SquadComponent, OrderComponent) }) {
    private val leaderMap = IntMap<Entity>()

    override fun onTick() {
        // Pass 1: Clear and Populate Leader Map
        leaderMap.clear()
        world.family { all(SquadComponent, OrderComponent) }.forEach { entity ->
            val squad = entity[SquadComponent]
            if (squad.role == SquadRole.LEADER) {
                leaderMap.put(squad.squadId, entity)
            }
        }

        // Pass 2: Iterate and sync (super.onTick() calls onTickEntity)
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val squad = entity[SquadComponent]

        if (squad.role == SquadRole.MEMBER) {
            val leader = leaderMap.get(squad.squadId, Entity.NONE)
            if (leader != Entity.NONE) {
                syncOrder(leader, entity)
            }
        }
    }

    private fun syncOrder(leader: Entity, member: Entity) {
        val leaderOrder = leader[OrderComponent]
        val memberOrder = member[OrderComponent]

        // Sync data fields (Zero-allocation Vector2 copy)
        memberOrder.type = leaderOrder.type
        memberOrder.targetPos.set(leaderOrder.targetPos)
        memberOrder.targetEntity = leaderOrder.targetEntity
    }
}
