package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

data class PositionComponent(val pos: Vector2 = Vector2()) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

data class OrientationComponent(var angleRad: Float = MathUtils.PI / 2f) : Component<OrientationComponent> {
    override fun type() = OrientationComponent
    companion object : ComponentType<OrientationComponent>()
}

data class VelocityComponent(
    val v: Vector2 = Vector2(),
    var acceleration: Float = 500f,
    var friction: Float = 2.5f
) : Component<VelocityComponent> {
    override fun type() = VelocityComponent
    companion object : ComponentType<VelocityComponent>()
}

data class LifeSpanComponent(
    var remaining: Float = 2f
) : Component<LifeSpanComponent> {
    override fun type() = LifeSpanComponent
    companion object : ComponentType<LifeSpanComponent>()
}

class PlayerComponent : Component<PlayerComponent> {
    override fun type() = PlayerComponent
    companion object : ComponentType<PlayerComponent>()
}
