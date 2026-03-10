package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

/**
 * Data class representing a collection of textures for a specific UI style.
 * v0.4.0: Visual DNA for procedural hardware skins.
 */
data class TextureCollection(
    val joystickBg: Drawable,
    val joystickKnob: Drawable,
    val buttonUp: Drawable,
    val buttonDown: Drawable,
    val toggleOff: Drawable,
    val toggleOn: Drawable,
    val sliderBg: Drawable,
    val sliderKnob: Drawable,
    val statusLightRed: Drawable,
    val statusLightYellow: Drawable,
    val statusLightGreen: Drawable,
    val panel: Drawable,
    val reticle: Drawable // Added for TacticalSurface support
)

/**
 * Procedural texture engine for generating visual "skins" for hardware instruments.
 * v0.4.0: Phase 6.2 - The Visual Core.
 * Refactored v0.5.0: Acts as a factory router delegating to specific Style Generators.
 */
object HUDTextureGenerator {

    /**
     * Generates a TextureCollection based on the provided style identifier.
     */
    fun generate(styleId: String, skin: Skin): TextureCollection {
        val generator: TacticalStyleGenerator = when (styleId.uppercase()) {
            "INDUSTRIAL" -> IndustrialStyle()
            "IMPERIAL" -> ImperialStyle()
            else -> IndustrialStyle()
        }
        return generator.generate(skin)
    }
}
