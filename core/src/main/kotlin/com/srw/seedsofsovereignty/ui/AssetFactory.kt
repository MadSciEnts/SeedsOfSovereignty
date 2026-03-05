package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

object AssetFactory {
    fun createWhitePixel(): TextureRegion {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE)
            fill()
        }
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(texture)
    }

    fun createStarLayers(): List<Texture> {
        val layers = mutableListOf<Texture>()
        val sizes = listOf(1024, 1024, 1024)
        val starCounts = listOf(200, 150, 100)

        sizes.forEachIndexed { index, size ->
            val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888).apply {
                setColor(0f, 0f, 0f, 0f)
                fill()
            }

            for (i in 0 until starCounts[index]) {
                val brightness = 0.5f + (Math.random() * 0.5f).toFloat()
                val x = (Math.random() * size).toInt()
                val y = (Math.random() * size).toInt()

                pixmap.setColor(brightness, brightness, brightness, 1f)
                pixmap.drawPixel(x, y)
                pixmap.setColor(brightness, brightness, brightness, 0.4f)
                pixmap.drawPixel(x + 1, y)
                pixmap.drawPixel(x - 1, y)
                pixmap.drawPixel(x, y + 1)
                pixmap.drawPixel(x, y - 1)
            }

            val tex = Texture(pixmap, true).apply {
                setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
                setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
            }
            layers.add(tex)
            pixmap.dispose()
        }
        return layers
    }
}
