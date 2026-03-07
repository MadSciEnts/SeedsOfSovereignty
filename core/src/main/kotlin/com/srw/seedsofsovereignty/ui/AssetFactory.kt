package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable

/**
 * Generates procedural assets at runtime to ensure high-bit pixel art consistency.
 */
object AssetFactory {

    fun createWhitePixel(): TextureRegion {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pix.setColor(Color.WHITE); pix.fill()
        val tex = Texture(pix)
        pix.dispose()
        return TextureRegion(tex)
    }

    fun createStarLayers(): List<Texture> {
        val layers = mutableListOf<Texture>()
        val counts = listOf(150, 70, 35)
        val sizes = listOf(1, 1, 2)

        counts.forEachIndexed { index, count ->
            val pix = Pixmap(512, 512, Pixmap.Format.RGBA8888)
            pix.setColor(0f, 0f, 0f, 0f); pix.fill()

            for (i in 0 until count) {
                val x = (Math.random() * 512).toInt()
                val y = (Math.random() * 512).toInt()

                val v = 0.3f + (Math.random().toFloat() * 0.7f)
                val roll = Math.random()
                when {
                    roll > 0.94 -> pix.setColor(0.6f * v, 0.9f * v, 1f * v, v)
                    roll > 0.88 -> pix.setColor(1f * v, 0.95f * v, 0.6f * v, v)
                    roll > 0.82 -> pix.setColor(1f * v, 0.4f * v, 0.4f * v, v * 0.7f)
                    else -> pix.setColor(v, v, v, v * 0.8f)
                }
                pix.fillRectangle(x, y, sizes[index], sizes[index])
            }
            val tex = Texture(pix)
            tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
            layers.add(tex)
            pix.dispose()
        }
        return layers
    }

    fun addProceduralAssets(skin: Skin) {
        // 1. SPECIMEN FRAME
        val s = 64
        val pix = Pixmap(s, s, Pixmap.Format.RGBA8888)
        val c = 12
        pix.setColor(0f, 0f, 0f, 0f); pix.fill()
        pix.setColor(0.4f, 0.9f, 1f, 1f)
        drawChamferedRect(pix, 0, 0, s, s, c, 2)
        pix.setColor(1f, 1f, 1f, 0.5f)
        drawChamferedRect(pix, 2, 2, s - 4, s - 4, c - 2, 1)
        pix.setColor(0f, 0f, 0f, 0.3f)
        drawChamferedRect(pix, 3, 3, s - 6, s - 6, c - 3, 1)
        val tex = Texture(pix)
        skin.add("specimen-frame", NinePatchDrawable(NinePatch(tex, 20, 20, 20, 20)), Drawable::class.java)
        pix.dispose()

        // 2. DATA NODE
        val pix2 = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pix2.setColor(Color.WHITE); pix2.fillRectangle(0, 0, 12, 12)
        pix2.setColor(Color.CYAN); pix2.drawRectangle(0, 0, 12, 12)
        skin.add("data-node", TextureRegion(Texture(pix2)))
        pix2.dispose()

        // 3. ESSENCE CORE (UPDATED: High-visibility tritone core)
        val pix3 = Pixmap(32, 32, Pixmap.Format.RGBA8888)
        pix3.setColor(0f, 0f, 0f, 0f); pix3.fill()
        pix3.setColor(1f, 0.6f, 0f, 0.4f); pix3.fillCircle(16, 16, 15)
        pix3.setColor(1f, 0.8f, 0.1f, 0.9f); pix3.drawCircle(16, 16, 10); pix3.drawCircle(16, 16, 9)
        pix3.setColor(Color.WHITE); pix3.fillCircle(16, 16, 5)
        pix3.setColor(Color.CYAN); pix3.drawCircle(16, 16, 4)
        skin.add("essence-core", TextureRegion(Texture(pix3)))
        pix3.dispose()

        // 4. SALVAGE BRACKET (New: High-contrast technical frame)
        val pix4 = Pixmap(32, 32, Pixmap.Format.RGBA8888)
        pix4.setColor(0f, 0f, 0f, 0f); pix4.fill()
        pix4.setColor(1f, 0.7f, 0f, 1f)
        val bS = 8
        pix4.drawLine(0, 0, bS, 0); pix4.drawLine(0, 0, 0, bS)
        pix4.drawLine(31, 0, 31 - bS, 0); pix4.drawLine(31, 0, 31, bS)
        pix4.drawLine(0, 31, bS, 31); pix4.drawLine(0, 31, 0, 31 - bS)
        pix4.drawLine(31, 31, 31 - bS, 31); pix4.drawLine(31, 31, 31, 31 - bS)
        skin.add("salvage-bracket", TextureRegion(Texture(pix4)))
        pix4.dispose()

        // 5. VFX ASSETS
        val pix5 = Pixmap(8, 8, Pixmap.Format.RGBA8888)
        pix5.setColor(Color.WHITE); pix5.fill()
        skin.add("vfx-spark", TextureRegion(Texture(pix5)))
        pix5.dispose()

        val pix6 = Pixmap(32, 32, Pixmap.Format.RGBA8888)
        pix6.setColor(1f, 1f, 1f, 1f); pix6.fillCircle(16, 16, 14)
        skin.add("vfx-glow", TextureRegion(Texture(pix6)))
        pix6.dispose()
    }

    private fun drawChamferedRect(pix: Pixmap, x: Int, y: Int, w: Int, h: Int, c: Int, thickness: Int) {
        for (t in 0 until thickness) {
            val ox = x + t; val oy = y + t; val ow = w - t * 2; val oh = h - t * 2
            pix.drawLine(ox + c, oy, ox + ow - c, oy)
            pix.drawLine(ox + c, oy + oh - 1, ox + ow - c, oy + oh - 1)
            pix.drawLine(ox, oy + c, ox, oy + oh - c)
            pix.drawLine(ox + ow - 1, oy + c, ox + ow - 1, oy + oh - c)
            pix.drawLine(ox, oy + c, ox + c, oy)
            pix.drawLine(ox + ow - c, oy, ox + ow - 1, oy + c)
            pix.drawLine(ox, oy + oh - c, ox + c, oy + oh - 1)
            pix.drawLine(ox + ow - c, oy + oh - 1, ox + ow - 1, oy + oh - c)
        }
    }
}
