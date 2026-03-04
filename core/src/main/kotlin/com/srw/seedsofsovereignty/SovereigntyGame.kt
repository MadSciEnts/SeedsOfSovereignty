package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class SovereigntyGame : KtxGame<KtxScreen>() {
    private val batch by lazy { SpriteBatch() }

    // Fleks World will hold our ECS logic
    private val world: World by lazy {
        configureWorld {
            inject(batch)
            // Systems will be registered here (e.g., MoveSystem, RenderSystem)
        }
    }

    override fun create() {
        KtxAsync.initiate()
        // Screens will be added here
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        world.dispose()
    }
}
