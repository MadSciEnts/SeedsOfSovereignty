package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.get
import ktx.ashley.mapperFor

/**
 * Module component for ship propulsion.
 * Truth on Disk: Defines thrust power and rotational limits for the ship.
 */
class ImpulseEngineComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.ENGINE
    override val name = "Impulse Engine"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override val commandPosition = Vector2()
    override var commandSpace = CoordinateSpace.OBJECT
    override var orientationReality = TacticalOrientationReality.SHIP_RELATIVE
    override var worldAimAngle: Float = 90f

    var thrustPower = 15f

    /**
     * Maximum degrees per second the ship can rotate.
     * Truth on Disk: Controls the responsiveness of the ship's orientation.
     */
    var maxRotationSpeed = 180f

    var isActive = false

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl(
            type = "JOYSTICK",
            channelName = "navigation",
            x = 37.5f,
            y = 75f,
            width = 300f,
            height = 300f
        )
    )

    override fun reset() {
        thrustPower = 15f
        maxRotationSpeed = 180f
        isActive = false
        commandPosition.set(0f, 0f)
        commandSpace = CoordinateSpace.OBJECT
        orientationReality = TacticalOrientationReality.SHIP_RELATIVE
        worldAimAngle = 90f
        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<ImpulseEngineComponent>()
    }
}
