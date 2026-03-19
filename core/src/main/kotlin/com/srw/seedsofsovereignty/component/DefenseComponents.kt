package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * DEFENSE & SUSTAINABILITY
 */

data class ArcedDefenseComponent(
    var shieldFore: Float = 0f,
    var shieldAft: Float = 0f,
    var shieldPort: Float = 0f,
    var shieldStbd: Float = 0f,
    var hullFore: Float = 100f,
    var hullAft: Float = 100f,
    var hullPort: Float = 100f,
    var hullStbd: Float = 100f
) : Component<ArcedDefenseComponent> {
    override fun type() = ArcedDefenseComponent
    companion object : ComponentType<ArcedDefenseComponent>()
}

// PowerCoreComponent removed from here. Main version lives in CombatComponents.kt.
