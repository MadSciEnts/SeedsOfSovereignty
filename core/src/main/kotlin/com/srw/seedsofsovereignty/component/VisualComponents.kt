package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * VISUAL REPRESENTATION
 */

enum class RenderEffect {
    STATIC,   // Solid hull/frame
    PULSE,    // Breathing glow (Power systems)
    FLICKER,  // Rapid nervous energy (Aggressive accents/Damage)
    GLOW      // High-intensity emissive
}

// Legacy/Primitive shapes for projectiles and UI markers
data class ShapeComponent(
    var type: ShapeType = ShapeType.SQUARE,
    var color: Color = Color.WHITE.cpy(),
    var size: Float = 1f,
    var rotation: Float = 0f
) : Component<ShapeComponent> {
    override fun type() = ShapeComponent
    companion object : ComponentType<ShapeComponent>()
}

enum class ShapeType { TRIANGLE, SQUARE, CIRCLE, HEXAGON }

// High-performance texture rendering for single sprites
data class TextureRegionComponent(
    var region: TextureRegion? = null,
    var scale: Float = 1f,
    var rotation: Float = 0f,
    var color: Color = Color.WHITE.cpy(),
    var zIndex: Int = 0
) : Component<TextureRegionComponent> {
    override fun type() = TextureRegionComponent
    companion object : ComponentType<TextureRegionComponent>()
}

/**
 * COMPOSITE VISUAL: Allows a single entity to have multiple overlapping texture layers.
 */
data class VisualLayer(
    var region: TextureRegion,
    var color: Color = Color.WHITE.cpy(),
    var offsetScale: Float = 1f,
    var rotationOffset: Float = 0f,
    var zIndex: Int = 0,
    var effect: RenderEffect = RenderEffect.STATIC
)

data class CompositeVisualComponent(
    val layers: MutableList<VisualLayer> = mutableListOf()
) : Component<CompositeVisualComponent> {
    override fun type() = CompositeVisualComponent
    companion object : ComponentType<CompositeVisualComponent>()
}

// Visual decorators for weapons (the barrels)
data class BarrelVisualComponent(
    var length: Float = 0.4f,
    var width: Float = 0.1f,
    var color: Color = Color.GRAY,
    var platformRotation: Float = 0f
) : Component<BarrelVisualComponent> {
    override fun type() = BarrelVisualComponent
    companion object : ComponentType<BarrelVisualComponent>()
}
