package com.srw.seedofsov.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.srw.seedofsov.SovereigntyGame

class TitleScreen(game: SovereigntyGame) : BaseScreen(game) {
    private var uiInitialized = false

    override fun render(delta: Float) {
        val s = _stage
        if (!uiInitialized && s != null && s.width > 0 && s.height > 0) {
            setupUi(s)
            uiInitialized = true
        }
        super.render(delta)

        // Proceed to Start Screen
        if (Gdx.input.justTouched()) {
            game.setScreen<StartScreen>()
        }
    }

    private fun setupUi(s: Stage) {
        // Use raw LabelStyles to avoid dependency on uninitialized UiFactory textures
        val titleStyle = Label.LabelStyle(game.uiFactory.titleFont, Color.WHITE)
        val defaultStyle = Label.LabelStyle(game.uiFactory.font, Color.GRAY)

        val srwLabel = Label("SRW", titleStyle).apply {
            setPosition((s.width - width) / 2f, (s.height - height) / 2f + 50f)
        }
        s.addActor(srwLabel)

        val tapLabel = Label("Tap to Continue...", defaultStyle).apply {
            setPosition((s.width - width) / 2f, srwLabel.y - 60f)
            addAction(forever(sequence(fadeIn(1f), fadeOut(1f))))
        }
        s.addActor(tapLabel)
    }
}
