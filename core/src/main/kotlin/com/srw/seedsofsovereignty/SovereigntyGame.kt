package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.srw.seedsofsovereignty.screen.PlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class SovereigntyGame : KtxGame<KtxScreen>() {
    // The Global Camera - shared across screens for consistent viewport math
    val camera by lazy {
        OrthographicCamera().apply {
            setToOrtho(false, 16f, 9f)
        }
    }

    override fun create() {
        // We'll eventually start with a SplashScreen or TitleScreen
        // For now, let's keep PlayScreen as the entry point but prepare the others

        // Register screens
        // addScreen(TitleScreen(this))
        // addScreen(MenuScreen(this))
        addScreen(PlayScreen(camera))

        setScreen<PlayScreen>()
    }

    override fun render() {
        super.render()
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = 16f
        camera.viewportHeight = 16f * (height.toFloat() / width.toFloat())
        camera.update()
        super.resize(width, height)
    }

    override fun dispose() {
        super.dispose()
    }
}
