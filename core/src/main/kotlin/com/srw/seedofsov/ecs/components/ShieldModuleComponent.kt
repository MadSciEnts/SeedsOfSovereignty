package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Defensive Shield Module.
 * Truth on Disk: The module controls the tactical projection parameters.
 */
class ShieldModuleComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.SHIELD
    override val name = "Aegis Mk I"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 1.2f
    override fun getTacticalRotation(): Float = 0f

    var shieldStrength = 500f
    var maxShieldStrength = 500f
    var rechargeRate = 10f

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl("STATS", "shield_status", 10f, 10f, 200f, 100f)
    )

    override fun getTacticalAttributes(): List<TacticalAttribute> {
        return listOf(
            TacticalAttribute("SHIELD", shieldStrength, maxShieldStrength, TacticalAttributeType.CONTINUOUS, 3, Color.GOLD)
        )
    }

    override fun reset() {
        shieldStrength = 500f
        maxShieldStrength = 500f
        rechargeRate = 10f
        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<ShieldModuleComponent>()
    }
}
