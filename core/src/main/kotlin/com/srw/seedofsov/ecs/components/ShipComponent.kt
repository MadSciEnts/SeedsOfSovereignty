package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.ModuleCategory
import com.srw.seedofsov.model.ModuleSlot
import ktx.ashley.mapperFor

/**
 * Master manifest for a ship's equipped modules, organized into a list of slots.
 * Truth on Disk: Supports multiple slots of the same category (e.g., multiple Drone Bays).
 */
class ShipComponent : Component, Pool.Poolable {
    // List of all slots on the ship
    val slots = mutableListOf<ModuleSlot>()

    /**
     * Adds a new slot of the specified category to the ship.
     */
    fun addSlot(category: ModuleCategory): ModuleSlot {
        val newSlot = ModuleSlot(category)
        slots.add(newSlot)
        return newSlot
    }

    /**
     * Returns the first slot found for a given category.
     * Primarily used for singleton systems like Engine or Power Core.
     */
    fun getFirstSlot(category: ModuleCategory): ModuleSlot? {
        return slots.find { it.category == category }
    }

    /**
     * Alias for getFirstSlot to satisfy various systems.
     */
    fun getSlot(category: ModuleCategory): ModuleSlot? = getFirstSlot(category)

    /**
     * Returns all slots belonging to a specific category.
     */
    fun getSlots(category: ModuleCategory): List<ModuleSlot> {
        return slots.filter { it.category == category }
    }

    override fun reset() {
        slots.clear()
    }

    companion object {
        val mapper = mapperFor<ShipComponent>()
    }
}
