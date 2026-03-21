package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Manages the deployment and tracking of Fighters (Epoch 2) or Drones (Epoch 1).
 * Truth on Disk: Stores swarm-specific orientation state to ensure stability.
 */
class BayModuleComponent : Component, Pool.Poolable, ShipModule {
    override var category = ModuleCategory.BAY
    override var name = "Drone Bay"

    var capacity = 4
    var launchRate = 3f
    var launchTimer = 0f
    val activeUnits = mutableListOf<Entity>()

    var unitType = "Drone"
    var subtier: Subtier = Subtier.A

    // Swarm State
    var swarmRotation = 0f
    val lerpedOffset = Vector2() // Smoothed relative offset from ship
    val worldTargetPos = Vector2() // Absolute world position of the Hub
    var lastStableAwayAngle = 0f   // Persistent orientation of the orbit plane

    // Command Inputs
    val commandPosition = Vector2() // -1..1 touchpad offset
    var commandSpace = CoordinateSpace.OBJECT
    var isLaunchRequested = false

    override fun getModuleControls(): List<ModuleControl> {
        return listOf(
            ModuleControl("TOUCHPAD", "target_position", 112.5f, 75f, 225f, 225f),
            ModuleControl("CYCLE_BUTTON", "cycle_space", 37.5f, 157.5f, 60f, 60f),
            ModuleControl("BUTTON", "launch_unit", 37.5f, 75f, 60f, 60f),
            ModuleControl("LABEL", "unit_count", 37.5f, 310f, 300f, 40f)
        )
    }

    override fun reset() {
        capacity = 4
        launchRate = 3f
        launchTimer = 0f
        activeUnits.clear()
        unitType = "Drone"
        subtier = Subtier.A
        swarmRotation = 0f
        lerpedOffset.set(0f, 0f)
        worldTargetPos.set(0f, 0f)
        lastStableAwayAngle = 0f
        commandPosition.set(0f, 0f)
        commandSpace = CoordinateSpace.OBJECT
        isLaunchRequested = false
    }

    companion object {
        val mapper = mapperFor<BayModuleComponent>()
    }
}
