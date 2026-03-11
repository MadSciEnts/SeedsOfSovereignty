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
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * High-fidelity mission theater (v2.5.4).
 * ROLE: The "Clean Slate" play arena utilizing Partitioned Viewports.
 */
class PlayScreen(
    private val world: World,
    private val batch: SpriteBatch,
    private val shipViewport: Viewport, // v2.5.4: Renamed from viewport for parity
    private val uiViewport: Viewport,
    private val drawer: ShapeDrawer
) : ktx.app.KtxScreen {

    private val uiStage = Stage(uiViewport, batch)
    private val skin: Skin = HUDFactory.createSkin()

    private val moveJoystick: SovereigntyTouchpad = HUDInputService.createMoveJoystick(skin)
    private val commandMatrix = CommandMatrixWidget(drawer, skin, world, { selectedModuleIndex = it })
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
            it += SensorComponent(radius = 1000f); it += EvolutionComponent(currentTier = 1, subTier = 1)
            it += DockingComponent(); it += ApertureComponent(); it += ChoiceComponent()
            it += HealthComponent(); it += ShieldComponent()
            it += ModuleSlotComponent(mutableListOf(ModuleSlot(SlotType.WEAPON, 1, "Stock Kinetic", 1)))
            it += ModuleGenerator.generateWeapon(1, Rarity.COMMON)
        }

        HUDFactory.createPermanentHUDEntities(world, player)
        with(world) {
            HUDFactory.createInstrumentEntities(world, player, player[WeaponModuleComponent].tacticalSchema)
        }

        uiStage.addActor(moveJoystick); uiStage.addActor(commandMatrix); uiStage.addActor(hangarUI); uiStage.addActor(testEvolveButton); uiStage.addActor(testWarpButton)

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
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val choice = player[ChoiceComponent]
                val isFullyDocked = docking.isDocked && aperture.state == ApertureComponent.State.OPEN
                val isUiRequested = isFullyDocked || choice.isAwaitingChoice

                hangarUI.isVisible = isUiRequested
                moveJoystick.isVisible = !isUiRequested
                commandMatrix.isVisible = !isUiRequested
                testEvolveButton.isVisible = !isUiRequested
                testWarpButton.isVisible = !isUiRequested

                if (isUiRequested && !wasDocked && !choice.isAwaitingChoice) {
                    hangarUI.resetToWelcome()
                    wasDocked = true
                } else if (!isUiRequested) { wasDocked = false }

                val slots = player[ModuleSlotComponent].slots
                commandMatrix.syncLoadout(slots, selectedModuleIndex)
                if (selectedModuleIndex < slots.size) {
                    val slot = slots[selectedModuleIndex]
                    val slotEntity = slot.slotEntity
                    val currentId = slotEntity?.toString() ?: slot.equippedName
                    if (currentId != lastObservedIdentity) {
                        HUDFactory.clearInstrumentEntities(world)
                        val schema = if (slotEntity != null && world.contains(slotEntity)) {
                            if (slotEntity.has(WeaponModuleComponent)) slotEntity[WeaponModuleComponent].tacticalSchema
                            else if (slotEntity.has(DroneModuleComponent)) slotEntity[DroneModuleComponent].tacticalSchema
                            else TacticalSchema()
                        } else player[WeaponModuleComponent].tacticalSchema
                        HUDFactory.createInstrumentEntities(world, player, schema)
                        commandMatrix.updateTacticalSurface(schema, selectedModuleIndex)
                        lastObservedIdentity = currentId
                    }
                }
                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)
            }
        }

        world.update(delta)
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        // FIX: Synchronized viewport update to restore circular hulls
        shipViewport.update(width, height, true)
        uiViewport.update(width, height, true)

        moveJoystick.setPosition(50f, 50f)
        commandMatrix.setPosition(uiViewport.worldWidth - 40f, 50f, Align.bottomRight)
        hangarUI.setSize(uiViewport.worldWidth * 0.95f, uiViewport.worldHeight * 0.9f)
        hangarUI.setPosition(uiViewport.worldWidth / 2f - hangarUI.width / 2f, uiViewport.worldHeight / 2f - hangarUI.height / 2f)
        testEvolveButton.setPosition(uiViewport.worldWidth / 2f - testEvolveButton.width / 2f, uiViewport.worldHeight - 100f)
        testWarpButton.setPosition(uiViewport.worldWidth / 2f - testWarpButton.width / 2f, uiViewport.worldHeight - 180f)
    }

    override fun dispose() { uiStage.dispose(); skin.dispose() }
}
