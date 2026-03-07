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
 * Optimized for high-contrast "Chamber Layering" aesthetics.
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
        // 1. Specimen Frame (Station Core)
        val s = 64
        val pix = Pixmap(s, s, Pixmap.Format.RGBA8888)
        val c = 12
        pix.setColor(0f, 0f, 0f, 0f); pix.fill()
        pix.setColor(0.4f, 0.9f, 1f, 1f)
        drawChamferedRect(pix, 0, 0, s, s, c, 2)
        pix.setColor(1f, 1f, 1f, 0.5f)
        drawChamferedRect(pix, 2, 2, s - 4, s - 4, c - 2, 1)

        val tex = Texture(pix)
        skin.add("specimen-frame", NinePatchDrawable(NinePatch(tex, 20, 20, 20, 20)), Drawable::class.java)
        pix.dispose()

        // 2. Data Node
        val pix2 = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pix2.setColor(Color.WHITE); pix2.fillRectangle(0, 0, 12, 12)
        pix2.setColor(Color.CYAN); pix2.drawRectangle(0, 0, 12, 12)
        skin.add("data-node", TextureRegion(Texture(pix2)))
        pix2.dispose()

        // 3. Spark Texture (Cross-hair style high-bit spark)
        val spSize = 16
        val sparkPix = Pixmap(spSize, spSize, Pixmap.Format.RGBA8888)
        sparkPix.setColor(0f,0f,0f,0f); sparkPix.fill()
        sparkPix.setColor(Color.WHITE)
        sparkPix.fillRectangle(spSize/2 - 1, 2, 2, spSize - 4)
        sparkPix.fillRectangle(2, spSize/2 - 1, spSize - 4, 2)
        skin.add("vfx-spark", TextureRegion(Texture(sparkPix)))
        sparkPix.dispose()

        // 4. Glow Texture (Soft radial)
        val glSize = 32
        val glowPix = Pixmap(glSize, glSize, Pixmap.Format.RGBA8888)
        glowPix.setColor(0f,0f,0f,0f); glowPix.fill()
        for(i in 0 until glSize/2) {
            val alpha = 1f - (i.toFloat() / (glSize/2f))
            glowPix.setColor(1f, 1f, 1f, alpha * alpha)
            glowPix.drawCircle(glSize/2, glSize/2, i)
        }
        skin.add("vfx-glow", TextureRegion(Texture(glowPix)))
        glowPix.dispose()

        // 5. Station Blueprint Texture
        val bSize = 128
        val bPix = Pixmap(bSize, bSize, Pixmap.Format.RGBA8888)
        bPix.setColor(0f,0f,0f,0f); bPix.fill()
        bPix.setColor(0.2f, 0.6f, 1f, 0.3f)
        for (i in 0 until bSize step 16) {
            bPix.drawLine(i, 0, i, bSize)
            bPix.drawLine(0, i, bSize, i)
        }
        bPix.setColor(0.2f, 0.6f, 1f, 0.8f)
        bPix.drawRectangle(0, 0, bSize, bSize)
        skin.add("station-blueprint", TextureRegion(Texture(bPix)))
        bPix.dispose()
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
