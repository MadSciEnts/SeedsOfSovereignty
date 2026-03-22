package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Defensive Shield Module.
 * Truth on Disk: Implements ShipModule with unified intent properties.
 */
class ShieldModuleComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.SHIELD
    override val name = "Aegis Mk I"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override val commandPosition = Vector2()

    // TRUTH ON DISK: Shields use WORLD space for the joystick reticle.
    // This ensures the touchpad input is in CAMERA space, matching the world-locked idle ring.
    override var commandSpace = CoordinateSpace.WORLD
    override var orientationReality = TacticalOrientationReality.AWAY_FROM_SHIP
    override var worldAimAngle: Float = 90f

    override fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 1.2f
    override fun getTacticalRotation(): Float = 0f

    var shieldStrength = 500f
    var maxShieldStrength = 500f
    var rechargeRate = 10f

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl("TOUCHPAD", "target_position", 112.5f, 75f, 225f, 225f),
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
        commandPosition.set(0f, 0f)
        commandSpace = CoordinateSpace.WORLD
        orientationReality = TacticalOrientationReality.AWAY_FROM_SHIP
        worldAimAngle = 90f
        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<ShieldModuleComponent>()
    }
}
