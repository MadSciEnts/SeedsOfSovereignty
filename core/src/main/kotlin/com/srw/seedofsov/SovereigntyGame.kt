package com.srw.seedofsov

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.srw.seedofsov.assets.FontManager
import com.srw.seedofsov.assets.StyleRegistry
import com.srw.seedofsov.screens.*
import com.srw.seedofsov.ui.UiFactory
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class SovereigntyGame : KtxGame<KtxScreen>() {
    val batch: Batch by lazy { SpriteBatch() }
    val assetManager by lazy { AssetManager() }
    val fontManager by lazy { FontManager() }

    private var _uiFactory: UiFactory? = null

    /**
     * Factory is initialized only when requested.
     * Screens should only call this after ensuring loading is complete.
     */
    val uiFactory: UiFactory
        get() {
            if (_uiFactory == null) {
                // Generate base fonts immediately for initial screens
                val font = try {
                    fontManager.getFont("font-technical.ttf", 24)
                } catch (e: Exception) {
                    com.badlogic.gdx.graphics.g2d.BitmapFont()
                }

                val titleFont = try {
                    fontManager.getFont("font-technical.ttf", 48)
                } catch (e: Exception) {
                    com.badlogic.gdx.graphics.g2d.BitmapFont()
                }

                _uiFactory = UiFactory(StyleRegistry.EPOCH_1_STYLE, font, titleFont)
            }
            return _uiFactory!!
        }

    val engine: Engine by lazy { PooledEngine() }

    override fun create() {
        KtxAsync.initiate()

        // Register all screens
        addScreen(TitleScreen(this))
        addScreen(StartScreen(this))
        addScreen(LoadingScreen(this))
        addScreen(MainMenuScreen(this))
        addScreen(GameScreen(this))

        // First Screen is now TitleScreen directly
        setScreen<TitleScreen>()

        super.create()
    }

    override fun dispose() {
        batch.dispose()
        assetManager.dispose()
        fontManager.dispose()
        _uiFactory?.dispose()
        super.dispose()
    }
}
