package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

/**
 * Core Spatial Data (v3.0.3).
 * FIXES:
 * 1. [SHARED VECTOR] - Changed default init to ensure unique memory instances per entity.
 */
data class PositionComponent(val pos: Vector2 = Vector2(0f, 0f)) : Component<PositionComponent> {
    override fun type() = PositionComponent
    companion object : ComponentType<PositionComponent>()
}

data class OrientationComponent(var angleRad: Float = MathUtils.PI / 2f) : Component<OrientationComponent> {
    override fun type() = OrientationComponent
    companion object : ComponentType<OrientationComponent>()
}

/**
 * Core Physics Data (v3.0.3).
 */
data class VelocityComponent(
    val v: Vector2 = Vector2(0f, 0f),
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

data class TacticalPersistenceComponent(
    val persistentInputs: MutableMap<String, Vector2> = mutableMapOf()
) : Component<TacticalPersistenceComponent> {
    override fun type() = TacticalPersistenceComponent
    companion object : ComponentType<TacticalPersistenceComponent>()
}
