package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
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
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class PlayScreen(private val camera: OrthographicCamera) : KtxScreen {

    private val batch: Batch = SpriteBatch()
    private val textureService = TextureService()

    private val gameViewport = ExtendViewport(16f, 9f, camera)
    private val uiViewport = ScreenViewport()
    private val uiStage = Stage(uiViewport, batch)

    private val drawer = ShapeDrawer(batch, textureService.whitePixel)

    // Manual Skin creation to ensure stability
    private val uiSkin = Skin().apply {
        val pixmap = Pixmap(100, 100, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color(1f, 1f, 1f, 0.15f))
        pixmap.fillCircle(50, 50, 50)
        add("bg", Texture(pixmap))
        pixmap.setColor(Color(0.2f, 0.8f, 1f, 0.6f))
        pixmap.fillCircle(50, 50, 25)
        add("knob", Texture(pixmap))
        pixmap.dispose()
        add("default", Touchpad.TouchpadStyle(getDrawable("bg"), getDrawable("knob")))
    }

    private val movementJoystick = Touchpad(10f, uiSkin)

    private val world: World = configureWorld {
        injectables {
            // Register both by type AND name to be safe
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
            add(TrailSystem())
            add(TrailRenderSystem())
            add(WorldRenderSystem())
            add(TacticalRenderSystem())
            add(RadarRenderSystem())
        }
    }

    init {
        Gdx.input.inputProcessor = uiStage
        movementJoystick.setSize(150f, 150f)
        uiStage.addActor(movementJoystick)

        // Set camera to very zoomed out scale
        camera.zoom = 10f

        val ship = world.entity { entity ->
            entity += PlayerComponent()
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
        movementJoystick.setPosition(40f, 40f)
        gameViewport.apply()
        batch.projectionMatrix = camera.combined
        batch.use {
            world.update(delta)
        }
        uiViewport.apply()
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
    }

    override fun dispose() {
        world.dispose()
        batch.dispose()
        textureService.dispose()
        uiStage.dispose()
        uiSkin.dispose()
    }
}
