package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Defensive Shield Module.
 * Truth on Disk: Supports directional quadrant reporting and priority tracking.
 */
class ShieldModuleComponent : Component, Pool.Poolable, ShipModule {
    override val category = ModuleCategory.SHIELD
    override val name = "Aegis Mk I"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override val commandPosition = Vector2()
    override var commandSpace = CoordinateSpace.WORLD
    override var orientationReality = TacticalOrientationReality.AWAY_FROM_SHIP
    override var worldAimAngle: Float = 90f

    override fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 1.2f
    override fun getTacticalRotation(): Float = 0f

    var shieldStrength = 500f
    var maxShieldStrength = 500f
    var rechargeRate = 10f

    // Directional Cache for Tactical Rendering
    val quadrantHealth = floatArrayOf(0f, 0f, 0f, 0f)
    val quadrantMax = floatArrayOf(100f, 100f, 100f, 100f)
    val quadrantActive = booleanArrayOf(true, true, true, true)
    val quadrantBuffer = floatArrayOf(0f, 0f, 0f, 0f)

    /**
     * Currently reinforced quadrant (0: Front, 1: Left, 2: Rear, 3: Right).
     * Truth on Disk: Calculated by ShieldSystem based on touchpad input.
     */
    var priorityQuadrant: Int = -1

    override fun getModuleControls(): List<ModuleControl> = listOf(
        ModuleControl("TOUCHPAD", "target_position", 112.5f, 75f, 225f, 225f),
        ModuleControl("STATS", "shield_status", 10f, 10f, 200f, 100f)
    )

    override fun getTacticalAttributes(): List<TacticalAttribute> {
        val attrs = mutableListOf<TacticalAttribute>()

        for (i in 0..3) {
            val isActive = quadrantActive[i]
            val current = if (isActive) quadrantHealth[i] else quadrantBuffer[i]
            val max = quadrantMax[i]
            val isPriority = (i == priorityQuadrant)

            // Cyan for health, Gold for charging. Brighter/Whiter if prioritized.
            val color = if (isActive) {
                if (isPriority) Color.WHITE else Color.SKY
            } else {
                if (isPriority) Color.YELLOW else Color.GOLD
            }

            attrs.add(TacticalAttribute(
                label = "QUAD_$i",
                current = current,
                max = max,
                type = TacticalAttributeType.CONTINUOUS,
                preferredQuadrant = i,
                colorOverride = color
            ))
        }

        return attrs
    }

    override fun reset() {
        shieldStrength = 500f
        maxShieldStrength = 500f
        rechargeRate = 10f
        priorityQuadrant = -1
        for (i in 0..3) {
            quadrantHealth[i] = 0f
            quadrantMax[i] = 100f
            quadrantActive[i] = true
            quadrantBuffer[i] = 0f
        }
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
