package com.srw.seedofsov.assets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable

/**
 * Ensures core safety textures are available synchronously.
 * Truth on Disk: Provides a 'VOID' texture to prevent NPEs during entity pooling.
 */
object TextureRegistry : Disposable {
    private var _voidTexture: Texture? = null

    val voidTexture: Texture
        get() {
            if (_voidTexture == null) {
                val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
                pixmap.setColor(Color.WHITE)
                pixmap.fill()
                _voidTexture = Texture(pixmap)
                pixmap.dispose()
            }
            return _voidTexture!!
        }

    override fun dispose() {
        _voidTexture?.dispose()
        _voidTexture = null
    }
}
