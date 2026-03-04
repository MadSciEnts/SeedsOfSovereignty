package com.yourname.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.srw.seedsofsovereignty.screen.PlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class SovereigntyGame : KtxGame<KtxScreen>() {
    // The Global Camera - we create it here so we can pass it to any screen
    private val camera by lazy { 
        OrthographicCamera().apply {
            // Set the initial viewport size (units, not pixels)
            // 16:9 aspect ratio units make math easier
            setToOrtho(false, 16f, 9f) 
        }
    }

    override fun create() {
        // Register our main gameplay screen
        addScreen(PlayScreen(camera))
        
        // Set the active screen to the PlayScreen
        setScreen<PlayScreen>()
    }

    override fun render() {
        // This calls the render method of the currently active screen
        super.render()
    }

    override fun resize(width: Int, height: Int) {
        // Essential for Android: handles screen rotation or window resizing
        // This ensures the 16:9 units scale properly to the device resolution
        camera.viewportWidth = 16f
        camera.viewportHeight = 16f * (height.toFloat() / width.toFloat())
        camera.update()
        super.resize(width, height)
    }

    override fun dispose() {
        // Clean up memory
        super.dispose()
    }
}