package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.srw.seedsofsovereignty.component.ModuleCategory

/**
 * TEXTURE SERVICE: The procedural asset factory.
 * Handles Pixmap generation, NinePatch creation, and Skin baking.
 */
class TextureService : Disposable {
    private val texturesToDispose = mutableListOf<Texture>()
    val uiSkin = Skin()

    init {
        bakeInitialSkin()
    }

    private fun bakeInitialSkin() {
        // 1. Create a White 1x1 pixel for backgrounds
        val whitePixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        whitePixmap.setColor(Color.WHITE)
        whitePixmap.fill()
        val whiteTex = Texture(whitePixmap)
        texturesToDispose.add(whiteTex)
        uiSkin.add("white", TextureRegion(whiteTex))
        whitePixmap.dispose()

        // 2. Create Joystick Textures
        val joystickPixmap = Pixmap(100, 100, Pixmap.Format.RGBA8888)
        joystickPixmap.setColor(Color(1f, 1f, 1f, 0.15f))
        joystickPixmap.fillCircle(50, 50, 50)
        val bgTex = Texture(joystickPixmap)
        texturesToDispose.add(bgTex)
        val joystickBg = TextureRegionDrawable(TextureRegion(bgTex))
        uiSkin.add("joystick_bg", joystickBg, Drawable::class.java)

        joystickPixmap.setColor(0f, 0f, 0f, 0f); joystickPixmap.fill()
        joystickPixmap.setColor(Color(0.2f, 0.8f, 1f, 0.6f))
        joystickPixmap.fillCircle(50, 50, 25)
        val knobTex = Texture(joystickPixmap)
        texturesToDispose.add(knobTex)
        val joystickKnob = TextureRegionDrawable(TextureRegion(knobTex))
        uiSkin.add("joystick_knob", joystickKnob, Drawable::class.java)
        joystickPixmap.dispose()

        // 3. Create NinePatch for Buttons & Panels
        val patchSize = 12
        val edge = 4
        val patchPixmap = Pixmap(patchSize, patchSize, Pixmap.Format.RGBA8888)

        // Base Button Up (White frame, we will tint it)
        patchPixmap.setColor(0f, 0f, 0f, 0f); patchPixmap.fill()
        patchPixmap.setColor(Color(0.1f, 0.1f, 0.1f, 0.8f))
        patchPixmap.fillRectangle(1, 1, patchSize - 2, patchSize - 2)
        patchPixmap.setColor(Color.WHITE)
        patchPixmap.drawRectangle(0, 0, patchSize, patchSize)
        val upTex = Texture(patchPixmap)
        texturesToDispose.add(upTex)
        val upPatch = NinePatchDrawable(NinePatch(upTex, edge, edge, edge, edge))
        uiSkin.add("btn_up", upPatch, Drawable::class.java)

        // Base Button Down
        patchPixmap.setColor(0f, 0f, 0f, 0f); patchPixmap.fill()
        patchPixmap.setColor(Color(0.5f, 0.5f, 0.5f, 0.9f))
        patchPixmap.fillRectangle(1, 1, patchSize - 2, patchSize - 2)
        patchPixmap.setColor(Color.WHITE)
        patchPixmap.drawRectangle(0, 0, patchSize, patchSize)
        val downTex = Texture(patchPixmap)
        texturesToDispose.add(downTex)
        val downPatch = NinePatchDrawable(NinePatch(downTex, edge, edge, edge, edge))
        uiSkin.add("btn_down", downPatch, Drawable::class.java)

        // Panel Background
        patchPixmap.setColor(0f, 0f, 0f, 0f); patchPixmap.fill()
        patchPixmap.setColor(Color(0f, 0f, 0f, 0.6f))
        patchPixmap.fillRectangle(1, 1, patchSize - 2, patchSize - 2)
        patchPixmap.setColor(Color.DARK_GRAY)
        patchPixmap.drawRectangle(0, 0, patchSize, patchSize)
        val panelTex = Texture(patchPixmap)
        texturesToDispose.add(panelTex)
        val panelPatch = NinePatchDrawable(NinePatch(panelTex, edge, edge, edge, edge))
        uiSkin.add("panel_bg", panelPatch, Drawable::class.java)

        patchPixmap.dispose()

        // 4. Baking Styles
        // Create a larger font (scale 2x)
        val font = BitmapFont().apply {
            data.setScale(2f)
        }
        uiSkin.add("default-font", font)
        uiSkin.add("default", Touchpad.TouchpadStyle(joystickBg, joystickKnob))

        // Default style
        uiSkin.add("default", TextButton.TextButtonStyle(upPatch, downPatch, upPatch, font))

        // Category Specific Styles (Color Coded)
        ModuleCategory.entries.forEach { cat ->
            val color = getCategoryColor(cat)
            val tintedUp = upPatch.tint(color)
            val tintedDown = downPatch.tint(color)
            uiSkin.add(cat.name.lowercase(), TextButton.TextButtonStyle(tintedUp, tintedDown, tintedUp, font))
        }
    }

    private fun getCategoryColor(category: ModuleCategory): Color {
        return when (category) {
            ModuleCategory.PRIMARY -> Color.RED
            ModuleCategory.SECONDARY -> Color.ORANGE
            ModuleCategory.TERTIARY -> Color.YELLOW
            ModuleCategory.QUADRARY -> Color.PURPLE
            ModuleCategory.SHIELDS -> Color.BLUE
            ModuleCategory.POWER -> Color.GREEN
            ModuleCategory.DRONES -> Color.CYAN
            ModuleCategory.FIGHTERS -> Color.WHITE
        }
    }

    val whitePixel: TextureRegion get() = uiSkin.getRegion("white")

    override fun dispose() {
        texturesToDispose.forEach { it.dispose() }
        uiSkin.dispose()
    }
}
