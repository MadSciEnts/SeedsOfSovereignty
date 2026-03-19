package com.srw.seedofsov

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class SovereigntyGame : KtxGame<KtxScreen>() {
    val batch: Batch by lazy { SpriteBatch() }
    val engine: Engine by lazy { PooledEngine() }

    override fun create() {
        KtxAsync.initiate()

        // Initial setup for ECS and Screens will go here

        super.create()
    }

    override fun dispose() {
        batch.dispose()
        super.dispose()
    }
}
