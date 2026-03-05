package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.screen.PlayScreen
import com.srw.seedsofsovereignty.system.*
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import space.earlygrey.shapedrawer.ShapeDrawer

class SovereigntyGame : KtxGame<KtxScreen>() {
    private val batch by lazy { SpriteBatch() }
    private val camera by lazy { OrthographicCamera() }
    private val viewport by lazy { ExtendViewport(1280f, 720f, camera) }
    private val uiViewport by lazy { ExtendViewport(1280f, 720f) }

    private val whitePixel by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        TextureRegion(texture)
    }

    private val starTextures by lazy {
        val layers = mutableListOf<Texture>()
        val sizes = listOf(1024, 1024, 1024)
        val starCounts = listOf(200, 150, 100)

        sizes.forEachIndexed { index, size ->
            val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            for (i in 0 until starCounts[index]) {
                val brightness = 0.5f + (Math.random() * 0.5f).toFloat()
                val x = (Math.random() * size).toInt()
                val y = (Math.random() * size).toInt()

                pixmap.setColor(brightness, brightness, brightness, 1f)
                pixmap.drawPixel(x, y)
                pixmap.setColor(brightness, brightness, brightness, 0.4f)
                pixmap.drawPixel(x + 1, y)
                pixmap.drawPixel(x - 1, y)
                pixmap.drawPixel(x, y + 1)
                pixmap.drawPixel(x, y - 1)
            }

            val tex = Texture(pixmap, true)
            tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
            tex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
            layers.add(tex)
            pixmap.dispose()
        }
        layers
    }

    private val drawer by lazy { ShapeDrawer(batch, whitePixel) }

    private val world: World by lazy {
        configureWorld {
            injectables {
                add(batch)
                add(camera)
                add("gameViewport", viewport)
                add("uiViewport", uiViewport)
                add(drawer)
            }
            systems {
                add(ClearScreenSystem())
                add(PlayerInputSystem())
                add(WeaponSystem())
                add(LifeSpanSystem())
                add(DroneSystem())
                add(EnemySpawnSystem(camera))
                add(StationSpawnSystem()) // ADDED: Missing Spawn System
                add(EnemyAISystem())
                add(CombatSystem())
                add(CollectionSystem())
                add(EvolutionSystem())
                add(SensorSystem())
                add(StationSystem())
                add(MoveSystem())
                add(CameraSystem(camera))
                add(StarfieldSystem(batch, camera, starTextures))
                add(RenderSystem(batch, camera, drawer))
                add(RadarRenderSystem(batch, drawer, uiViewport))
                add(ApertureSystem(batch, drawer, uiViewport))
                add(HUDSystem(batch, drawer, uiViewport))
                add(OriginSystem())
            }
        }
    }

    override fun create() {
        KtxAsync.initiate()
        addScreen(PlayScreen(world, viewport, uiViewport))
        setScreen<PlayScreen>()
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        whitePixel.texture.dispose()
        starTextures.forEach { it.dispose() }
        world.dispose()
    }
}
