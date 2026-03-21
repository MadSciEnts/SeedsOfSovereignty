package com.srw.seedofsov.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.srw.seedofsov.SovereigntyGame
import ktx.app.KtxScreen

abstract class BaseScreen(val game: SovereigntyGame) : KtxScreen {
    protected var _stage: Stage? = null

    // Explicitly check for null to prevent NPEs
    val stage: Stage
        get() = _stage ?: throw IllegalStateException("Stage accessed before initialization in show()")

    override fun show() {
        if (_stage == null) {
            _stage = Stage(ScreenViewport(), game.batch)
        }
        Gdx.input.inputProcessor = _stage
    }

    override fun resize(width: Int, height: Int) {
        _stage?.viewport?.update(width, height, true)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f)
        _stage?.let {
            it.act(delta)
            it.draw()
        }
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        _stage?.dispose()
        _stage = null
    }
}
