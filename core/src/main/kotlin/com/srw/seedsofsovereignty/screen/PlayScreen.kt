package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import com.srw.seedsofsovereignty.ui.HUDFactory
import com.srw.seedsofsovereignty.ui.StationHangarUI
import com.srw.seedsofsovereignty.util.ModuleGenerator
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

    private val testEvolveButton: TextButton = HUDFactory.createDebugButton("DEV: EVOLVE", skin)

    private val dockingManager = DockingManager(world)
    private val evolutionManager = EvolutionManager(world)
    private val marketManager = MarketManager(world)

    private val hangarUI = StationHangarUI(skin, drawer, world, dockingManager, marketManager)

    private var wasDocked = false

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
                description = "Standard kinetic stream. Reliable but primitive.",
                projectileTier = 1,
                projectileSpeed = 1000f,
                fireRate = 0.6f,
                rarity = Rarity.COMMON,
                projectileColor = Color.YELLOW,
                effects = mutableListOf(
                    ProjectileEffect(EffectType.HULL_DAMAGE, 10f, vfx = ImpactVfxType.SPARK_YELLOW),
                    ProjectileEffect(EffectType.SHIELD_DAMAGE, 10f, vfx = ImpactVfxType.SHIELD_RIPPLE_BLUE)
                )
            )
            it += SensorComponent(radius = 1000f)
            it += EvolutionComponent(currentTier = 1, subTier = 1, essenceCollected = 0, essenceRequired = 5)
            it += DockingComponent()
            it += ApertureComponent()
            it += ChoiceComponent()
            it += HealthComponent(current = 100f, max = 100f)
            it += ShieldComponent(current = 50f, max = 50f)

            it += ModuleSlotComponent(mutableListOf(
                ModuleSlot(SlotType.WEAPON, 1, "Stock Kinetic", 1),
                ModuleSlot(SlotType.DRONE, 1, "EMPTY", 0)
            ))
        }

        // INITIAL TEST STATION
        world.entity {
            it += PositionComponent().apply { pos.set(500f, 500f) }
            it += StationComponent(name = "Alpha Outpost", radius = 200f)
            it += MarketComponent().apply {
                for (i in 0 until 30) {
                    weaponOffers.add(ModuleGenerator.generateWeapon(1))
                }
            }
            it += VisibleComponent()
        }

        testEvolveButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                evolutionManager.cheatEvolve()
            }
        })

        uiStage.addActor(moveJoystick)
        uiStage.addActor(aimJoystick)
        uiStage.addActor(fireButton)
        uiStage.addActor(hangarUI)
        uiStage.addActor(testEvolveButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        val player = world.family { all(PlayerComponent, EvolutionComponent, DockingComponent, ApertureComponent, ChoiceComponent) }.firstOrNull()

        if (player != null) {
            with(world) {
                val evo = player[EvolutionComponent]
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val choice = player[ChoiceComponent]

                val isTwinStickTier = evo.currentTier >= 4
                val isFullyDocked = docking.isDocked && aperture.state == ApertureComponent.State.OPEN
                val isUiRequested = isFullyDocked || choice.isAwaitingChoice

                moveJoystick.isVisible = !isUiRequested
                aimJoystick.isVisible = !isUiRequested && isTwinStickTier
                fireButton.isVisible = !isUiRequested && !isTwinStickTier
                testEvolveButton.isVisible = !isUiRequested

                if (isUiRequested) {
                    if (!wasDocked) {
                        if (!choice.isAwaitingChoice) hangarUI.resetToWelcome()
                        wasDocked = true
                    }
                    hangarUI.isVisible = true
                } else {
                    hangarUI.isVisible = false
                    wasDocked = false
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

        // Fix: Explicitly bound Hangar UI to ensure it never overflows vertically
        val hangarW = uiStage.width * 0.95f
        val hangarH = uiStage.height * 0.9f // Increased to 90% but capped
        hangarUI.setSize(hangarW, hangarH)
        hangarUI.setPosition(uiStage.width / 2f - hangarUI.width / 2f, uiStage.height / 2f - hangarUI.height / 2f)

        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 100f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
