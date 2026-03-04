import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.graphics.use

class PlayScreen(private val camera: OrthographicCamera) : KtxScreen {
    
    // Initialize Fleks World with our systems
    private val world = world {
        systems {
            add(PlayerInputSystem())
            add(MoveSystem())
            // Add CameraSystem and RenderSystem here later
        }
    }

    init {
        // Create our Tier 1 Ship Entity
        world.entity {
            add<PlayerComponent>()
            add<PositionComponent> { pos.set(0f, 0f) }
            add<VelocityComponent>()
        }
    }

    override fun render(delta: Float) {
        // Update physics and logic
        world.update(delta)
        
        // Update camera
        camera.update()
        
        // Render logic will go here
    }

    override fun dispose() {
        world.dispose()
    }
}