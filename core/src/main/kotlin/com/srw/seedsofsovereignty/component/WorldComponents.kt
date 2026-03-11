package com.srw.seedsofsovereignty.component

import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class Faction { NEUTRAL, VANGUARD, SWARM, ANCIENTS }

object FactionPalette {
    val VANGUARD = FactionDNA(primary = Color.CYAN, secondary = Color(0.275f, 0.337f, 0.439f, 1f), accent = Color.WHITE)
    val SWARM = FactionDNA(primary = Color.GREEN, secondary = Color.BLACK, accent = Color.PURPLE)
    val ANCIENTS = FactionDNA(primary = Color.GOLDENROD, secondary = Color.WHITE, accent = Color.RED)
    val NEUTRAL = FactionDNA(primary = Color.LIGHT_GRAY, secondary = Color.DARK_GRAY, accent = Color.GRAY)

    fun get(faction: Faction) = when(faction) {
        Faction.VANGUARD -> VANGUARD
        Faction.SWARM -> SWARM
        Faction.ANCIENTS -> ANCIENTS
        Faction.NEUTRAL -> NEUTRAL
    }
}

data class FactionDNA(val primary: Color, val secondary: Color, val accent: Color)

data class SectorComponent(
    var name: String = "Unknown Void",
    var faction: Faction = Faction.NEUTRAL,
    var tension: Float = 0f
) : Component<SectorComponent> {
    override fun type() = SectorComponent
    companion object : ComponentType<SectorComponent>()
}

data class WarpComponent(
    var isActive: Boolean = false,
    var speed: Float = 0f,
    var progress: Float = 0f
) : Component<WarpComponent> {
    override fun type() = WarpComponent
    companion object : ComponentType<WarpComponent>()
}

data class HUDGlitchComponent(
    var intensity: Float = 0f,
    var duration: Float = 0f
) : Component<HUDGlitchComponent> {
    override fun type() = HUDGlitchComponent
    companion object : ComponentType<HUDGlitchComponent>()
}
