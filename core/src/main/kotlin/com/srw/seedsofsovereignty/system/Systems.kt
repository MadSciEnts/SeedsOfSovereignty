import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.AllOf

// 1. INPUT SYSTEM: Translates keys to intention
@AllOf(entities = [PlayerComponent::class, VelocityComponent::class])
class PlayerInputSystem : IteratingSystem() {
    override fun onEntityTick(entity: Entity) {
        val vel = entity[VelocityComponent]
        val moveForce = Vector2(0f, 0f)

        if (Gdx.input.isKeyPressed(Keys.W)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D)) moveForce.x += 1f

        // Normalize so diagonal movement isn't faster
        if (moveForce.len() > 0) {
            moveForce.nor().scl(vel.acceleration * deltaTime)
            vel.v.add(moveForce)
        }
    }
}

// 2. MOVE SYSTEM: Applies physics and drag
@AllOf(entities = [PositionComponent::class, VelocityComponent::class])
class MoveSystem : IteratingSystem() {
    override fun onEntityTick(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        // Apply velocity to position
        pos.pos.x += vel.v.x * deltaTime
        pos.pos.y += vel.v.y * deltaTime

        // Apply friction/drag so ship eventually stops
        vel.v.scl(1f - (vel.friction * deltaTime))
    }
}