package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import kotlin.random.Random

/**
 * Industrial style generator for HUD textures.
 * v0.5.0: Phase 7.3 - Style Decomposition.
 */
class IndustrialStyle : TacticalStyleGenerator {

    private val DARK_GREY = Color.valueOf("1A1A1A")
    private val HAZARD_ORANGE = Color.valueOf("FF8C00")
    private val STEEL_BLUE = Color.valueOf("4682B4")

    override fun generate(skin: Skin): TextureCollection {
        // Art-Aware Split Constants (v0.4.0 Phase 6.5)
        val cornerArtSize = 16

        // 1. Joystick Background (250x250 circular)
        val jBgPix = Pixmap(250, 250, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f)
            fillCircle(125, 125, 120)
            applyNoise(this)
            setColor(STEEL_BLUE)
            drawCircle(125, 125, 120)
            drawCircle(125, 125, 119)
            // Rivets
            setColor(Color.GRAY)
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                val rx = (125 + 110 * Math.cos(angle)).toInt()
                val ry = (125 + 110 * Math.sin(angle)).toInt()
                fillCircle(rx, ry, 3)
            }
        }
        val jBgTex = Texture(jBgPix).also { skin.add("ind_j_bg", it) }
        jBgPix.dispose()

        // 2. Joystick Knob (60x60 centered handle)
        val jKnobPix = Pixmap(60, 60, Pixmap.Format.RGBA8888).apply {
            setColor(STEEL_BLUE)
            fillCircle(30, 30, 25)
            setColor(Color.LIGHT_GRAY)
            drawCircle(30, 30, 25)
            // Metallic shine
            setColor(1f, 1f, 1f, 0.3f)
            fillCircle(20, 20, 5)
        }
        val jKnobTex = Texture(jKnobPix).also { skin.add("ind_j_knob", it) }
        jKnobPix.dispose()

        // 3. Button Up (Square base with hard bevels)
        val bUpPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f)
            fill()
            applyNoise(this)
            setColor(STEEL_BLUE)
            drawRectangle(0, 0, 64, 64)
            // 4px hard bevel
            for (i in 1..4) {
                setColor(STEEL_BLUE.r, STEEL_BLUE.g, STEEL_BLUE.b, 1f - (i * 0.2f))
                drawRectangle(i, i, 64 - 2 * i, 64 - 2 * i)
            }
            // Art-Aware Rivets in corners
            setColor(Color.GRAY)
            fillCircle(cornerArtSize / 2, cornerArtSize / 2, 3)
            fillCircle(64 - cornerArtSize / 2, cornerArtSize / 2, 3)
            fillCircle(cornerArtSize / 2, 64 - cornerArtSize / 2, 3)
            fillCircle(64 - cornerArtSize / 2, 64 - cornerArtSize / 2, 3)
        }
        val bUpTex = Texture(bUpPix).also { skin.add("ind_b_up", it) }
        bUpPix.dispose()

        // 4. Button Down (Glowing orange border)
        val bDownPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.95f)
            fill()
            applyNoise(this)
            setColor(HAZARD_ORANGE)
            drawRectangle(0, 0, 64, 64)
            for (i in 1..4) {
                drawRectangle(i, i, 64 - 2 * i, 64 - 2 * i)
            }
        }
        val bDownTex = Texture(bDownPix).also { skin.add("ind_b_down", it) }
        bDownPix.dispose()

        // 5. Toggle Off (Low saturation)
        val tOffPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(0.1f, 0.1f, 0.1f, 0.85f)
            fill()
            setColor(Color.DARK_GRAY)
            drawRectangle(0, 0, 64, 64)
        }
        val tOffTex = Texture(tOffPix).also { skin.add("ind_t_off", it) }
        tOffPix.dispose()

        // 6. Toggle On (High energy with light)
        val tOnPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f)
            fill()
            setColor(HAZARD_ORANGE)
            drawRectangle(0, 0, 64, 64)
            // Indicator light
            setColor(HAZARD_ORANGE)
            fillCircle(50, 14, 6)
        }
        val tOnTex = Texture(tOnPix).also { skin.add("ind_t_on", it) }
        tOnPix.dispose()

        // 7. Slider BG (Thin trough)
        val sBgPix = Pixmap(250, 20, Pixmap.Format.RGBA8888).apply {
            setColor(0.05f, 0.05f, 0.05f, 0.9f)
            fill()
            setColor(STEEL_BLUE)
            drawRectangle(0, 0, 250, 20)
        }
        val sBgTex = Texture(sBgPix).also { skin.add("ind_s_bg", it) }
        sBgPix.dispose()

        // 8. Slider Knob (Wide handle)
        val sKnobPix = Pixmap(40, 60, Pixmap.Format.RGBA8888).apply {
            setColor(STEEL_BLUE)
            fill()
            setColor(Color.WHITE)
            fillRectangle(18, 10, 4, 40) // Grip detail
        }
        val sKnobTex = Texture(sKnobPix).also { skin.add("ind_s_knob", it) }
        sKnobPix.dispose()

        // 9. Status Lights
        fun createStatusLight(color: Color, name: String): Drawable {
            val pix = Pixmap(20, 20, Pixmap.Format.RGBA8888).apply {
                setColor(color.r * 0.3f, color.g * 0.3f, color.b * 0.3f, 1f)
                fillCircle(10, 10, 9)
                setColor(color)
                fillCircle(10, 10, 6)
                setColor(Color.WHITE)
                fillCircle(7, 7, 2) // Glint
            }
            val tex = Texture(pix).also { skin.add(name, it) }
            pix.dispose()
            return TextureRegionDrawable(tex)
        }

        // 10. Panel (Moving logic from HUDFactory)
        val pPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.96f)
            fill()
            setColor(STEEL_BLUE)
            drawRectangle(0, 0, 64, 64)
            // Corner highlights
            fillRectangle(0, 0, 16, 4); fillRectangle(0, 0, 4, 16)
            fillRectangle(48, 0, 16, 4); fillRectangle(60, 0, 4, 16)
            fillRectangle(0, 60, 16, 4); fillRectangle(0, 48, 4, 16)
            fillRectangle(48, 60, 16, 4); fillRectangle(60, 48, 4, 16)
        }
        val pTex = Texture(pPix).also { skin.add("ind_panel", it) }
        pPix.dispose()

        return TextureCollection(
            joystickBg = TextureRegionDrawable(jBgTex),
            joystickKnob = TextureRegionDrawable(jKnobTex),
            buttonUp = NinePatchDrawable(NinePatch(bUpTex, cornerArtSize, cornerArtSize, cornerArtSize, cornerArtSize)),
            buttonDown = NinePatchDrawable(NinePatch(bDownTex, cornerArtSize, cornerArtSize, cornerArtSize, cornerArtSize)),
            toggleOff = NinePatchDrawable(NinePatch(tOffTex, 10, 10, 10, 10)),
            toggleOn = NinePatchDrawable(NinePatch(tOnTex, 10, 10, 10, 10)),
            sliderBg = NinePatchDrawable(NinePatch(sBgTex, 5, 5, 5, 5)),
            sliderKnob = TextureRegionDrawable(sKnobTex),
            statusLightRed = createStatusLight(Color.RED, "ind_light_red"),
            statusLightYellow = createStatusLight(Color.YELLOW, "ind_light_yellow"),
            statusLightGreen = createStatusLight(Color.GREEN, "ind_light_green"),
            panel = NinePatchDrawable(NinePatch(pTex, 20, 20, 20, 20))
        )
    }

    private fun applyNoise(pixmap: Pixmap, intensity: Float = 0.05f) {
        for (y in 0 until pixmap.height) {
            for (x in 0 until pixmap.width) {
                val colorInt = pixmap.getPixel(x, y)
                val color = Color(colorInt)
                val noise = (Random.nextFloat() - 0.5f) * intensity
                color.r = (color.r + noise).coerceIn(0f, 1f)
                color.g = (color.g + noise).coerceIn(0f, 1f)
                color.b = (color.b + noise).coerceIn(0f, 1f)
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }
    }
}
