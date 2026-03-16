package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * HUD VISUAL EFFECTS & SHADER STATE
 */
data class HudEffectComponent(
    var glitchIntensity: Float = 0f,    // 0.0 to 1.0 (driven by damage impact)
    var flickerIntensity: Float = 0f,   // 0.0 to 1.0 (driven by power core integrity)
    var alpha: Float = 0f,              // 0.0 to 1.0 (faded out until warp-in)
    var distortion: Float = 0f,         // Pulse or warp distortion
    var colorShift: Float = 0f          // RGB split or faction shift
) : Component<HudEffectComponent> {
    override fun type() = HudEffectComponent
    companion object : ComponentType<HudEffectComponent>()
}
