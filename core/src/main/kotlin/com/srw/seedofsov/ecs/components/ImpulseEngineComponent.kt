package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.get
import ktx.ashley.mapperFor

/**
 * Module component for ship propulsion.
 */
class ImpulseEngineComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.ENGINE
    override val name = "Impulse Engine"
    override val tacticalState = TacticalState()

    var thrustPower = 10f
    var isActive = false

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl(
            type = "JOYSTICK",
            channelName = "navigation",
            x = 37.5f, // Centered in 375px area (300px joystick)
            y = 75f,
            width = 300f,
            height = 300f
        )
    )

    override fun reset() {
        thrustPower = 15f
        isActive = false

        // Reset Tactical State
        tacticalState.worldTargetPos.set(0f, 0f)
        tacticalState.lastStableAwayAngle = 90f
        tacticalState.heightSquish = 1f
        tacticalState.aimTransition = 0f
        tacticalState.selectionAnimation = 0f
        tacticalState.isSelected = false
        tacticalState.layoutOrbitIndex = 0
        tacticalState.lerpedOffset.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<ImpulseEngineComponent>()
    }
}
