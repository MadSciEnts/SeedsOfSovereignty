package com.srw.seedsofsovereignty

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.registry.SystemRegistry.configureSystems
import com.srw.seedsofsovereignty.screen.PlayScreen
import com.srw.seedsofsovereignty.ui.AssetFactory
import com.srw.seedsofsovereignty.ui.HUDFactory
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Sovereign Strategic Hub (v2.6.0).
 * ROLE: Entry point utilizing the Sovereign Vault architectures.
 *
 * FIXES:
 * 1. [SECURITY] - Migrated ECS draw order to SystemRegistry.kt for Lead Architect protection.
 */
class SovereigntyGame : KtxGame<KtxScreen>() {
    private val batch by lazy { SpriteBatch() }
    private val shipCamera by lazy { OrthographicCamera() }
    private val shipViewport by lazy { ExtendViewport(1280f, 720f, shipCamera) }
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
            // --- PROTECTED REGISTRY HANDOFF ---
            configureSystems(
                batch, drawer, shipCamera, shipViewport, uiViewport,
                skin, evolutionManager, sectorManager, starTextures
            )
        }
    }

    override fun create() {
        KtxAsync.initiate()
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
