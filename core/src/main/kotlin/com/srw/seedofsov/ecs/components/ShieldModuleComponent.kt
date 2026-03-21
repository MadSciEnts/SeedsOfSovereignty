package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.ModuleCategory
import com.srw.seedofsov.model.ModuleControl
import com.srw.seedofsov.model.ShipModule
import ktx.ashley.mapperFor

class ShieldModuleComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.SHIELD
    override val name = "Aegis Mk I"

    var shieldStrength = 500f
    var rechargeRate = 10f

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl("STATS", "shield_status", 10f, 10f, 200f, 100f)
    )

    override fun reset() {
        shieldStrength = 500f
        rechargeRate = 10f
    }

    companion object {
        val mapper = mapperFor<ShieldModuleComponent>()
    }
}
