package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import com.srw.seedsofsovereignty.ui.*
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * High-fidelity mission theater (v0.3.32).
 * Redirected to TVM (Tactical Virtual Machine) input kernel.
 * v0.4.0: Updated for Bidirectional Instrument DNA.
 */
class PlayScreen(
    private val world: World,
    private val batch: SpriteBatch,
    private val viewport: Viewport,
    private val uiViewport: Viewport,
    private val drawer: ShapeDrawer
) : KtxScreen {

    private val uiStage = Stage(uiViewport, batch)
    private val skin: Skin = HUDFactory.createSkin()

    // 1. Core Controls (Movement is still a dedicated primary joystick)
    private val moveJoystick: SovereigntyTouchpad = HUDInputService.createMoveJoystick(skin)

    // 2. Adaptive Tactical Overlay (TVM v0.3.32 - Unified Source of Truth)
    private var selectedModuleIndex = 0
    private var lastObservedIdentity: String? = null
    private var currentHUDStyle = "INDUSTRIAL"

    private val vitalsHUD = VitalsClusterWidget(drawer, skin, world)

    private val commandMatrix = CommandMatrixWidget(
        drawer,
        skin,
        world,
        { newIndex: Int -> selectedModuleIndex = newIndex }
    )

    private val testEvolveButton: TextButton = HUDInputService.createDebugButton("DEV: EVOLVE", skin)

    private val dockingManager = DockingManager(world)
    private val evolutionManager = EvolutionManager()
    private val marketManager = MarketManager(world)

    private val hangarUI = StationHangarUI(skin, drawer, world, dockingManager, marketManager)

    private var wasDocked = false
    private val tempCoords = Vector2()

    init {
        // Initialize Player Entity with TVM Action Hub
        world.entity {
            it += PlayerComponent()
            it += TacticalHubComponent() // The zero-allocation bidirectional hub
            it += TacticalPersistenceComponent()
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
                // TVM v0.4.0: Updated schema for bidirectional instruments
                tacticalSchema = TacticalSchema(listOf(InputChannel(ControlClassification.ACTION_BUTTON, "FIRE"))),
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

    override fun render(delta: Float) {
        val player = world.family { all(PlayerComponent, TacticalHubComponent, EvolutionComponent, DockingComponent, ApertureComponent, ChoiceComponent, FleetCommandComponent) }.firstOrNull()

        if (player != null) {
            val inputSystem = world.system<PlayerInputSystem>()
            with(world) {
                // 1. Update ATO Hubs
                vitalsHUD.update(player)

                // TVM Phase 7.2: Evolution-linked HUD Skin
                val evo = player[EvolutionComponent]
                if (evo.currentTier >= 10 && currentHUDStyle != "IMPERIAL") {
                    currentHUDStyle = "IMPERIAL"
                    HUDFactory.swapSkin("IMPERIAL", skin)
                    HUDFactory.refreshUI(uiStage, skin)
                    // Trigger a commandMatrix refresh
                    lastObservedIdentity = null
                }

                val slots = if (player.has(ModuleSlotComponent)) player[ModuleSlotComponent].slots else emptyList()
                commandMatrix.syncLoadout(slots, selectedModuleIndex)

                // 2. Fetch Schema of selected system
                if (selectedModuleIndex < slots.size) {
                    val slot = slots[selectedModuleIndex]
                    val slotEntity = slot.slotEntity
                    val currentIdentity = slotEntity?.toString() ?: slot.equippedName

                    // 3. Morph Tactical Surface only if identity changed
                    if (currentIdentity != lastObservedIdentity) {
                        var schema = TacticalSchema()
                        if (slotEntity != null && world.contains(slotEntity)) {
                            if (slotEntity.has(WeaponModuleComponent)) schema = slotEntity[WeaponModuleComponent].tacticalSchema
                            if (slotEntity.has(DroneModuleComponent)) schema = slotEntity[DroneModuleComponent].tacticalSchema
                        } else if (slot.type == SlotType.WEAPON && player.has(WeaponModuleComponent)) {
                            schema = player[WeaponModuleComponent].tacticalSchema
                        }
                        commandMatrix.updateTacticalSurface(schema, selectedModuleIndex)
                        lastObservedIdentity = currentIdentity
                    }
                }

                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val choice = player[ChoiceComponent]

                // Phase 9.4: Global "MODE" logic removed. Drone formation localization is now individualized.

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

                // 5. Movement Routing (Still primary locomotion)
                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)
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

        vitalsHUD.setPosition(30f, uiViewport.worldHeight - 100f)

        // v0.3.32: Compact Instrument Finalization - Correct Snap.
        // Origin is bottomRight, so (worldWidth - 40, 50) anchors the bottom-right corner of the widget.
        commandMatrix.setPosition(uiViewport.worldWidth - 40f, 50f, Align.bottomRight)  // LEAVE ALIGN.BOTTOMRIGHT. IT MUST BE HERE
        hangarUI.setSize(uiStage.width * 0.95f, uiStage.height * 0.9f)
        hangarUI.setPosition(uiStage.width / 2f - hangarUI.width / 2f, uiStage.height / 2f - hangarUI.height / 2f)
        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 100f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
