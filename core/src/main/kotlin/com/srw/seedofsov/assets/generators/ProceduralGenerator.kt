package com.srw.seedofsov.assets.generators

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.utils.Disposable

/**
 * Base interface for all procedural texture generators.
 */
interface ProceduralGenerator : Disposable {
    /**
     * Generates a Pixmap representing the asset.
     * The caller is responsible for disposing the returned Pixmap or uploading it to a Texture.
     */
    fun generate(): Pixmap

    override fun dispose() {}
}
