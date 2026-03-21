package com.srw.seedofsov.assets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.generators.ui.CircleGenerator
import com.srw.seedofsov.assets.generators.ui.NautilusArcGenerator
import com.srw.seedofsov.assets.generators.ui.NinePatchGenerator
import com.srw.seedofsov.assets.generators.ui.RadarGenerator

/**
 * Orchestrates the generation of all UI-related procedural textures based on a Style.
 */
class UiAssetGenerator(private val style: UiStyle) {

    data class ColorPalette(
        val primary: Color,
        val secondary: Color,
        val background: Color,
        val highlight: Color,
        val danger: Color
    )

    // UI - Buttons & NinePatches
    fun generateButton(width: Int, height: Int, isDown: Boolean = false): Pixmap {
        val color = if (isDown) style.palette.primary.cpy().lerp(Color.BLACK, 0.2f) else style.palette.primary
        return NinePatchGenerator(
            width = width,
            height = height,
            borderWidth = style.borderWidth,
            borderColor = color,
            fillColor = style.palette.background,
            chamferSize = style.chamferSize
        ).generate()
    }

    fun generateBackgroundBox(width: Int, height: Int): Pixmap {
        return NinePatchGenerator(
            width = width,
            height = height,
            borderWidth = style.borderWidth / 2,
            borderColor = style.palette.secondary,
            fillColor = style.palette.background.cpy().apply { a = 0.5f },
            chamferSize = style.chamferSize
        ).generate()
    }

    // UI - Joysticks
    fun generateJoystickBackground(radius: Int): Pixmap {
        return CircleGenerator(radius, style.palette.secondary, false, style.borderWidth).generate()
    }

    fun generateJoystickHandle(radius: Int): Pixmap {
        return CircleGenerator(radius, style.palette.primary, true).generate()
    }

    // UI - Sliders & Bars
    fun generateSliderBackground(width: Int, height: Int): Pixmap {
        return NinePatchGenerator(
            width = width,
            height = height,
            borderWidth = style.borderWidth / 2,
            borderColor = style.palette.secondary,
            fillColor = style.palette.background,
            chamferSize = style.chamferSize / 2
        ).generate()
    }

    fun generateBarForeground(width: Int, height: Int, color: Color): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        return pixmap
    }

    // Nautilus Arcs
    fun generateNautilusArc(radius: Int, thickness: Int, startAngle: Float, arcDegrees: Float, color: Color): Pixmap {
        return NautilusArcGenerator(radius, thickness, startAngle, arcDegrees, color).generate()
    }

    // Radar
    fun generateRadarBackground(radius: Int): Pixmap {
        return RadarGenerator(radius, style.palette.primary).generateBackground()
    }

    fun generateRadarPoint(radius: Int, color: Color): Pixmap {
        return CircleGenerator(radius, color, true).generate()
    }

    // TouchPad
    fun generateTouchPadBackground(radius: Int): Pixmap {
        return CircleGenerator(radius, style.palette.secondary, false, style.borderWidth / 2).generate()
    }

    fun generateTouchPadHandle(radius: Int): Pixmap {
        return CircleGenerator(radius, style.palette.highlight, true).generate()
    }
}
