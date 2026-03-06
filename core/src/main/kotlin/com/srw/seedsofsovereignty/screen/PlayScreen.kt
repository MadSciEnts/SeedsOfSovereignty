package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import com.srw.seedsofsovereignty.ui.HUDFactory
import com.srw.seedsofsovereignty.ui.WeaponComparisonWidget
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

class PlayScreen(
    private val world: World,
    private val batch: SpriteBatch,
    private val viewport: Viewport,
    private val uiViewport: Viewport,
    private val drawer: ShapeDrawer
) : KtxScreen {

    private val uiStage = Stage(uiViewport, batch)
    private val skin: Skin = HUDFactory.createSkin()

    private val moveJoystick: Touchpad = HUDFactory.createMoveJoystick(skin)
    private val aimJoystick: Touchpad = HUDFactory.createAimJoystick(skin)
    private val fireButton: TextButton = HUDFactory.createFireButton(skin)

    private val repairButton: TextButton = HUDFactory.createStationButton("REPAIR SHIP", skin)
    private val undockButton: TextButton = HUDFactory.createStationButton("UNDOCK", skin)
    private val swapButton: TextButton = HUDFactory.createMarketButton("SWAP MODULE", "market-swap", skin)
    private val scrapButton: TextButton = HUDFactory.createMarketButton("SCRAP MODULE", "market-scrap", skin)
    private val marketWidget = WeaponComparisonWidget(skin, drawer)

    private val testEvolveButton: TextButton = HUDFactory.createDebugButton("DEV: EVOLVE", skin)

    private val dockingManager = DockingManager(world)
    private val evolutionManager = EvolutionManager(world)
    private val marketManager = MarketManager(world)

    init {
        // Initialize Player Entity
        world.entity {
            it += PlayerComponent()
            it += PositionComponent()
            it += OrientationComponent()
            it += VelocityComponent()
            it += WeaponComponent()
            it += WeaponModuleComponent(
                name = "Stock T1",
                projectileTier = 1,
                damage = 10f,
                projectileSpeed = 1000f,
                fireRate = 0.6f
            )
            it += SensorComponent(radius = 1000f)
            it += EvolutionComponent(currentTier = 1, subTier = 1, essenceCollected = 0, essenceRequired = 5)
            it += DockingComponent()
            it += ApertureComponent()
            it += HealthComponent(current = 100f, max = 100f)
            it += ShieldComponent(current = 50f, max = 50f)
        }

        // INITIAL TEST STATION
        world.entity {
            it += PositionComponent().apply { pos.set(500f, 500f) }
            it += StationComponent(name = "Alpha Outpost", radius = 200f)
            it += MarketComponent().apply {
                offeredModule = WeaponModuleComponent(
                    name = "T2 Heavy Proto",
                    projectileTier = 2,
                    damage = 35f,
                    fireRate = 0.45f
                )
            }
            it += VisibleComponent()
        }

        undockButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) { dockingManager.undock() }
        })

        repairButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val player = world.family { all(PlayerComponent, HealthComponent, ShieldComponent) }.firstOrNull()
                player?.let { p ->
                    with(world) {
                        p[HealthComponent].current = p[HealthComponent].max
                        p[ShieldComponent].current = p[ShieldComponent].max
                    }
                }
            }
        })

        swapButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val player = world.family { all(PlayerComponent, DockingComponent) }.firstOrNull()
                if (player != null) {
                    val station = with(world) { player[DockingComponent].nearestStation }
                    if (station != null) marketManager.swapModule(player, station)
                }
            }
        })

        scrapButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val player = world.family { all(PlayerComponent, DockingComponent) }.firstOrNull()
                if (player != null) {
                    val station = with(world) { player[DockingComponent].nearestStation }
                    if (station != null) marketManager.scrapModule(player, station)
                }
            }
        })

        testEvolveButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) { evolutionManager.cheatEvolve() }
        })

        uiStage.addActor(moveJoystick)
        uiStage.addActor(aimJoystick)
        uiStage.addActor(fireButton)
        uiStage.addActor(repairButton)
        uiStage.addActor(undockButton)
        uiStage.addActor(swapButton)
        uiStage.addActor(scrapButton)
        uiStage.addActor(marketWidget)
        uiStage.addActor(testEvolveButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        val player = world.family { all(PlayerComponent, EvolutionComponent, DockingComponent, ApertureComponent, WeaponModuleComponent) }.firstOrNull()

        if (player != null) {
            with(world) {
                val evo = player[EvolutionComponent]
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val currentModule = player[WeaponModuleComponent]

                val isTwinStickTier = evo.currentTier >= 4
                val isFullyDocked = docking.isDocked && aperture.state == ApertureComponent.State.OPEN

                moveJoystick.isVisible = !isFullyDocked
                aimJoystick.isVisible = !isFullyDocked && isTwinStickTier
                fireButton.isVisible = !isFullyDocked && !isTwinStickTier
                repairButton.isVisible = isFullyDocked
                undockButton.isVisible = isFullyDocked

                val station = docking.nearestStation
                val hasOffer = if (station != null && station.has(MarketComponent)) {
                    station[MarketComponent].offeredModule != null
                } else false

                swapButton.isVisible = isFullyDocked && hasOffer
                scrapButton.isVisible = isFullyDocked && hasOffer
                marketWidget.isVisible = isFullyDocked && hasOffer

                if (hasOffer && station != null) {
                    val offer = station[MarketComponent].offeredModule
                    if (offer != null) marketWidget.update(currentModule, offer)
                }

                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)
                if (isTwinStickTier) {
                    inputSystem.aimVector.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
                    inputSystem.isFiringRequested = !inputSystem.aimVector.isZero
                } else {
                    inputSystem.aimVector.set(0f, 0f)
                    inputSystem.isFiringRequested = fireButton.isPressed
                }
            }
        }

        world.update(delta)
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)

        moveJoystick.setPosition(50f, 50f)
        aimJoystick.setPosition(uiStage.width - aimJoystick.width - 50f, 50f)
        fireButton.setPosition(uiStage.width - fireButton.width - 50f, 50f)
        repairButton.setPosition(uiStage.width / 2f - repairButton.width / 2f, uiStage.height / 2f + 180f)
        undockButton.setPosition(uiStage.width / 2f - undockButton.width / 2f, uiStage.height / 2f - 220f)

        marketWidget.setPosition(uiViewport.worldWidth / 2f - marketWidget.width / 2f, uiViewport.worldHeight / 2f - 100f)
        swapButton.setPosition(uiViewport.worldWidth / 2f - 260f, uiViewport.worldHeight / 2f - 180f)
        scrapButton.setPosition(uiViewport.worldWidth / 2f + 10f, uiViewport.worldHeight / 2f - 180f)

        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 70f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
