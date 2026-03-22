package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Ship Power Core.
 * Truth on Disk: Implements ShipModule with unified intent properties.
 */
class PowerCoreComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.POWER_CORE
    override val name = "Fusion Core T1"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override val commandPosition = Vector2()
    override var commandSpace = CoordinateSpace.OBJECT
    override var orientationReality = TacticalOrientationReality.SHIP_RELATIVE
    override var worldAimAngle: Float = 90f

    var output = 100f
    var integrity = 1.0f

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl("SLIDER", "pwr_nav", 30f, 30f, 60f, 300f),
        ModuleControl("SLIDER", "pwr_tac", 120f, 30f, 60f, 300f),
        ModuleControl("SLIDER", "pwr_shd", 210f, 30f, 60f, 300f)
    )

    override fun reset() {
        output = 100f
        integrity = 1.0f
        commandPosition.set(0f, 0f)
        commandSpace = CoordinateSpace.OBJECT
        orientationReality = TacticalOrientationReality.SHIP_RELATIVE
        worldAimAngle = 90f
        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<PowerCoreComponent>()
    }
}
