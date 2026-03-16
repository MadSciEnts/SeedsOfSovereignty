package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2

/**
 * COMBAT & WEAPONRY
 */

data class WeaponModuleComponent(
    var evolution: SubTier = SubTier.A,
    var fireRate: Float = 0.5f,      // Time between individual barrel shots
    var currentCooldown: Float = 0f,
    var barrelIndex: Int = 0,
    val barrelOffsets: MutableList<Vector2> = mutableListOf(Vector2(0f, 0f)),
    var damage: Float = 10f,
    var projectileSpeed: Float = 500f
) : Component<WeaponModuleComponent> {
    override fun type() = WeaponModuleComponent
    companion object : ComponentType<WeaponModuleComponent>()
}

/**
 * DATA CHANNELS: Standardizes communication between the Command Matrix (UI)
 * and the Fleks Systems (Logic).
 */

data class InputChannelData(
    var vector: Vector2 = Vector2(),
    var value: Float = 0f,
    var triggered: Boolean = false
)

data class OutputChannelData(
    var status: Float = 0f,         // e.g., Heat, Charge, Ammo %
    var active: Boolean = false,
    var message: String = ""
)

data class ModuleChannelsComponent(
    val type: ModuleType,
    val inputs: MutableMap<String, InputChannelData> = mutableMapOf(),
    val outputs: MutableMap<String, OutputChannelData> = mutableMapOf()
) : Component<ModuleChannelsComponent> {
    override fun type() = ModuleChannelsComponent
    companion object : ComponentType<ModuleChannelsComponent>()
}

/**
 * PAYLOADS: The "Required Data" for what a module actually produces.
 */

data class ProjectileBlueprint(
    val type: ProjectileType,
    val damage: Float,
    val speed: Float,
    val lifeTime: Float = 2f,
    val color: com.badlogic.gdx.graphics.Color = com.badlogic.gdx.graphics.Color.WHITE
)

enum class ProjectileType { KINETIC, ENERGY, TORPEDO }

data class ProjectilePayloadComponent(
    val blueprint: ProjectileBlueprint
) : Component<ProjectilePayloadComponent> {
    override fun type() = ProjectilePayloadComponent
    companion object : ComponentType<ProjectilePayloadComponent>()
}

// Tag and state for projectiles in flight
data class ProjectileComponent(
    var lifeTime: Float = 0f
) : Component<ProjectileComponent> {
    override fun type() = ProjectileComponent
    companion object : ComponentType<ProjectileComponent>()
}

// Stores info about who fired it to avoid self-collision
data class FactionComponent(
    val type: FactionType
) : Component<FactionComponent> {
    override fun type() = FactionComponent
    companion object : ComponentType<FactionComponent>()
}

enum class FactionType { PLAYER, ENEMY, NEUTRAL }
