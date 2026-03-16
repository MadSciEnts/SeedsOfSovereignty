package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.system.*
import ktx.app.KtxScreen

class PlayScreen(private val camera: OrthographicCamera) : KtxScreen {

    // Viewport for the actual game world (units)
    private val gameViewport = ExtendViewport(16f, 9f, camera)

    // Viewport for the HUD/UI (pixels)
    private val uiViewport = ScreenViewport()

    // Initialize Fleks World with Dependency Injection
    private val world: World = configureWorld {
        injectables {
            add(camera)
            add(gameViewport)
        }

        systems {
            add(PlayerInputSystem())
            add(MoveSystem())
            add(CameraFollowSystem())
            add(ModuleAttachmentSystem())
            add(PowerThrottlingSystem())
        }
    }

    init {
        // Create our Tier 1 Ship Entity (The Power Core / Hull)
        val ship = world.entity { entity ->
            entity += PlayerComponent()
            entity += PositionComponent(Vector2(0f, 0f))
            entity += VelocityComponent()
            entity += PowerCoreComponent()
            entity += ArcedDefenseComponent()
        }

        // Example: Adding a Primary Weapon Module
        world.entity { module ->
            module += PositionComponent()
            module += AttachmentComponent(parent = ship, offset = Vector2(0.5f, 0.5f))
            module += ModuleComponent(category = ModuleCategory.PRIMARY)
        }
    }

    override fun render(delta: Float) {
        // 1. Update ECS World (Logic, Physics, Attachments, Throttling)
        world.update(delta)

        // 2. Render Game World
        gameViewport.apply()
        camera.update()
        // batch.use(camera) { ... render entities ... }

        // 3. Render HUD/UI
        uiViewport.apply()
        // stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
    }

    override fun dispose() {
        world.dispose()
    }
}
