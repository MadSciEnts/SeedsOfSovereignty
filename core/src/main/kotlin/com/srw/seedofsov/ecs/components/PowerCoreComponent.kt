package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

class PowerCoreComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.POWER_CORE
    override val name = "Fusion Core T1"
    override val tacticalState = TacticalState()

    var output = 100f
    var integrity = 1.0f

    override fun getModuleControls(): List<ModuleControl> = listOf(
        // 1.5x Scale: 60x300 sliders with 120px horizontal spacing
        ModuleControl("SLIDER", "pwr_nav", 30f, 30f, 60f, 300f),
        ModuleControl("SLIDER", "pwr_tac", 120f, 30f, 60f, 300f),
        ModuleControl("SLIDER", "pwr_shd", 210f, 30f, 60f, 300f)
    )

    override fun reset() {
        output = 100f
        integrity = 1.0f

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
        val mapper = mapperFor<PowerCoreComponent>()
    }
}
