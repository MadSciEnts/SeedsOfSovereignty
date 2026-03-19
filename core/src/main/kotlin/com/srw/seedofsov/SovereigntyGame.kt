package com.srw.seedofsov

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.srw.seedofsov.ecs.systems.RenderSystem
import com.srw.seedofsov.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class SovereigntyGame : KtxGame<KtxScreen>() {
    val batch: Batch by lazy { SpriteBatch() }
    val engine: Engine by lazy {
        PooledEngine().apply {
            addSystem(RenderSystem(batch))
        }
    }

    override fun create() {
        KtxAsync.initiate()

        addScreen(GameScreen(this))
        setScreen<GameScreen>()

        super.create()
    }

    override fun dispose() {
        batch.dispose()
        super.dispose()
    }
}
