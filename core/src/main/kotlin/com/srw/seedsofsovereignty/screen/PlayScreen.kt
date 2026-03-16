package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.TextureService
import com.srw.seedsofsovereignty.system.*
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
    private val commandMatrix: Table
    private val selectionShelf: Table
    private val controlArea: Table
    private val categoryShelf: Table

    private val world: World

    init {
        Gdx.input.inputProcessor = uiStage

        movementJoystick = Touchpad(10f, uiSkin)
        devEvolveButton = TextButton("DEV EVOLVE", uiSkin)

        selectionShelf = Table(uiSkin).apply {
            background = uiSkin.getDrawable("panel_bg")
            add(TextButton("MOD 1", uiSkin)).expandX().fillX().pad(4f)
        }

        controlArea = Table(uiSkin).apply {
            background = uiSkin.getDrawable("panel_bg")
            add(TextButton("MATRIX CONTROLS", uiSkin)).expand().fill().pad(10f)
        }

        categoryShelf = Table(uiSkin).apply {
            background = uiSkin.getDrawable("panel_bg")
            ModuleCategory.entries.forEach { cat ->
                val btn = TextButton(cat.name.take(1), uiSkin)
                add(btn).expandX().fillX().pad(2f)
            }
        }

        commandMatrix = Table().apply {
            add(selectionShelf).expandX().fillX().height(60f).row()
            add(controlArea).expand().fill().row()
            add(categoryShelf).expandX().fillX().height(60f)
        }

        uiStage.addActor(movementJoystick)
        uiStage.addActor(devEvolveButton)
        uiStage.addActor(commandMatrix)

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
            }

            systems {
                add(StarfieldRenderSystem())
                add(PlayerInputSystem())
                add(MoveSystem())
                add(CameraFollowSystem())
                add(ModuleAttachmentSystem())
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

        devEvolveButton.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            override fun changed(event: com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                world.system<EvolutionSystem>().evolvePlayer()
            }
        })

        setupInitialEntities()
    }

    private fun setupInitialEntities() {
        camera.zoom = 10f
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += EvolutionComponent()
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent(acceleration = 15f, friction = 1.5f)
            entity += PowerCoreComponent()
            entity += ArcedDefenseComponent()
            entity += RadarComponent()
            entity += ShapeComponent(type = ShapeType.TRIANGLE, color = Color.CYAN, size = 1f)
            entity += TrailComponent()
        }

        world.entity { module ->
            module += PositionComponent()
            module += AttachmentComponent(parent = ship, offset = Vector2(0f, 0.6f))
            module += ModuleComponent(category = ModuleCategory.PRIMARY)
            module += WeaponModuleComponent(evolution = SubTier.A, barrelOffsets = mutableListOf(Vector2(0f, 0f)))
            module += BarrelVisualComponent()
        }
    }

    override fun render(delta: Float) {
        val margin = 20f
        movementJoystick.setPosition(margin, margin)
        devEvolveButton.setPosition((uiViewport.worldWidth - devEvolveButton.width) / 2f, uiViewport.worldHeight - devEvolveButton.height - margin)

        val matrixWidth = movementJoystick.width * 1.5f
        val matrixHeight = movementJoystick.height + 120f
        commandMatrix.setSize(matrixWidth, matrixHeight)
        commandMatrix.setPosition(uiViewport.worldWidth - matrixWidth - margin, margin)

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
    }

    override fun dispose() {
        world.dispose()
        batch.dispose()
        textureService.dispose()
        uiStage.dispose()
    }
}
