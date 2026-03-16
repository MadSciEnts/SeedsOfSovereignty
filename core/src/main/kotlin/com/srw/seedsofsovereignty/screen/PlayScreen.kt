package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
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
    private var lastCategories = setOf<ModuleCategory>()
    private var shelfUpdateTimer = 0f

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

        commandMatrix = Table().apply {
            add(selectionShelf).expandX().fillX().height(60f).row()
            add(controlArea).expand().fill()
        }

        categoryShelf = Table(uiSkin).apply {
            background = uiSkin.getDrawable("panel_bg")
        }

        uiStage.addActor(movementJoystick)
        uiStage.addActor(devEvolveButton)
        uiStage.addActor(commandMatrix)
        uiStage.addActor(categoryShelf)

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

        devEvolveButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                world.system<EvolutionSystem>().evolvePlayer()
                updateCategoryShelf(true)
            }
        })

        setupInitialEntities()
        updateCategoryShelf(true)
    }

    private fun updateCategoryShelf(force: Boolean = false) {
        val player = world.family { all(PlayerComponent) }.firstOrNull() ?: return

        val activeCategories = mutableSetOf<ModuleCategory>()

        // Using world.run to access Entity extension functions (get/has)
        world.run {
            if (player has PowerCoreComponent) activeCategories.add(ModuleCategory.POWER)
            if (player has ArcedDefenseComponent) activeCategories.add(ModuleCategory.SHIELDS)

            world.family { all(ModuleComponent, AttachmentComponent) }.forEach { module ->
                if (module.get(AttachmentComponent).parent == player) {
                    activeCategories.add(module.get(ModuleComponent).category)
                }
            }
        }

        if (!force && activeCategories == lastCategories) return
        lastCategories = activeCategories

        categoryShelf.clearChildren()
        ModuleCategory.entries.forEach { cat ->
            if (cat in activeCategories) {
                val btn = TextButton(cat.name, uiSkin, cat.name.lowercase())
                categoryShelf.add(btn).size(180f, 80f).pad(2f)

                btn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        commandMatrix.color = getCategoryColor(cat).cpy().apply { a = 0.8f }
                    }
                })
            }
        }
        categoryShelf.pack()
    }

    private fun getCategoryColor(category: ModuleCategory): Color {
        return when (category) {
            ModuleCategory.PRIMARY -> Color.RED
            ModuleCategory.SECONDARY -> Color.ORANGE
            ModuleCategory.TERTIARY -> Color.YELLOW
            ModuleCategory.QUADRARY -> Color.PURPLE
            ModuleCategory.SHIELDS -> Color.BLUE
            ModuleCategory.POWER -> Color.GREEN
            ModuleCategory.DRONES -> Color.CYAN
            ModuleCategory.FIGHTERS -> Color.WHITE
        }
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

        shelfUpdateTimer += delta
        if (shelfUpdateTimer >= 0.5f) {
            shelfUpdateTimer = 0f
            updateCategoryShelf()
        }

        movementJoystick.setPosition(margin, margin)
        devEvolveButton.setPosition((uiViewport.worldWidth - devEvolveButton.width) / 2f, uiViewport.worldHeight - devEvolveButton.height - margin)

        categoryShelf.setPosition(uiViewport.worldWidth - categoryShelf.width - margin, margin)

        val matrixWidth = movementJoystick.width * 1.5f
        val matrixHeight = 60f + movementJoystick.height
        commandMatrix.setSize(matrixWidth, matrixHeight)
        commandMatrix.setPosition(uiViewport.worldWidth - matrixWidth - margin, margin + categoryShelf.height + 5f)

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
