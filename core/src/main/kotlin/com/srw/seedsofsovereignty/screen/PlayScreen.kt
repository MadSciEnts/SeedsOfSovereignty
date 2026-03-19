package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.blueprint.BlueprintRegistry
import com.srw.seedsofsovereignty.component.FactionType
import com.srw.seedsofsovereignty.factory.ProjectileFactory
import com.srw.seedsofsovereignty.factory.ShipFactory
import com.srw.seedsofsovereignty.factory.UniversalFactory
import com.srw.seedsofsovereignty.service.TextureService
import com.srw.seedsofsovereignty.system.EvolutionSystem
import com.srw.seedsofsovereignty.ui.CommandMatrixView
import com.srw.seedsofsovereignty.world.GameContext
import com.srw.seedsofsovereignty.world.WorldModules.registerContext
import com.srw.seedsofsovereignty.world.WorldModules.registerSystems
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * PLAY SCREEN: The primary gameplay entry point.
 * Refactored to utilize the Universal DNA Factory.
 */
class PlayScreen(private val camera: OrthographicCamera) : KtxScreen {

    private val batch: Batch = SpriteBatch()
    private val textureService = TextureService()
    private val uiSkin: Skin = textureService.uiSkin

    private val gameViewport = ExtendViewport(16f, 9f, camera)
    private val uiViewport = ScreenViewport()
    private val uiStage = Stage(uiViewport, batch)

    private val drawer = ShapeDrawer(batch, textureService.whitePixel)
    private val movementJoystick = Touchpad(10f, uiSkin)
    private val devEvolveButton = TextButton("DEV EVOLVE", uiSkin)

    private val world: World
    private val shipFactory = ShipFactory() // Legacy - to be removed
    private val commandMatrix = CommandMatrixView(uiSkin)

    private val margin = 20f

    init {
        Gdx.input.inputProcessor = uiStage

        // 1. Build the Game Context
        val context = GameContext(
            batch = batch,
            drawer = drawer,
            textureService = textureService,
            gameViewport = gameViewport,
            uiViewport = uiViewport,
            camera = camera,
            joystick = movementJoystick,
            shipFactory = shipFactory,
            projectileFactory = ProjectileFactory(),
            commandMatrix = commandMatrix
        )

        // 2. Configure the World via Modules
        world = configureWorld {
            registerContext(context)
            registerSystems()
        }

        // 3. Wire the UI-ECS Bridge
        commandMatrix.attachWorld(world)

        // 4. Build Scene2D Layout
        uiStage.addActor(movementJoystick)
        uiStage.addActor(devEvolveButton)
        uiStage.addActor(commandMatrix.root)

        // 5. Global Event Handlers
        devEvolveButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                world.system<EvolutionSystem>().evolvePlayer()
            }
        })

        // 6. INITIAL SPAWN LOGIC: Using the Universal DNA Factory
        val playerDNA = BlueprintRegistry.getUnit(1)
        UniversalFactory.spawnUnit(world, playerDNA, FactionType.PLAYER, Vector2(0f, 0f))

        // Spawn some T1A test enemies
        val enemyDNA = BlueprintRegistry.getUnit(1)
        repeat(3) { i ->
            UniversalFactory.spawnUnit(world, enemyDNA, FactionType.ENEMY, Vector2(5f + i, 5f + i))
        }

        layoutUI()
    }

    private fun layoutUI() {
        val matrixWidth = movementJoystick.width * 1.5f
        val matrixHeight = 150f + movementJoystick.height
        commandMatrix.root.setSize(matrixWidth, matrixHeight)
        commandMatrix.root.setPosition(uiViewport.worldWidth - matrixWidth - margin, margin)
    }

    override fun render(delta: Float) {
        gameViewport.apply()
        batch.projectionMatrix = camera.combined
        batch.begin()
        world.update(delta)
        batch.end()

        uiViewport.apply()
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)

        val joystickSize = height / 3f
        movementJoystick.setSize(joystickSize, joystickSize)
        movementJoystick.setPosition(margin, margin)

        commandMatrix.joystickSize = joystickSize
        devEvolveButton.setPosition((uiViewport.worldWidth - devEvolveButton.width) / 2f, uiViewport.worldHeight - devEvolveButton.height - margin)

        layoutUI()
    }

    override fun dispose() {
        world.dispose()
        batch.dispose()
        textureService.dispose()
        uiStage.dispose()
    }
}
