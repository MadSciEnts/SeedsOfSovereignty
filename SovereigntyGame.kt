package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.screen.PlayScreen
import com.srw.seedsofsovereignty.system.*
import com.srw.seedsofsovereignty.ui.AssetFactory
import com.srw.seedsofsovereignty.ui.HUDFactory
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
    private val skin: Skin by lazy { HUDFactory.createSkin() }

    private val evolutionManager = EvolutionManager()

    private val world: World by lazy {
        configureWorld {
            injectables {
                add(batch)
                add(skin)
                add(camera)
                add("gameViewport", viewport)
                add("uiViewport", uiViewport)
                add(drawer)
                add(evolutionManager)
            }
            systems {
                add(ClearScreenSystem())
                add(StationSpawnSystem())
                add(EnemySpawnSystem(camera))
                add(StarfieldSystem(batch, camera, starTextures))

                add(PlayerInputSystem())
                add(EnemyAISystem(camera))

                add(DroneRegenSystem())
                add(DroneFormationSystem(camera))

                add(MoveSystem())
                add(CameraSystem(camera))

                add(WeaponSystem())
                add(CombatSystem())
                add(CollisionDamageSystem())
                add(VitalsSystem())
                add(StatusEffectSystem())
                add(EffectSystem())
                add(LifeSpanSystem())

                add(CollectionSystem())
                add(EvolutionSystem(evolutionManager))
                add(EvolutionCelebrationSystem(batch, drawer, uiViewport))

                add(SensorSystem())
                add(StationSystem())
                add(ApertureSystem(batch, drawer, uiViewport))

                // FIXED: Explicitly pass dependencies to avoid Fleks NoSuchInjectableException
                add(EntityRenderSystem(batch, drawer, viewport, skin))
                add(RenderSystem(batch, camera, drawer))

                add(RadarRenderSystem(batch, drawer, uiViewport))
                add(HUDSystem(batch, drawer, uiViewport))

                add(OriginSystem())
            }
        }
    }

    override fun create() {
        KtxAsync.initiate()
        addScreen(PlayScreen(world, batch, viewport, uiViewport, drawer))
        setScreen<PlayScreen>()
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        skin.dispose()
        whitePixel.texture.dispose()
        starTextures.forEach { it.dispose() }
        world.dispose()
    }
}
