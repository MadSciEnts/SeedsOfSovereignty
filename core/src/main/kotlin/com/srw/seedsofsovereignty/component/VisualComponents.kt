package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * VISUAL REPRESENTATION
 */

// The main body shape of an entity
data class ShapeComponent(
    var type: ShapeType = ShapeType.TRIANGLE,
    var color: Color = Color.WHITE,
    var size: Float = 1f,
    var rotation: Float = 0f
) : Component<ShapeComponent> {
    override fun type() = ShapeComponent
    companion object : ComponentType<ShapeComponent>()
}

enum class ShapeType { TRIANGLE, SQUARE, CIRCLE, HEXAGON }

// Visual decorators for weapons (the barrels)
data class BarrelVisualComponent(
    var length: Float = 0.4f,
    var width: Float = 0.1f,
    var color: Color = Color.GRAY
) : Component<BarrelVisualComponent> {
    override fun type() = BarrelVisualComponent
    companion object : ComponentType<BarrelVisualComponent>()
}
