package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.screen.PlayScreen
import com.srw.seedsofsovereignty.system.*
import com.srw.seedsofsovereignty.ui.AssetFactory
import com.srw.seedsofsovereignty.ui.HUDFactory
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Sovereign Strategic Hub (v2.5.4).
 * ROLE: Primary Registry with Aspect-Synchronized viewports.
 *
 * FIXES:
 * 1. [ASPECT] - Hardened viewport creation to ensure circular hulls.
 * 2. [PARTITION] - Maintained physics/UI separation for forensic clarity.
 */
class SovereigntyGame : ktx.app.KtxGame<ktx.app.KtxScreen>() {
    private val batch by lazy { SpriteBatch() }

    // 1. PRIMARY PHYSICS CAMERA (Isolated)
    private val shipCamera by lazy { OrthographicCamera() }
    private val shipViewport by lazy { ExtendViewport(1280f, 720f, shipCamera) }

    // 2. PRIMARY UI CAMERA
    private val uiViewport by lazy { ExtendViewport(1280f, 720f) }

    private val whitePixel by lazy { AssetFactory.createWhitePixel() }
    private val starTextures by lazy { AssetFactory.createStarLayers() }
    private val drawer by lazy { ShapeDrawer(batch, whitePixel) }
    private val skin: Skin by lazy { HUDFactory.createSkin() }

    private val evolutionManager = EvolutionManager()
    private val sectorManager = SectorManager()

    private val world: World by lazy {
        configureWorld {
            injectables {
                add(batch); add(skin); add(shipCamera)
                add("gameViewport", shipViewport); add("uiViewport", uiViewport)
                add(drawer); add(evolutionManager); add(sectorManager)
            }
            systems {
                add(ClearScreenSystem())
                add(SectorPopulationSystem(shipCamera, sectorManager))
                add(StarfieldSystem(batch, shipCamera, starTextures))

                add(PlayerInputSystem(shipViewport))
                add(EnemyAISystem(shipCamera))

                add(DroneRegenSystem())
                add(DroneFormationSystem(shipCamera))

                add(MoveSystem())
                add(CameraSystem(shipCamera))

                add(WeaponSystem())
                add(CombatSystem())
                add(CollisionDamageSystem())
                add(VitalsSystem())
                add(StatusEffectSystem())
                add(LifeSpanSystem())

                add(EvolutionSystem(evolutionManager))
                add(EvolutionCelebrationSystem(batch, drawer, uiViewport))

                add(SensorSystem())
                add(SensorVisualSystem())
                add(StationSystem())
                add(SalvageSystem())

                // MANDATORY: Physics Objects FIRST (Ship Viewport)
                add(EntityRenderSystem(batch, drawer, shipViewport, skin))

                // MANDATORY: VFX SECOND (Ship Viewport)
                add(RenderSystem(batch, drawer, shipViewport, skin))

                add(RadarRenderSystem(batch, drawer, uiViewport))
                add(DiagnosticSignalSystem())

                // MANDATORY: Diegetic HUD TOP (Bridge between spaces)
                add(DiegeticHUDSystem(batch, drawer, shipViewport, uiViewport, skin, sectorManager))

                add(ApertureSystem(batch, drawer, uiViewport))
                add(OriginSystem())
            }
        }
    }

    override fun create() {
        ktx.async.KtxAsync.initiate()
        addScreen(PlayScreen(world, batch, shipViewport, uiViewport, drawer))
        setScreen<PlayScreen>()
    }

    override fun dispose() {
        super.dispose()
        batch.dispose(); skin.dispose()
        whitePixel.texture.dispose()
        starTextures.forEach { it.dispose() }
        world.dispose()
    }
}
