package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.screen.PlayScreen
import com.srw.seedsofsovereignty.system.*
import com.srw.seedsofsovereignty.ui.AssetFactory
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import space.earlygrey.shapedrawer.ShapeDrawer

class SovereigntyGame : KtxGame<KtxScreen>() {
    private val batch by lazy { SpriteBatch() }
    private val camera by lazy { OrthographicCamera() }
    private val viewport by lazy { ExtendViewport(1280f, 720f, camera) }
    private val uiViewport by lazy { ExtendViewport(1280f, 720f) }

    private val whitePixel by lazy { AssetFactory.createWhitePixel() }
    private val starTextures by lazy { AssetFactory.createStarLayers() }
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
                add(StationSpawnSystem())
                add(EnemySpawnSystem(camera))
                add(StarfieldSystem(batch, camera, starTextures))

                add(PlayerInputSystem())
                add(EnemyAISystem(camera))

                // NEW CONSOLIDATED DRONE DOMAIN
                add(DroneRegenSystem())
                add(DroneFormationSystem(camera))

                add(MoveSystem())
                add(CameraSystem(camera))

                add(WeaponSystem())
                add(CombatSystem())
                add(CollisionDamageSystem())
                add(VitalsSystem())
                add(EffectSystem())
                add(LifeSpanSystem())

                add(CollectionSystem())
                add(EvolutionSystem())

                add(SensorSystem())
                add(StationSystem())
                add(ApertureSystem(batch, drawer, uiViewport))
                add(RenderSystem(batch, camera, drawer))
                add(RadarRenderSystem(batch, drawer, uiViewport))
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
