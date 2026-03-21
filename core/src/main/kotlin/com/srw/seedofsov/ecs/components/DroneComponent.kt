package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Marks an entity as a Drone belonging to a parent ship.
 * Truth on Disk: Implements individual phase tracking for organic orientation-independent spacing.
 */
class DroneComponent : Component, Pool.Poolable {
    var parent: Entity? = null
    var type = "OFFENSIVE" // OFFENSIVE, DEFENSIVE, REPAIR

    var fireTimer = 0f
    var fireRate = 0.8f

    // Combat tracking
    var combatTarget: Entity? = null

    // --- Orbital Phase Logic ---
    // The current angle (0..360) on the orbital curve.
    // This is world-aligned and independent of ship rotation.
    var orbitPhase = -1f

    override fun reset() {
        parent = null
        type = "OFFENSIVE"
        fireTimer = 0f
        fireRate = 0.8f
        combatTarget = null
        orbitPhase = -1f
    }

    companion object {
        val mapper = mapperFor<DroneComponent>()
    }
}
