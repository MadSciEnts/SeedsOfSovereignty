package com.srw.seedofsov.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.srw.seedofsov.SovereigntyGame

class StartScreen(game: SovereigntyGame) : BaseScreen(game) {
    private var uiInitialized = false

    override fun render(delta: Float) {
        val s = _stage
        if (!uiInitialized && s != null && s.width > 0 && s.height > 0) {
            setupUi(s)
            uiInitialized = true
        }
        super.render(delta)

        // Route through Loading Screen to Main Menu
        if (Gdx.input.justTouched()) {
            val loadingScreen = game.getScreen<LoadingScreen>()
            loadingScreen.setDestination(MainMenuScreen::class.java)
            game.setScreen<LoadingScreen>()
        }
    }

    private fun setupUi(s: Stage) {
        // Use raw LabelStyles to avoid dependency on uninitialized procedural textures
        val titleStyle = Label.LabelStyle(game.uiFactory.titleFont, Color.CYAN)
        val defaultStyle = Label.LabelStyle(game.uiFactory.font, Color.GRAY)

        // Game Title
        val titleLabel = Label("SEEDS OF SOVEREIGNTY", titleStyle).apply {
            setPosition((s.width - width) / 2f, (s.height - height) / 2f + 50f)
        }
        s.addActor(titleLabel)

        // Pulsing Tap to Continue
        val tapLabel = Label("Tap to Continue...", defaultStyle).apply {
            setPosition((s.width - width) / 2f, titleLabel.y - 60f)
            addAction(forever(sequence(fadeIn(1f), fadeOut(1f))))
        }
        s.addActor(tapLabel)
    }
}
