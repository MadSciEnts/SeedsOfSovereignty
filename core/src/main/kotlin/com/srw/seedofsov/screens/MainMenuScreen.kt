package com.srw.seedofsov.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ui.Panel

class MainMenuScreen(game: SovereigntyGame) : BaseScreen(game) {
    private var uiInitialized = false
    private var menuPanel: Panel? = null

    override fun render(delta: Float) {
        // Ensure stage is initialized by super.render or super.show
        val s = _stage
        if (!uiInitialized && s != null && s.width > 0 && s.height > 0) {
            setupUi(s)
            uiInitialized = true
        }
        super.render(delta)
    }

    private fun setupUi(s: Stage) {
        val factory = game.uiFactory

        // Create a central container panel
        val panel = Panel(factory.skin.getDrawable("panel")).apply {
            setSize(400f, 500f)
            setPosition(
                (s.width - 400f) / 2f,
                (s.height - 500f) / 2f
            )
        }
        menuPanel = panel
        s.addActor(panel)

        // Add Title
        val title = factory.createLabel("Main Menu").apply {
            setPosition((panel.width - width) / 2f, panel.height - 60f)
        }
        panel.addActor(title)

        // Add Start Button
        val startBtn = factory.createButton("START SECTOR").apply {
            setSize(300f, 60f)
            setPosition((panel.width - 300f) / 2f, 300f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    val loadingScreen = game.getScreen<LoadingScreen>()
                    loadingScreen.setDestination(GameScreen::class.java)
                    game.setScreen<LoadingScreen>()
                }
            })
        }
        panel.addActor(startBtn)

        // Add Options Button
        val optionsBtn = factory.createButton("OPTIONS").apply {
            setSize(300f, 60f)
            setPosition((panel.width - 300f) / 2f, 220f)
        }
        panel.addActor(optionsBtn)

        // Add Exit Button
        val exitBtn = factory.createButton("EXIT").apply {
            setSize(300f, 60f)
            setPosition((panel.width - 300f) / 2f, 140f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        }
        panel.addActor(exitBtn)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        val s = _stage
        val p = menuPanel
        if (uiInitialized && s != null && p != null) {
            p.setPosition(
                (s.width - p.width) / 2f,
                (s.height - p.height) / 2f
            )
        }
    }
}
