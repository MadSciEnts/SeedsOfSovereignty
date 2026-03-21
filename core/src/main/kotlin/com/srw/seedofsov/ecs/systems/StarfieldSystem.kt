package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.srw.seedofsov.assets.generators.ui.StarfieldTextureGenerator
import ktx.graphics.use

/**
 * Renders a high-resolution texture-based starfield with 3-layer parallax.
 * Priority 40: Ensures it renders AFTER camera tracking is finalized.
 */
class StarfieldSystem(
    private val batch: Batch,
    private val viewport: Viewport
) : EntitySystem(40) {

    private val layers = mutableListOf<StarLayer>()
    private val worldTileSize = 100f

    private data class StarLayer(
        val texture: Texture,
        val scrollFactor: Float,
        val alpha: Float
    )

    init {
        val gen1 = StarfieldTextureGenerator(2048, 2048, 1500, 1234L)
        val gen2 = StarfieldTextureGenerator(2048, 2048, 800, 5678L)
        val gen3 = StarfieldTextureGenerator(2048, 2048, 300, 9012L)

        layers.add(StarLayer(Texture(gen1.generate()), 0.01f, 0.4f))
        layers.add(StarLayer(Texture(gen2.generate()), 0.05f, 0.7f))
        layers.add(StarLayer(Texture(gen3.generate()), 0.15f, 1.0f))

        gen1.dispose()
        gen2.dispose()
        gen3.dispose()
    }

    override fun update(deltaTime: Float) {
        val camera = viewport.camera
        val camX = camera.position.x
        val camY = camera.position.y

        viewport.apply()

        batch.use(camera.combined) { b ->
            b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)

            layers.forEach { layer ->
                b.setColor(1f, 1f, 1f, layer.alpha)
                val drawSize = worldTileSize
                val offsetX = (camX * layer.scrollFactor) % drawSize
                val offsetY = (camY * layer.scrollFactor) % drawSize

                for (i in -1..1) {
                    for (j in -1..1) {
                        val x = camX - drawSize / 2f + (i * drawSize) - offsetX
                        val drawY = camY - drawSize / 2f + (j * drawSize) - offsetY
                        b.draw(layer.texture, x, drawY, drawSize, drawSize)
                    }
                }
            }

            b.setColor(1f, 1f, 1f, 1f)
            b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        }
    }

    fun dispose() {
        layers.forEach { it.texture.dispose() }
    }
}
