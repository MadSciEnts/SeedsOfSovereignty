package com.srw.seedofsov.assets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.srw.seedofsov.assets.generators.ProjectileAssetGenerator

/**
 * Ensures core safety textures are available synchronously and persists impact assets.
 * Truth on Disk: Centralized authority for procedural weapon and impact textures.
 */
object TextureRegistry : Disposable {
    private var _voidTexture: Texture? = null

    private var _shieldImpactTexture: Texture? = null
    private var _debrisTexture: Texture? = null
    private var _explosionTexture: Texture? = null
    private var _sparkTexture: Texture? = null

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

    val shieldImpactTexture: Texture
        get() {
            if (_shieldImpactTexture == null) {
                val gen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)
                _shieldImpactTexture = Texture(gen.generateShieldImpact())
            }
            return _shieldImpactTexture!!
        }

    val debrisTexture: Texture
        get() {
            if (_debrisTexture == null) {
                val gen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)
                _debrisTexture = Texture(gen.generateHullDebris())
            }
            return _debrisTexture!!
        }

    val explosionTexture: Texture
        get() {
            if (_explosionTexture == null) {
                val gen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)
                _explosionTexture = Texture(gen.generateExplosion())
            }
            return _explosionTexture!!
        }

    val sparkTexture: Texture
        get() {
            if (_sparkTexture == null) {
                val gen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)
                _sparkTexture = Texture(gen.generateSpark())
            }
            return _sparkTexture!!
        }

    override fun dispose() {
        _voidTexture?.dispose()
        _voidTexture = null
        _shieldImpactTexture?.dispose()
        _shieldImpactTexture = null
        _debrisTexture?.dispose()
        _debrisTexture = null
        _explosionTexture?.dispose()
        _explosionTexture = null
        _sparkTexture?.dispose()
        _sparkTexture = null
    }
}
