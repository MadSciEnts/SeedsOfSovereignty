package com.srw.seedofsov.screens

import com.badlogic.gdx.utils.ScreenUtils
import com.srw.seedofsov.SovereigntyGame
import ktx.app.KtxScreen

class GameScreen(
    private val game: SovereigntyGame
) : KtxScreen {
    override fun render(delta: Float) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        game.engine.update(delta)
    }

    override fun dispose() {
        // Dispose screen-specific assets here
    }
}
