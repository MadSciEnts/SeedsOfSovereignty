package com.srw.seedofsov.assets

import com.badlogic.gdx.graphics.Color

/**
 * Defines the visual "look" for a set of UI elements.
 */
data class UiStyle(
    val palette: UiAssetGenerator.ColorPalette,
    val cornerRadius: Float = 0f,
    val borderWidth: Int = 2,
    val chamferSize: Int = 0,
    val glitchIntensity: Float = 0f,
    val fontStyle: String = "TECHNICAL",
    val bevelEffect: Boolean = false
)

/**
 * Registry for managing UI styles for different Epochs and Tiers.
 */
object StyleRegistry {
    private val styles = mutableMapOf<String, UiStyle>()

    fun register(key: String, style: UiStyle) {
        styles[key] = style
    }

    fun get(key: String): UiStyle = styles[key] ?: DEFAULT_STYLE

    val DEFAULT_STYLE = UiStyle(
        palette = UiAssetGenerator.ColorPalette(
            primary = Color.CYAN,
            secondary = Color.TEAL,
            background = Color.DARK_GRAY,
            highlight = Color.WHITE,
            danger = Color.RED
        )
    )

    val EPOCH_1_STYLE = UiStyle(
        palette = UiAssetGenerator.ColorPalette(
            primary = Color(0.2f, 0.6f, 1f, 1f), // Scientific Blue
            secondary = Color(0.1f, 0.3f, 0.5f, 1f),
            background = Color(0.05f, 0.05f, 0.1f, 0.9f),
            highlight = Color(0.8f, 0.9f, 1f, 1f),
            danger = Color(1f, 0.2f, 0.2f, 1f)
        ),
        borderWidth = 1,
        chamferSize = 4
    )

    val EPOCH_2_STYLE = UiStyle(
        palette = UiAssetGenerator.ColorPalette(
            primary = Color(1f, 0.8f, 0.2f, 1f), // Tactical Gold
            secondary = Color(0.5f, 0.4f, 0.1f, 1f),
            background = Color(0.1f, 0.08f, 0.05f, 0.9f),
            highlight = Color(1f, 0.95f, 0.8f, 1f),
            danger = Color(1f, 0.4f, 0.1f, 1f)
        ),
        borderWidth = 2,
        chamferSize = 8,
        bevelEffect = true
    )
}
