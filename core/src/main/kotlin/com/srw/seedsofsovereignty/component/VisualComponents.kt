package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ParticleComponent(
    val type: Type,
    val color: Color = Color.WHITE,
    var size: Float = 1f,
    var maxLife: Float = 0.5f,
    var fade: Boolean = true
) : Component<ParticleComponent> {
    enum class Type { SHIELD_RIPPLE, SPARK }
    override fun type() = ParticleComponent
    companion object : ComponentType<ParticleComponent>()
}
