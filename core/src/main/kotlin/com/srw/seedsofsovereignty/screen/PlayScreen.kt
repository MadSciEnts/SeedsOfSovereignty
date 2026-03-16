package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ProjectileFactory
import com.srw.seedsofsovereignty.factory.ShipFactory
import com.srw.seedsofsovereignty.service.TextureService
import com.srw.seedsofsovereignty.system.*
import com.srw.seedsofsovereignty.ui.CommandMatrixView
import ktx.app.KtxScreen
import space.earlygrey.shapedrawer.ShapeDrawer

class PlayScreen(private val camera: OrthographicCamera) : KtxScreen {

    private val batch: Batch = SpriteBatch()
    private val textureService = TextureService()
    private val uiSkin: Skin = textureService.uiSkin

    private val gameViewport = ExtendViewport(16f, 9f, camera)
    private val uiViewport = ScreenViewport()
    private val uiStage = Stage(uiViewport, batch)

    private val drawer = ShapeDrawer(batch, textureService.whitePixel)

    private val movementJoystick: Touchpad
    private val devEvolveButton: TextButton

    private val world: World
    private val shipFactory: ShipFactory
    private val commandMatrixView: CommandMatrixView

    private var lastCategories = setOf<ModuleCategory>()
    private var shelfUpdateTimer = 0f
    private val margin = 20f

    init {
        Gdx.input.inputProcessor = uiStage

        movementJoystick = Touchpad(10f, uiSkin)
        devEvolveButton = TextButton("DEV EVOLVE", uiSkin)

        world = configureWorld {
            injectables {
                add<Batch>(batch)
                add("batch", batch)
                add(drawer)
                add("drawer", drawer)
                add(textureService)
                add<Viewport>("gameViewport", gameViewport)
                add<Viewport>("uiViewport", uiViewport)
                add(camera)
                add("camera", camera)
                add("joystick", movementJoystick)
                add(ProjectileFactory())
            }

            systems {
                add(StarfieldRenderSystem())
                add(PlayerInputSystem())
                add(MoveSystem())
                add(CameraFollowSystem())
                add(ModuleAttachmentSystem())
                add(ModuleActionSystem())
                add(ProjectileSystem())
                add(PowerThrottlingSystem())
                add(RadarSensorSystem())
                add(EvolutionSystem())
                add(TrailSystem())
                add(TrailRenderSystem())
                add(WorldRenderSystem())
                add(TacticalRenderSystem())
                add(RadarRenderSystem())
            }
        }

        shipFactory = ShipFactory(world)
        commandMatrixView = CommandMatrixView(world, uiSkin)

        uiStage.addActor(movementJoystick)
        uiStage.addActor(devEvolveButton)
        uiStage.addActor(commandMatrixView.root)
        uiStage.addActor(commandMatrixView.categoryShelf)

        devEvolveButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                world.system<EvolutionSystem>().evolvePlayer()
                updateCategoryShelf(true)
            }
        })

        shipFactory.createPlayerTier1()
        updateCategoryShelf(true)
    }

    private fun updateCategoryShelf(force: Boolean = false) {
        val player = world.family { all(PlayerComponent) }.firstOrNull() ?: return
        val activeCategories = mutableSetOf<ModuleCategory>()

        with(world) {
            if (player.has(PowerCoreComponent)) activeCategories.add(ModuleCategory.POWER)
            if (player.has(ArcedDefenseComponent)) activeCategories.add(ModuleCategory.SHIELDS)

            family { all(ModuleComponent, AttachmentComponent) }.forEach { entity ->
                if (entity.get(AttachmentComponent).parent == player) {
                    activeCategories.add(entity.get(ModuleComponent).category)
                }
            }
        }

        if (!force && activeCategories == lastCategories) return
        lastCategories = activeCategories
        commandMatrixView.updateCategories(activeCategories)
        layoutCommandMatrix()
    }

    private fun layoutCommandMatrix() {
        commandMatrixView.categoryShelf.setPosition(uiViewport.worldWidth - commandMatrixView.categoryShelf.width - margin, margin)

        val matrixWidth = movementJoystick.width * 1.5f
        val matrixHeight = 80f + movementJoystick.height
        commandMatrixView.root.setSize(matrixWidth, matrixHeight)
        commandMatrixView.root.setPosition(uiViewport.worldWidth - matrixWidth - margin, margin + commandMatrixView.categoryShelf.height + 5f)
    }

    override fun render(delta: Float) {
        shelfUpdateTimer += delta
        if (shelfUpdateTimer >= 0.5f) {
            shelfUpdateTimer = 0f
            updateCategoryShelf()
        }

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

        commandMatrixView.joystickSize = joystickSize
        world.family { all(SelectedModuleComponent) }.firstOrNull()?.let {
            commandMatrixView.updateControlArea(it)
        }

        devEvolveButton.setPosition((uiViewport.worldWidth - devEvolveButton.width) / 2f, uiViewport.worldHeight - devEvolveButton.height - margin)

        layoutCommandMatrix()
    }

    override fun dispose() {
        world.dispose()
        batch.dispose()
        textureService.dispose()
        uiStage.dispose()
    }
}
