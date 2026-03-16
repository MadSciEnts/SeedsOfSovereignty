package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

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
