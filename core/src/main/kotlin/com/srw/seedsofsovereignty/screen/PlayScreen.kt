package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import com.srw.seedsofsovereignty.ui.*
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

    // 1. Core Controls
    private val moveJoystick: Touchpad = HUDFactory.createMoveJoystick(skin)
    private val aimJoystick: SovereigntyTouchpad = HUDFactory.createAimJoystick(skin) as SovereigntyTouchpad
    private val fireButton: TextButton = HUDFactory.createFireButton(skin)
    private val modeToggle: TextButton = HUDFactory.createModeToggle(skin)
    private val localizeButton: TextButton = HUDFactory.createDebugButton("LOC: CAM", skin).apply { setSize(120f, 60f) }

    // 2. Technical Loadout HUD (v0.1.10)
    private val loadoutHUD = TechnicalLoadoutWidget(drawer, skin, world)

    private val testEvolveButton: TextButton = HUDFactory.createDebugButton("DEV: EVOLVE", skin)

    private val dockingManager = DockingManager(world)
    private val evolutionManager = EvolutionManager()
    private val marketManager = MarketManager(world)

    private val hangarUI = StationHangarUI(skin, drawer, world, dockingManager, marketManager)

    private var wasDocked = false
    private val tempCoords = Vector2()

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
                description = "Standard kinetic stream.",
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
            it += FleetCommandComponent()
            it += HealthComponent(current = 100f, max = 100f)
            it += ShieldComponent(current = 50f, max = 50f)
            it += ModuleSlotComponent(mutableListOf(ModuleSlot(SlotType.WEAPON, 1, "Stock Kinetic", 1)))
        }

        // INITIAL TEST STATION
        world.entity {
            it += PositionComponent().apply { pos.set(500f, 500f) }
            it += StationComponent(name = "Alpha Outpost", radius = 200f)
            it += MarketComponent().apply {
                for (i in 0 until 15) weaponOffers.add(ModuleGenerator.generateWeapon(1))
                for (i in 0 until 5) droneOffers.add(ModuleGenerator.generateDroneModule(1))
            }
            it += VisibleComponent()
        }

        modeToggle.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                world.family { all(PlayerComponent, FleetCommandComponent) }.firstOrNull()?.let { p ->
                    with(world) {
                        val fleet = p[FleetCommandComponent]
                        if (fleet.controlMode == FleetCommandComponent.ControlMode.ATTACK_DIRECTION) {
                            fleet.lastWeaponAim.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
                            fleet.controlMode = FleetCommandComponent.ControlMode.DRONE_FOCUS
                            aimJoystick.isPersistent = true
                            aimJoystick.updateStyle("drone-mode", skin)
                            aimJoystick.setKnobPosition(fleet.lastDroneAim.x, fleet.lastDroneAim.y)
                            modeToggle.setText("DRN")
                        } else {
                            fleet.lastDroneAim.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
                            fleet.controlMode = FleetCommandComponent.ControlMode.ATTACK_DIRECTION
                            aimJoystick.isPersistent = false
                            aimJoystick.updateStyle("aim", skin)
                            aimJoystick.setKnobPosition(fleet.lastWeaponAim.x, fleet.lastWeaponAim.y)
                            modeToggle.setText("WPN")
                        }
                    }
                }
            }
        })

        localizeButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                world.family { all(PlayerComponent, FleetCommandComponent) }.firstOrNull()?.let { p ->
                    with(world) {
                        val fleet = p[FleetCommandComponent]
                        fleet.localization = if (fleet.localization == FleetCommandComponent.LocalizationMode.CAMERA)
                            FleetCommandComponent.LocalizationMode.SHIP else FleetCommandComponent.LocalizationMode.CAMERA
                        localizeButton.setText(if (fleet.localization == FleetCommandComponent.LocalizationMode.CAMERA) "LOC: CAM" else "LOC: SHIP")
                    }
                }
            }
        })

        testEvolveButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                evolutionManager.cheatEvolve(world)
            }
        })

        uiStage.addActor(moveJoystick)
        uiStage.addActor(aimJoystick)
        uiStage.addActor(fireButton)
        uiStage.addActor(modeToggle)
        uiStage.addActor(localizeButton)
        uiStage.addActor(loadoutHUD)
        uiStage.addActor(hangarUI)
        uiStage.addActor(testEvolveButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        val player = world.family { all(PlayerComponent, EvolutionComponent, DockingComponent, ApertureComponent, ChoiceComponent, FleetCommandComponent) }.firstOrNull()

        if (player != null) {
            with(world) {
                val evo = player[EvolutionComponent]
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val choice = player[ChoiceComponent]
                val fleet = player[FleetCommandComponent]

                loadoutHUD.update(player)

                if (player.has(EvolutionCelebrationComponent)) {
                    val celeb = player[EvolutionCelebrationComponent]
                    if (celeb.phase == EvolutionCelebrationComponent.Phase.FLASH && celeb.timer > 0.1f) {
                        triggerFlyingIcon(player, celeb)
                        celeb.phase = EvolutionCelebrationComponent.Phase.FLYING_ICON
                    }
                }

                val isTwinStickTier = evo.currentTier >= 4
                val hasDrones = player.has(DroneLauncherComponent) ||
                               (player.has(ModuleSlotComponent) && player[ModuleSlotComponent].slots.any { it.type == SlotType.DRONE })

                val isFullyDocked = docking.isDocked && aperture.state == ApertureComponent.State.OPEN
                val isUiRequested = isFullyDocked || choice.isAwaitingChoice

                moveJoystick.isVisible = !isUiRequested
                aimJoystick.isVisible = !isUiRequested && isTwinStickTier
                fireButton.isVisible = !isUiRequested && !isTwinStickTier
                testEvolveButton.isVisible = !isUiRequested
                modeToggle.isVisible = !isUiRequested && isTwinStickTier && hasDrones
                localizeButton.isVisible = modeToggle.isVisible && fleet.controlMode == FleetCommandComponent.ControlMode.DRONE_FOCUS
                loadoutHUD.isVisible = !isUiRequested

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
                    inputSystem.isFiringRequested = !inputSystem.aimVector.isZero && fleet.controlMode == FleetCommandComponent.ControlMode.ATTACK_DIRECTION
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

    private fun triggerFlyingIcon(player: Entity, celeb: EvolutionCelebrationComponent) {
        val dna = celeb.newWeaponDNA ?: return

        // 1. Create the Celebratory Icon
        val flyingIcon = ProjectileIcon(drawer, skin).apply {
            tier = dna.projectileTier
            projectileColor = dna.projectileColor
            iconScale = 4.0f
            setSize(250f, 250f)
            setOrigin(Align.center)
            setPosition(uiViewport.worldWidth / 2f, uiViewport.worldHeight / 2f, Align.center)
        }

        // 2. Add Congratulations Label
        val congratsLabel = HUDFactory.createLabel("EVOLUTION COMPLETE: TIER ${celeb.newTier}", skin, "title").apply {
            setAlignment(Align.center)
            setPosition(uiViewport.worldWidth / 2f, uiViewport.worldHeight - 150f, Align.center)
            color.a = 0f
        }

        // 3. Coordinate Mapping (Target the new HUD primitive)
        loadoutHUD.validate()
        val targetSlot = loadoutHUD.getSlot(0) ?: return
        val targetIcon = targetSlot.getIconCenter()

        tempCoords.set(targetIcon.width / 2f, targetIcon.height / 2f)
        targetIcon.localToStageCoordinates(tempCoords)
        val targetX = tempCoords.x
        val targetY = tempCoords.y

        // 4. Tween Logic
        congratsLabel.addAction(Actions.sequence(
            Actions.fadeIn(0.3f),
            Actions.delay(1.5f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ))

        flyingIcon.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.parallel(
                Actions.moveToAligned(targetX, targetY, Align.center, 1.2f, com.badlogic.gdx.math.Interpolation.pow3Out),
                Actions.sizeTo(targetIcon.width, targetIcon.height, 1.2f, com.badlogic.gdx.math.Interpolation.pow3Out),
                object : Action() {
                    override fun act(delta: Float): Boolean {
                        val icon = actor as ProjectileIcon
                        val progress = (actor.width - 250f) / (targetIcon.width - 250f)
                        icon.iconScale = 4.0f + (1.8f - 4.0f) * progress.coerceIn(0f, 1f)
                        return true
                    }
                },
                Actions.rotateBy(720f, 1.2f)
            ),
            Actions.run {
                celeb.phase = EvolutionCelebrationComponent.Phase.COMPLETED
            },
            Actions.removeActor()
        ))

        uiStage.addActor(congratsLabel)
        uiStage.addActor(flyingIcon)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)

        moveJoystick.setPosition(50f, 50f)
        aimJoystick.setPosition(uiStage.width - aimJoystick.width - 50f, 50f)
        fireButton.setPosition(uiStage.width - fireButton.width - 50f, 50f)
        modeToggle.setPosition(uiStage.width - modeToggle.width - 50f, aimJoystick.y + aimJoystick.height + 20f)
        localizeButton.setPosition(aimJoystick.x - localizeButton.width - 20f, aimJoystick.y + (aimJoystick.height / 2f) - (localizeButton.height / 2f))

        // Final positioning for the new Loadout HUD
        loadoutHUD.setPosition(30f, uiViewport.worldHeight - 140f)

        hangarUI.setSize(uiStage.width * 0.95f, uiStage.height * 0.9f)
        hangarUI.setPosition(uiStage.width / 2f - hangarUI.width / 2f, uiStage.height / 2f - hangarUI.height / 2f)
        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 100f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
