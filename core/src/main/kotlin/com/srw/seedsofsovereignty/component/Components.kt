package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * CORE PHYSICS & IDENTITY
 */

data class PositionComponent(val pos: Vector2 = Vector2()) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

data class VelocityComponent(
    val v: Vector2 = Vector2(),
    val acceleration: Float = 500f,
    val friction: Float = 2.5f
) : Component<VelocityComponent> {
    override fun type() = VelocityComponent
    companion object : ComponentType<VelocityComponent>()
}

class PlayerComponent : Component<PlayerComponent> {
    override fun type() = PlayerComponent
    companion object : ComponentType<PlayerComponent>()
}

/**
 * DEFENSE & INTEGRITY
 */

data class ArcedDefenseComponent(
    val shieldFore: Float = 100f,
    val shieldAft: Float = 100f,
    val shieldPort: Float = 100f,
    val shieldStbd: Float = 100f,
    val hullFore: Float = 100f,
    val hullAft: Float = 100f,
    val hullPort: Float = 100f,
    val hullStbd: Float = 100f
) : Component<ArcedDefenseComponent> {
    override fun type() = ArcedDefenseComponent
    companion object : ComponentType<ArcedDefenseComponent>()
}

data class PowerCoreComponent(
    var integrity: Float = 1.0f,
    var weaponPower: Float = 0.25f,
    var shieldPower: Float = 0.25f,
    var enginePower: Float = 0.25f,
    var utilityPower: Float = 0.25f,
    var baseOutput: Float = 100f,
    var throttle: Float = 1.0f
) : Component<PowerCoreComponent> {
    override fun type() = PowerCoreComponent
    companion object : ComponentType<PowerCoreComponent>()
}

/**
 * RADAR & SENSORS
 */

data class RadarComponent(
    var range: Float = 2500f,
    var sweepAngle: Float = 0f,
    val blips: MutableList<RadarBlip> = mutableListOf()
) : Component<RadarComponent> {
    override fun type() = RadarComponent
    companion object : ComponentType<RadarComponent>()
}

data class RadarBlip(
    val entityId: Int,
    val relativePos: Vector2 = Vector2(),
    val color: Color = Color.WHITE
)

data class VisibleOnRadarComponent(
    val color: Color = Color.WHITE,
    val iconType: BlipIcon = BlipIcon.DOT
) : Component<VisibleOnRadarComponent> {
    override fun type() = VisibleOnRadarComponent
    companion object : ComponentType<VisibleOnRadarComponent>()
}

enum class BlipIcon { DOT, TRIANGLE, SQUARE, DIAMOND }

/**
 * HIERARCHY & MODULES
 */

data class AttachmentComponent(
    var parent: Entity = Entity.NONE,
    val offset: Vector2 = Vector2()
) : Component<AttachmentComponent> {
    override fun type() = AttachmentComponent
    companion object : ComponentType<AttachmentComponent>()
}

data class ModuleComponent(
    val category: ModuleCategory,
    var cooldown: Float = 0f,
    var powerRequirement: Float = 10f
) : Component<ModuleComponent> {
    override fun type() = ModuleComponent
    companion object : ComponentType<ModuleComponent>()
}

enum class ModuleCategory {
    PRIMARY, SECONDARY, TERTIARY, QUADRARY, SHIELDS, POWER, DRONES, FIGHTERS
}

class SelectedModuleComponent : Component<SelectedModuleComponent> {
    override fun type() = SelectedModuleComponent
    companion object : ComponentType<SelectedModuleComponent>()
}
