package com.srw.seedofsov.screens

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.srw.seedofsov.SovereigntyGame
import ktx.app.KtxScreen

/**
 * Displays loading progress while external assets and procedural textures are prepared.
 */
class LoadingScreen(game: SovereigntyGame) : BaseScreen(game) {
    private var uiInitialized = false
    private var nextScreen: Class<out KtxScreen>? = null
    private var progressLabel: Label? = null

    /**
     * Sets the destination screen to transition to after loading.
     */
    fun setDestination(screenClass: Class<out KtxScreen>) {
        nextScreen = screenClass
    }

    override fun render(delta: Float) {
        val s = _stage
        if (!uiInitialized && s != null && s.width > 0 && s.height > 0) {
            setupUi(s)
            uiInitialized = true
        }

        // 1. Update AssetManager (external files)
        val assetsFinished = game.assetManager.update()

        // 2. Update UiFactory (procedural textures)
        val proceduralFinished = game.uiFactory.update()

        // 3. Update UI feedback
        val totalProgress = (game.assetManager.progress + game.uiFactory.getProgress()) / 2f
        progressLabel?.setText("LOADING... ${(totalProgress * 100).toInt()}%")

        // 4. Transition when both are complete
        if (assetsFinished && proceduralFinished) {
            nextScreen?.let {
                game.setScreen(it)
                nextScreen = null
            }
        }

        super.render(delta)
    }

    private fun setupUi(s: Stage) {
        // Note: Title screen uses large font, but we'll use default here
        val factory = game.uiFactory
        val label = factory.createLabel("LOADING... 0%").apply {
            setPosition((s.width - width) / 2f, (s.height - height) / 2f)
        }
        progressLabel = label
        s.addActor(label)
    }
}
