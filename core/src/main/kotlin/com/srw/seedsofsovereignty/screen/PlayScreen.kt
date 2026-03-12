package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.*
import com.srw.seedsofsovereignty.system.DiegeticHUDSystem
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import com.srw.seedsofsovereignty.ui.*
import com.srw.seedsofsovereignty.util.ModuleGenerator
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * High-fidelity mission theater (v3.2.3).
 * ROLE: Active-Wake visual standard.
 *
 * FIXES:
 * 1. [WAKES] - Finalized organic wake standard (maxLength 48).
 * 2. [HUD] - Diagnostic Rings BASELINE v3.2.3.
 */
class PlayScreen(
    private val world: World,
    private val batch: SpriteBatch,
    private val shipViewport: Viewport,
    private val uiViewport: Viewport,
    private val drawer: ShapeDrawer
) : ktx.app.KtxScreen {

    private val uiStage = Stage(uiViewport, batch)
    private val skin: Skin = HUDFactory.createSkin()

    private val moveJoystick: SovereigntyTouchpad = HUDInputService.createMoveJoystick(skin)
    private val commandMatrix = CommandMatrixWidget(drawer, skin, world, { selectedModuleIndex = it })
    private val diagnosticWidget = DiagnosticWidget(drawer, skin, world)
    private val testEvolveButton: TextButton = HUDInputService.createDebugButton("DEV: EVOLVE", skin)
    private val testWarpButton: TextButton = HUDInputService.createDebugButton("DEV: WARP", skin)

    private val evolutionManager = world.inject<EvolutionManager>()
    private val sectorManager = world.inject<SectorManager>()
    private val dockingManager = DockingManager(world)
    private val marketManager = MarketManager(world)
    private val hangarUI = StationHangarUI(skin, drawer, world, dockingManager, marketManager)

    private var selectedModuleIndex = 0
    private var lastObservedIdentity: String? = null
    private var wasDocked = false

    init {
        val player = world.entity {
            it += PlayerComponent(); it += TacticalHubComponent(); it += TacticalLockComponent(); it += TacticalPersistenceComponent()
            it += PositionComponent(); it += OrientationComponent(); it += VelocityComponent(); it += WeaponComponent()
            it += SensorComponent(radius = 512f); it += EvolutionComponent(currentTier = 1, subTier = 1)

            it += TacticalStatsComponent(attackDistance = 50f, maxSpeed = 100f, rotationSpeed = 2.5f)

            // CALIBRATED PLAYER WAKE (v3.2.3): 48 segments, width = hullSize * 0.8, turbulence = 0.08f
            val hullSize = ProceduralShipDrawer.getShipSize(1, 1)
            it += TrailComponent(color = Color.CYAN.cpy(), maxLength = 48, width = hullSize * 0.8f, turbulenceIntensity = 0.08f)

            it += DockingComponent(); it += ApertureComponent(); it += ChoiceComponent()
            it += HealthComponent(); it += ShieldComponent()
            it += ModuleSlotComponent(mutableListOf(ModuleSlot(SlotType.WEAPON, 1, "Stock Kinetic", 1)))
            it += ModuleGenerator.generateWeapon(1, Rarity.COMMON)
        }

        HUDFactory.createPermanentHUDEntities(world, player)
        with(world) { HUDFactory.createInstrumentEntities(world, player, player[WeaponModuleComponent].tacticalSchema) }

        uiStage.addActor(moveJoystick)
        uiStage.addActor(commandMatrix)
        uiStage.addActor(diagnosticWidget)
        uiStage.addActor(hangarUI)
        uiStage.addActor(testEvolveButton)
        uiStage.addActor(testWarpButton)

        testEvolveButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) { evolutionManager.cheatEvolve(world) }
        })
        testWarpButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) { sectorManager.initiateWarp(world) }
        })

        uiStage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (event.target != uiStage.root) return false
                return world.system<DiegeticHUDSystem>().handleTouch(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            }
        })
        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val player = world.family { all(PlayerComponent, DockingComponent, ApertureComponent, ChoiceComponent) }.firstOrNull()
        if (player != null) {
            val inputSystem = world.system<PlayerInputSystem>()
            with(world) {
                val docking = player[DockingComponent]; val aperture = player[ApertureComponent]; val choice = player[ChoiceComponent]
                val isUiRequested = (docking.isDocked && aperture.state == ApertureComponent.State.OPEN) || choice.isAwaitingChoice
                hangarUI.isVisible = isUiRequested; moveJoystick.isVisible = !isUiRequested; commandMatrix.isVisible = !isUiRequested
                diagnosticWidget.isVisible = !isUiRequested
                testEvolveButton.isVisible = !isUiRequested; testWarpButton.isVisible = !isUiRequested

                if (isUiRequested && !wasDocked && !choice.isAwaitingChoice) { hangarUI.resetToWelcome(); wasDocked = true } else if (!isUiRequested) { wasDocked = false }

                val slots = player[ModuleSlotComponent].slots; commandMatrix.syncLoadout(slots, selectedModuleIndex)
                diagnosticWidget.update(player)

                if (selectedModuleIndex < slots.size) {
                    val currentId = slots[selectedModuleIndex].slotEntity?.toString() ?: slots[selectedModuleIndex].equippedName
                    if (currentId != lastObservedIdentity) {
                        HUDFactory.clearInstrumentEntities(world)
                        val schema = if (slots[selectedModuleIndex].slotEntity?.has(WeaponModuleComponent) == true) slots[selectedModuleIndex].slotEntity!![WeaponModuleComponent].tacticalSchema else player[WeaponModuleComponent].tacticalSchema
                        HUDFactory.createInstrumentEntities(world, player, schema); commandMatrix.updateTacticalSurface(schema, selectedModuleIndex); lastObservedIdentity = currentId
                    }
                }
                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)
            }
        }
        world.update(delta); uiStage.act(delta); uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        shipViewport.update(width, height, true); uiViewport.update(width, height, true)
        moveJoystick.setPosition(50f, 50f); commandMatrix.setPosition(uiViewport.worldWidth - 40f, 50f, Align.bottomRight)
        diagnosticWidget.setPosition(20f, uiViewport.worldHeight - 20f, Align.topLeft)
        hangarUI.setSize(uiViewport.worldWidth * 0.95f, uiViewport.worldHeight * 0.9f)
        hangarUI.setPosition(uiViewport.worldWidth / 2f - hangarUI.width / 2f, uiViewport.worldHeight / 2f - hangarUI.height / 2f)
        testEvolveButton.setPosition(uiViewport.worldWidth / 2f - testEvolveButton.width / 2f, uiViewport.worldHeight - 100f)
        testWarpButton.setPosition(uiViewport.worldWidth / 2f - testWarpButton.width / 2f, uiViewport.worldHeight - 180f)
    }

    override fun dispose() { uiStage.dispose(); skin.dispose() }
}
