package com.srw.seedofsov.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Disposable

/**
 * Manages the generation and caching of high-quality FreeType fonts.
 */
class FontManager : Disposable {
    private val generators = mutableMapOf<String, FreeTypeFontGenerator>()
    private val fonts = mutableMapOf<String, BitmapFont>()

    fun getFont(fontFile: String, size: Int): BitmapFont {
        val key = "${fontFile}_$size"
        if (fonts.containsKey(key)) return fonts[key]!!

        val generator = generators.getOrPut(fontFile) {
            FreeTypeFontGenerator(Gdx.files.internal(fontFile))
        }

        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            this.size = size
        }

        val font = generator.generateFont(parameter)
        fonts[key] = font
        return font
    }

    override fun dispose() {
        fonts.values.forEach { it.dispose() }
        generators.values.forEach { it.dispose() }
    }
}
