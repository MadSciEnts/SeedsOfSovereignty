package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable

/**
 * Service for generating and caching procedural textures.
 */
class TextureService : Disposable {
    private val textureCache = mutableMapOf<String, TextureRegion>()
    private val texturesToDispose = mutableListOf<Texture>()

    /**
     * Returns a 1x1 white pixel texture region, essential for ShapeDrawer.
     */
    val whitePixel: TextureRegion by lazy {
        getOrCreate("white_pixel") {
            val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
            pixmap.setColor(Color.WHITE)
            pixmap.fill()
            val texture = Texture(pixmap)
            pixmap.dispose()
            TextureRegion(texture)
        }
    }

    private fun getOrCreate(key: String, factory: () -> TextureRegion): TextureRegion {
        return textureCache.getOrPut(key) {
            val region = factory()
            texturesToDispose.add(region.texture)
            region
        }
    }

    override fun dispose() {
        texturesToDispose.forEach { it.dispose() }
        textureCache.clear()
    }
}
