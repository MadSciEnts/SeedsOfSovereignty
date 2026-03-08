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
    private val moveJoystick: SovereigntyTouchpad = HUDFactory.createMoveJoystick(skin)
    private val aimJoystick: SovereigntyTouchpad = HUDFactory.createAimJoystick(skin)
    private val droneJoystick: SovereigntyTouchpad = HUDFactory.createDroneJoystick(skin).apply {
        isPersistent = true
    }
    private val fireButton: TextButton = HUDFactory.createFireButton(skin)
    private val modeToggle: TextButton = HUDFactory.createModeToggle(skin)
    private val localizeButton: TextButton = HUDFactory.createDebugButton("LOC: CAM", skin).apply { setSize(120f, 60f) }

    // 2. Adaptive Tactical Overlay (ATO v0.2.1 - Single Source of Truth)
    private var selectedModuleIndex = 0
    private var lastObservedControlType: ControlType? = null

    private val vitalsHUD = VitalsClusterWidget(drawer, skin, world)

    // Task Fix: Pass both selection and localization callbacks to matrix
    private val commandMatrix = CommandMatrixWidget(
        drawer,
        skin,
        world,
        { newIndex: Int -> selectedModuleIndex = newIndex },
        { toggleDroneLocalization() }
    )

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
                rarity = Rarity.COMMON,
                controlType = ControlType.BUTTON,
                projectileSpeed = 1000f,
                fireRate = 0.6f,
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

        // ATO Root Integration
        uiStage.addActor(moveJoystick)
        uiStage.addActor(vitalsHUD)
        uiStage.addActor(commandMatrix)
        uiStage.addActor(hangarUI)
        uiStage.addActor(testEvolveButton)

        testEvolveButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                evolutionManager.cheatEvolve(world)
            }
        })

        Gdx.input.inputProcessor = uiStage
    }

    private fun toggleDroneLocalization() {
        val player = world.family { all(PlayerComponent, FleetCommandComponent) }.firstOrNull() ?: return
        with(world) {
            val fleet = player[FleetCommandComponent]
            fleet.localization = if (fleet.localization == FleetCommandComponent.LocalizationMode.CAMERA)
                FleetCommandComponent.LocalizationMode.SHIP else FleetCommandComponent.LocalizationMode.CAMERA
            commandMatrix.updateSpaceLabel(fleet.localization == FleetCommandComponent.LocalizationMode.CAMERA)
        }
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        val player = world.family { all(PlayerComponent, EvolutionComponent, DockingComponent, ApertureComponent, ChoiceComponent, FleetCommandComponent) }.firstOrNull()

        if (player != null) {
            with(world) {
                // 1. Update ATO Hubs
                vitalsHUD.update(player)

                val slots = if (player.has(ModuleSlotComponent)) player[ModuleSlotComponent].slots else emptyList()
                commandMatrix.syncLoadout(slots, selectedModuleIndex)

                // 2. Fetch Control DNA of selected system
                var currentControlType = ControlType.BUTTON
                var targetDroneSlotIndex = -1

                if (selectedModuleIndex < slots.size) {
                    val slot = slots[selectedModuleIndex]
                    val slotEntity = slot.slotEntity
                    if (slotEntity != null && world.contains(slotEntity)) {
                        if (slotEntity.has(WeaponModuleComponent)) currentControlType = slotEntity[WeaponModuleComponent].controlType
                        if (slotEntity.has(DroneModuleComponent)) {
                            currentControlType = ControlType.TACTICAL_FOCUS_STICK
                            if (slotEntity.has(DroneLauncherComponent)) targetDroneSlotIndex = slotEntity[DroneLauncherComponent].slotIndex
                        }
                    } else if (slot.type == SlotType.WEAPON && player.has(WeaponModuleComponent)) {
                        currentControlType = player[WeaponModuleComponent].controlType
                    }
                }

                // 3. Morph Input Proxy
                if (currentControlType != lastObservedControlType) {
                    commandMatrix.updateInputDisplay(currentControlType, aimJoystick, droneJoystick, fireButton)

                    // Restore position for tactical sticks
                    if (currentControlType == ControlType.TACTICAL_FOCUS_STICK && targetDroneSlotIndex != -1) {
                        val fleet = player[FleetCommandComponent]
                        val focus = fleet.slotFocusPoints[targetDroneSlotIndex]
                        droneJoystick.setKnobPosition(focus.x / 250f, focus.y / 250f)
                        commandMatrix.updateSpaceLabel(fleet.localization == FleetCommandComponent.LocalizationMode.CAMERA)
                    }

                    lastObservedControlType = currentControlType
                }

                val evo = player[EvolutionComponent]
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val choice = player[ChoiceComponent]
                val fleet = player[FleetCommandComponent]

                val isFullyDocked = docking.isDocked && aperture.state == ApertureComponent.State.OPEN
                val isUiRequested = isFullyDocked || choice.isAwaitingChoice

                moveJoystick.isVisible = !isUiRequested
                vitalsHUD.isVisible = !isUiRequested
                commandMatrix.isVisible = !isUiRequested
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

                // 4. Tactical Input Routing
                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)

                when (currentControlType) {
                    ControlType.DIRECTIONAL_STICK -> {
                        inputSystem.aimVector.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
                        inputSystem.isFiringRequested = !inputSystem.aimVector.isZero
                    }
                    ControlType.TACTICAL_FOCUS_STICK -> {
                        if (targetDroneSlotIndex != -1) {
                            val range = 250f
                            fleet.slotFocusPoints[targetDroneSlotIndex].set(
                                droneJoystick.knobPercentX * range,
                                droneJoystick.knobPercentY * range
                            )
                        }
                        fleet.lastDroneAim.set(droneJoystick.knobPercentX, droneJoystick.knobPercentY)
                        inputSystem.aimVector.set(0f, 0f)
                        inputSystem.isFiringRequested = false
                    }
                    else -> {
                        inputSystem.aimVector.set(0f, 0f)
                        inputSystem.isFiringRequested = fireButton.isPressed
                    }
                }
            }
        }

        world.update(delta)
        uiStage.act(delta)
        uiStage.draw()
    }

    private fun triggerFlyingIcon(player: Entity, celeb: EvolutionCelebrationComponent) {
        val dna = celeb.newWeaponDNA ?: return

        val flyingIcon = ProjectileIcon(drawer, skin).apply {
            tier = dna.projectileTier
            projectileColor = dna.projectileColor
            iconScale = 4.0f
            setSize(250f, 250f)
            setOrigin(Align.center)
            setPosition(uiViewport.worldWidth / 2f, uiViewport.worldHeight / 2f, Align.center)
        }

        val congratsLabel = HUDFactory.createLabel("EVOLUTION COMPLETE: TIER ${celeb.newTier}", skin, "title").apply {
            setAlignment(Align.center)
            setPosition(uiViewport.worldWidth / 2f, uiViewport.worldHeight - 150f, Align.center)
            color.a = 0f
        }

        commandMatrix.validate()
        val targetSlot = commandMatrix.getSlotButton(0)

        if (targetSlot != null) {
            tempCoords.set(targetSlot.width / 2f, targetSlot.height / 2f)
            targetSlot.localToStageCoordinates(tempCoords)
            val targetX = tempCoords.x
            val targetY = tempCoords.y

            flyingIcon.addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.parallel(
                    Actions.moveToAligned(targetX, targetY, Align.center, 1.2f, com.badlogic.gdx.math.Interpolation.pow3Out),
                    Actions.sizeTo(targetSlot.width, targetSlot.height, 1.2f, com.badlogic.gdx.math.Interpolation.pow3Out),
                    object : Action() {
                        override fun act(delta: Float): Boolean {
                            val icon = actor as ProjectileIcon
                            val progress = (actor.width - 250f) / (targetSlot.width - 250f)
                            icon.iconScale = 4.0f + (1.5f - 4.0f) * progress.coerceIn(0f, 1f)
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
        }

        congratsLabel.addAction(Actions.sequence(
            Actions.fadeIn(0.3f),
            Actions.delay(1.5f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ))

        uiStage.addActor(congratsLabel)
        uiStage.addActor(flyingIcon)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)

        moveJoystick.setPosition(50f, 50f)

        // Adaptive Tactical Overlay Root Anchors
        vitalsHUD.setPosition(30f, uiViewport.worldHeight - 100f)
        commandMatrix.setPosition(uiViewport.worldWidth - 300f, 50f)

        hangarUI.setSize(uiStage.width * 0.95f, uiStage.height * 0.9f)
        hangarUI.setPosition(uiStage.width / 2f - hangarUI.width / 2f, uiStage.height / 2f - hangarUI.height / 2f)
        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 100f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
