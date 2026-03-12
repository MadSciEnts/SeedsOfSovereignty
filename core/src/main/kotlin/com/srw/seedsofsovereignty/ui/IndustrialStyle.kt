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
 * Industrial style generator for HUD textures (v2.8.2).
 * FIXES:
 * 1. [FIDELITY] - Scaled Tactical Reticle to 256x256 for GSF 0.1 HUD clarity.
 * 2. [PIXMAP] - Fixed drawCircle signature mismatch.
 */
class IndustrialStyle : TacticalStyleGenerator {

    private val DARK_GREY = Color.valueOf("1A1A1A")
    private val HAZARD_ORANGE = Color.valueOf("FF8C00")
    private val STEEL_BLUE = Color.valueOf("4682B4")

    override fun generate(skin: Skin): TextureCollection {
        val cornerArtSize = 16

        // 1. Joystick Background
        val jBgPix = Pixmap(250, 250, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f); fillCircle(125, 125, 120)
            applyNoise(this); setColor(STEEL_BLUE); drawCircle(125, 125, 120); drawCircle(125, 125, 119)
            setColor(Color.GRAY)
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                fillCircle((125 + 110 * Math.cos(angle)).toInt(), (125 + 110 * Math.sin(angle)).toInt(), 3)
            }
        }
        val jBgTex = Texture(jBgPix).also { skin.add("ind_j_bg", it) }; jBgPix.dispose()

        // 2. Joystick Knob
        val jKnobPix = Pixmap(60, 60, Pixmap.Format.RGBA8888).apply {
            setColor(STEEL_BLUE); fillCircle(30, 30, 25); setColor(Color.LIGHT_GRAY); drawCircle(30, 30, 25)
            setColor(1f, 1f, 1f, 0.3f); fillCircle(20, 20, 5)
        }
        val jKnobTex = Texture(jKnobPix).also { skin.add("ind_j_knob", it) }; jKnobPix.dispose()

        // 3. Button Up
        val bUpPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f); fill(); applyNoise(this)
            setColor(STEEL_BLUE); drawRectangle(0, 0, 64, 64)
            for (i in 1..4) {
                setColor(STEEL_BLUE.r, STEEL_BLUE.g, STEEL_BLUE.b, 1f - (i * 0.2f))
                drawRectangle(i, i, 64 - 2 * i, 64 - 2 * i)
            }
            setColor(Color.GRAY)
            fillCircle(cornerArtSize/2, cornerArtSize/2, 3); fillCircle(64-cornerArtSize/2, cornerArtSize/2, 3)
            fillCircle(cornerArtSize/2, 64-cornerArtSize/2, 3); fillCircle(64-cornerArtSize/2, 64-cornerArtSize/2, 3)
        }
        val bUpTex = Texture(bUpPix).also { skin.add("ind_b_up", it) }; bUpPix.dispose()

        // 4. Button Down
        val bDownPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.95f); fill(); applyNoise(this)
            setColor(HAZARD_ORANGE); drawRectangle(0, 0, 64, 64)
            for (i in 1..4) drawRectangle(i, i, 64 - 2 * i, 64 - 2 * i)
        }
        val bDownTex = Texture(bDownPix).also { skin.add("ind_b_down", it) }; bDownPix.dispose()

        // 5. Toggle Off
        val tOffPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(0.1f, 0.1f, 0.1f, 0.85f); fill(); setColor(Color.DARK_GRAY); drawRectangle(0, 0, 64, 64)
        }
        val tOffTex = Texture(tOffPix).also { skin.add("ind_t_off", it) }; tOffPix.dispose()

        // 6. Toggle On
        val tOnPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.85f); fill()
            setColor(HAZARD_ORANGE); drawRectangle(0, 0, 64, 64); fillCircle(50, 14, 6)
        }
        val tOnTex = Texture(tOnPix).also { skin.add("ind_t_on", it) }; tOnPix.dispose()

        // 7. Slider BG
        val sBgPix = Pixmap(250, 20, Pixmap.Format.RGBA8888).apply {
            setColor(0.05f, 0.05f, 0.05f, 0.9f); fill(); setColor(STEEL_BLUE); drawRectangle(0, 0, 250, 20)
        }
        val sBgTex = Texture(sBgPix).also { skin.add("ind_s_bg", it) }; sBgPix.dispose()

        // 8. Slider Knob
        val sKnobPix = Pixmap(40, 60, Pixmap.Format.RGBA8888).apply {
            setColor(STEEL_BLUE); fill(); setColor(Color.WHITE); fillRectangle(18, 10, 4, 40)
        }
        val sKnobTex = Texture(sKnobPix).also { skin.add("ind_s_knob", it) }; sKnobPix.dispose()

        // 9. Status Lights
        fun createStatusLight(color: Color, name: String): Drawable {
            val pix = Pixmap(20, 20, Pixmap.Format.RGBA8888).apply {
                setColor(color.r * 0.3f, color.g * 0.3f, color.b * 0.3f, 1f); fillCircle(10, 10, 9)
                setColor(color); fillCircle(10, 10, 6); setColor(Color.WHITE); fillCircle(7, 7, 2)
            }
            val tex = Texture(pix).also { skin.add(name, it) }; pix.dispose()
            return TextureRegionDrawable(tex)
        }

        // 10. Panel
        val pPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(DARK_GREY.r, DARK_GREY.g, DARK_GREY.b, 0.96f); fill()
            setColor(STEEL_BLUE); drawRectangle(0, 0, 64, 64)
            fillRectangle(0, 0, 16, 4); fillRectangle(0, 0, 4, 16)
            fillRectangle(48, 0, 16, 4); fillRectangle(60, 0, 4, 16)
            fillRectangle(0, 60, 16, 4); fillRectangle(0, 48, 4, 16)
            fillRectangle(48, 60, 16, 4); fillRectangle(60, 48, 4, 16)
        }
        val pTex = Texture(pPix).also { skin.add("ind_panel", it) }; pPix.dispose()

        // 11. Tactical Reticle (v2.8.2 Scaled 4x for GSF 0.1)
        val rPix = Pixmap(256, 256, Pixmap.Format.RGBA8888).apply {
            setColor(HAZARD_ORANGE)
            // HARDENING: Concentric circles simulate thickness for standard Pixmap API
            drawCircle(128, 128, 120); drawCircle(128, 128, 119); drawCircle(128, 128, 118); drawCircle(128, 128, 117)
            // Crosshairs
            fillRectangle(126, 0, 4, 60); fillRectangle(126, 196, 4, 60)
            fillRectangle(0, 126, 60, 4); fillRectangle(196, 126, 60, 4)
            // Center Lead Pip
            fillCircle(128, 128, 12)
        }
        val rTex = Texture(rPix).also { skin.add("ind_reticle", it) }; rPix.dispose()

        val collection = TextureCollection(
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
            panel = NinePatchDrawable(NinePatch(pTex, 20, 20, 20, 20)),
            reticle = TextureRegionDrawable(rTex)
        )

        skin.add("default", TacticalSurface.TacticalSurfaceStyle().apply {
            background = collection.joystickBg; reticle = collection.reticle
        })

        return collection
    }

    private fun applyNoise(pixmap: Pixmap, intensity: Float = 0.05f) {
        for (y in 0 until pixmap.height) {
            for (x in 0 until pixmap.width) {
                val colorInt = pixmap.getPixel(x, y); val color = Color(colorInt)
                val noise = (Random.nextFloat() - 0.5f) * intensity
                color.r = (color.r + noise).coerceIn(0f, 1f)
                color.g = (color.g + noise).coerceIn(0f, 1f)
                color.b = (color.b + noise).coerceIn(0f, 1f)
                pixmap.drawPixel(x, y, Color.rgba8888(color))
            }
        }
    }
}
