package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.*
import ktx.ashley.mapperFor

/**
 * Modular Primary or Secondary Weapon.
 * Truth on Disk: The module controls the tactical projection parameters.
 */
class WeaponModuleComponent : Component, Pool.Poolable, ShipModule {
    override var category = ModuleCategory.WEAPON_PRIMARY
    override var name = "Weapon System"
    override val tacticalState = TacticalState()

    // --- ShipModule Interface: Tactical Control ---
    override fun getProjectedRadius(shipRadius: Float): Float = shipRadius * 1.5f
    override fun getTacticalRotation(): Float = platformRotation

    var projectileName = "Standard Bolt"

    var damage = 25f
    var fireRate = 0.5f
    var projectileSpeed = 45f
    var damageType = DamageType.KINETIC

    var cooldownTimer = 0f
    var isSecondary = false

    var subtier: Subtier = Subtier.A

    // Attack Platform orientation
    var platformRotation = 0f

    // --- Intent Projection State ---
    val commandPosition = Vector2()

    var commandSpace = CoordinateSpace.WORLD

    // Visual Scales
    var barrelWidth = 0.2f
    var barrelHeight = 0.6f
    var visuals = VisualMetadata()

    private val cachedOffsets = mutableListOf<Vector2>()
    private var lastCalculatedSubtier: Subtier? = null

    fun getBarrelOffsets(): List<Vector2> {
        if (subtier == lastCalculatedSubtier && cachedOffsets.isNotEmpty()) {
            return cachedOffsets
        }
        cachedOffsets.clear()
        when (subtier) {
            Subtier.A -> cachedOffsets.add(Vector2(0f, 0.5f))
            Subtier.B -> {
                cachedOffsets.add(Vector2(-0.25f, 0.5f))
                cachedOffsets.add(Vector2(0.25f, 0.5f))
            }
            Subtier.C -> {
                cachedOffsets.add(Vector2(-0.4f, 0.4f))
                cachedOffsets.add(Vector2(0f, 0.6f))
                cachedOffsets.add(Vector2(0.4f, 0.4f))
            }
        }
        lastCalculatedSubtier = subtier
        return cachedOffsets
    }

    override fun getModuleControls(): List<ModuleControl> {
        val controls = mutableListOf<ModuleControl>()
        controls.add(ModuleControl("LABEL", "ammo_type", 37.5f, 380f, 300f, 40f))
        if (isSecondary) {
            controls.add(ModuleControl("FIRE_BUTTON", "fire_button", 87.5f, 75f, 200f, 200f))
        } else {
            controls.add(ModuleControl("JOYSTICK", "weapon_aim", 37.5f, 75f, 300f, 300f))
        }
        return controls
    }

    override fun getTacticalAttributes(): List<TacticalAttribute> {
        return listOf(
            TacticalAttribute("COOLDOWN", cooldownTimer, fireRate, TacticalAttributeType.CONTINUOUS, 0, Color.RED, TacticalProjection.FLAT),
            TacticalAttribute("AIM", platformRotation, 360f, TacticalAttributeType.DIRECTIONAL, 0, Color.RED, TacticalProjection.PROJECTED)
        )
    }

    override fun reset() {
        damage = 25f
        fireRate = 0.5f
        projectileSpeed = 45f
        damageType = DamageType.KINETIC
        cooldownTimer = 0f
        isSecondary = false
        subtier = Subtier.A
        platformRotation = 0f
        commandPosition.set(0f, 0f)
        projectileName = "Standard Bolt"
        category = ModuleCategory.WEAPON_PRIMARY
        name = "Weapon System"
        barrelWidth = 0.2f
        barrelHeight = 0.6f
        visuals = VisualMetadata()
        cachedOffsets.clear()
        lastCalculatedSubtier = null

        tacticalState.reset()
    }

    companion object {
        val mapper = mapperFor<WeaponModuleComponent>()
    }
}
