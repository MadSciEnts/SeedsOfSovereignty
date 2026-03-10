package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * Imperial style generator for HUD textures.
 * v0.5.0: Phase 7.3 - Style Decomposition.
 * v0.7.2: Resized Tactical Reticle to 80x80.
 */
class ImperialStyle : TacticalStyleGenerator {

    private val GOLD = Color.valueOf("FFD700")
    private val ROYAL_PURPLE = Color.valueOf("7851A9")
    private val SLATE_GREY = Color.valueOf("708090")

    override fun generate(skin: Skin): TextureCollection {
        val cornerArtSize = 16

        // 1. Joystick Background
        val jBgPix = Pixmap(250, 250, Pixmap.Format.RGBA8888).apply {
            setColor(SLATE_GREY.r, SLATE_GREY.g, SLATE_GREY.b, 0.85f)
            fillCircle(125, 125, 120)
            setColor(GOLD)
            drawCircle(125, 125, 120)
            drawCircle(125, 125, 119)
            setColor(ROYAL_PURPLE)
            drawCircle(125, 125, 40)
            setColor(ROYAL_PURPLE.r, ROYAL_PURPLE.g, ROYAL_PURPLE.b, 0.4f)
            fillCircle(125, 125, 38)
        }
        val jBgTex = Texture(jBgPix).also { skin.add("imp_j_bg", it) }
        jBgPix.dispose()

        // 2. Joystick Knob
        val jKnobPix = Pixmap(60, 60, Pixmap.Format.RGBA8888).apply {
            setColor(GOLD)
            fillCircle(30, 30, 25)
            setColor(SLATE_GREY)
            drawCircle(30, 30, 25)
            fillCircle(30, 30, 5)
        }
        val jKnobTex = Texture(jKnobPix).also { skin.add("imp_j_knob", it) }
        jKnobPix.dispose()

        // 3. Button Up
        val bUpPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(ROYAL_PURPLE)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 64, 64)
            drawRectangle(1, 1, 62, 62)
            fillRectangle(0, 0, 8, 2); fillRectangle(0, 0, 2, 8)
            fillRectangle(56, 0, 8, 2); fillRectangle(62, 0, 2, 8)
            fillRectangle(0, 62, 8, 2); fillRectangle(0, 56, 2, 8)
            fillRectangle(56, 62, 8, 2); fillRectangle(62, 56, 2, 8)
        }
        val bUpTex = Texture(bUpPix).also { skin.add("imp_b_up", it) }
        bUpPix.dispose()

        // 4. Button Down
        val bDownPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(ROYAL_PURPLE.r * 0.7f, ROYAL_PURPLE.g * 0.7f, ROYAL_PURPLE.b * 0.7f, 1f)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 64, 64)
            drawRectangle(1, 1, 62, 62)
            drawRectangle(2, 2, 60, 60)
        }
        val bDownTex = Texture(bDownPix).also { skin.add("imp_b_down", it) }
        bDownPix.dispose()

        // 5. Toggle Off
        val tOffPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(SLATE_GREY)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 64, 64)
            drawRectangle(1, 1, 62, 62)
        }
        val tOffTex = Texture(tOffPix).also { skin.add("imp_t_off", it) }
        tOffPix.dispose()

        // 6. Toggle On
        val tOnPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(ROYAL_PURPLE)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 64, 64)
            drawRectangle(1, 1, 62, 62)
            setColor(GOLD)
            fillCircle(50, 14, 6)
        }
        val tOnTex = Texture(tOnPix).also { skin.add("imp_t_on", it) }
        tOnPix.dispose()

        // 7. Slider BG
        val sBgPix = Pixmap(250, 20, Pixmap.Format.RGBA8888).apply {
            setColor(SLATE_GREY)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 250, 20)
            drawRectangle(1, 1, 248, 18)
        }
        val sBgTex = Texture(sBgPix).also { skin.add("imp_s_bg", it) }
        sBgPix.dispose()

        // 8. Slider Knob
        val sKnobPix = Pixmap(40, 60, Pixmap.Format.RGBA8888).apply {
            setColor(GOLD)
            fill()
            setColor(ROYAL_PURPLE)
            fillRectangle(18, 10, 4, 40)
        }
        val sKnobTex = Texture(sKnobPix).also { skin.add("imp_s_knob", it) }
        sKnobPix.dispose()

        // 9. Status Lights
        fun createStatusLight(color: Color, name: String): Drawable {
            val pix = Pixmap(20, 20, Pixmap.Format.RGBA8888).apply {
                setColor(color.r * 0.3f, color.g * 0.3f, color.b * 0.3f, 1f)
                fillCircle(10, 10, 9)
                setColor(color)
                fillCircle(10, 10, 6)
                setColor(Color.WHITE)
                fillCircle(7, 7, 2)
            }
            val tex = Texture(pix).also { skin.add(name, it) }
            pix.dispose()
            return TextureRegionDrawable(tex)
        }

        // 10. Panel
        val pPix = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
            setColor(ROYAL_PURPLE.r * 0.5f, ROYAL_PURPLE.g * 0.5f, ROYAL_PURPLE.b * 0.5f, 0.96f)
            fill()
            setColor(GOLD)
            drawRectangle(0, 0, 64, 64)
            drawRectangle(1, 1, 62, 62)
            fillRectangle(0, 0, 8, 2); fillRectangle(0, 0, 2, 8)
            fillRectangle(56, 0, 8, 2); fillRectangle(62, 0, 2, 8)
            fillRectangle(0, 62, 8, 2); fillRectangle(0, 56, 2, 8)
            fillRectangle(56, 62, 8, 2); fillRectangle(62, 56, 2, 8)
        }
        val pTex = Texture(pPix).also { skin.add("imp_panel", it) }
        pPix.dispose()

        // 11. Tactical Reticle (v0.7.2 Resized 2x)
        val rPix = Pixmap(80, 80, Pixmap.Format.RGBA8888).apply {
            setColor(GOLD)
            drawCircle(40, 40, 38)
            drawCircle(40, 40, 37)
            drawCircle(40, 40, 20) // Imperial focus ring
            drawLine(40, 0, 40, 80)
            drawLine(0, 40, 80, 40)
            // Center Dot
            fillCircle(40, 40, 4)
        }
        val rTex = Texture(rPix).also { skin.add("imp_reticle", it) }
        rPix.dispose()

        val collection = TextureCollection(
            joystickBg = TextureRegionDrawable(jBgTex),
            joystickKnob = TextureRegionDrawable(jKnobTex),
            buttonUp = NinePatchDrawable(NinePatch(bUpTex, cornerArtSize, cornerArtSize, cornerArtSize, cornerArtSize)),
            buttonDown = NinePatchDrawable(NinePatch(bDownTex, cornerArtSize, cornerArtSize, cornerArtSize, cornerArtSize)),
            toggleOff = NinePatchDrawable(NinePatch(tOffTex, 10, 10, 10, 10)),
            toggleOn = NinePatchDrawable(NinePatch(tOnTex, 10, 10, 10, 10)),
            sliderBg = NinePatchDrawable(NinePatch(sBgTex, 5, 5, 5, 5)),
            sliderKnob = TextureRegionDrawable(sKnobTex),
            statusLightRed = createStatusLight(Color.RED, "imp_light_red"),
            statusLightYellow = createStatusLight(Color.YELLOW, "imp_light_yellow"),
            statusLightGreen = createStatusLight(Color.GREEN, "imp_light_green"),
            panel = NinePatchDrawable(NinePatch(pTex, 20, 20, 20, 20)),
            reticle = TextureRegionDrawable(rTex)
        )

        skin.add("imperial", TacticalSurface.TacticalSurfaceStyle().apply {
            background = collection.joystickBg
            reticle = collection.reticle
        })

        return collection
    }
}
