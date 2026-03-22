package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Manages the deployment and tracking of Fighters (Epoch 2) or Drones (Epoch 1).
 * Truth on Disk: Implements ShipModule with direct world orientation reporting.
 */
class BayModuleComponent : Component, Pool.Poolable, ShipModule {
    override var category = ModuleCategory.BAY
    override var name = "Drone Bay"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override val commandPosition = Vector2()

    // TRUTH ON DISK: Drones use WORLD space for the joystick reticle.
    // This ensures the touchpad input is in CAMERA space, matching the world-locked idle ring.
    override var commandSpace = CoordinateSpace.WORLD
    override var orientationReality = TacticalOrientationReality.AWAY_FROM_SHIP
    override var worldAimAngle: Float = 0f

    override fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 2.8f
    override fun getTacticalRotation(): Float = swarmRotation

    var capacity = 4
    var launchRate = 3f
    var launchTimer = 0f
    val activeUnits = mutableListOf<Entity>()

    var unitType = "Drone"
    var subtier: Subtier = Subtier.A

    // Swarm State (Specific to Bays)
    var swarmRotation = 0f

    // Configurable Physics
    var orbitSpeed = 0.7f

    var isLaunchRequested = false

    override fun getModuleControls(): List<ModuleControl> {
        return listOf(
            ModuleControl("TOUCHPAD", "target_position", 112.5f, 75f, 225f, 225f),
            ModuleControl("SLIDER", "orbit_speed", 350f, 75f, 40f, 225f),
            ModuleControl("CYCLE_BUTTON", "cycle_space", 37.5f, 157.5f, 60f, 60f),
            ModuleControl("BUTTON", "launch_unit", 37.5f, 75f, 60f, 60f),
            ModuleControl("LABEL", "unit_count", 37.5f, 310f, 300f, 40f)
        )
    }

    override fun getTacticalAttributes(): List<TacticalAttribute> {
        return listOf(
            TacticalAttribute("SWARM", activeUnits.size.toFloat(), capacity.toFloat(), TacticalAttributeType.DISCRETE, 1, Color.CYAN),
            TacticalAttribute("SPIN", swarmRotation, 360f, TacticalAttributeType.DIRECTIONAL, 1, Color.CYAN)
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
        orbitSpeed = 0.7f
        commandPosition.set(0f, 0f)
        commandSpace = CoordinateSpace.WORLD
        orientationReality = TacticalOrientationReality.AWAY_FROM_SHIP
        worldAimAngle = 0f
        isLaunchRequested = false

        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<BayModuleComponent>()
    }
}
