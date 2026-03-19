package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2

// The physical location in the game world
data class PositionComponent(val pos: Vector2 = Vector2()) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

// Speed, direction, and physical constraints
data class VelocityComponent(
    val v: Vector2 = Vector2(),
    var acceleration: Float = 0f,
    var friction: Float = 2.0f,
    var maxSpeed: Float = 15f
) : Component<VelocityComponent> {
    override fun type() = VelocityComponent
    companion object : ComponentType<VelocityComponent>()
}

// Marks the player-controlled ship core
class PlayerComponent : Component<PlayerComponent> {
    override fun type() = PlayerComponent
    companion object : ComponentType<PlayerComponent>()
}
