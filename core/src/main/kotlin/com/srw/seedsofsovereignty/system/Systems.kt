package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.VelocityComponent
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class ClearScreenSystem : IntervalSystem() {
    override fun onTick() {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f)
    }
}

class StarfieldSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera,
    private val layers: List<Texture>
) : IntervalSystem() {
    // Increased parallax factors for more pronounced depth
    private val parallaxFactors = listOf(0.1f, 0.3f, 0.6f)

    override fun onTick() {
        if (layers.isEmpty()) return

        batch.use(camera.combined) { b ->
            layers.forEachIndexed { index, texture ->
                val factor = parallaxFactors.getOrElse(index) { 0.1f }

                // Screen dimensions in world units
                val w = camera.viewportWidth * camera.zoom
                val h = camera.viewportHeight * camera.zoom

                // Position of the bottom-left corner of the screen in world units
                val x = camera.position.x - w / 2f
                val y = camera.position.y - h / 2f

                // UV calculation centered on camera position
                val uWidth = w / texture.width
                val vHeight = h / texture.height
                val uCenter = (camera.position.x * factor) / texture.width
                val vCenter = (camera.position.y * factor) / texture.height

                val u = uCenter - uWidth / 2f
                val v = vCenter - vHeight / 2f
                val u2 = uCenter + uWidth / 2f
                val v2 = vCenter + vHeight / 2f

                b.draw(texture, x, y, w, h, u, v, u2, v2)
            }
        }
    }
}

class PlayerInputSystem : IteratingSystem(family { all(PlayerComponent, VelocityComponent) }) {
    private val moveForce = Vector2()
    val joystickVector = Vector2()

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent]
        moveForce.set(joystickVector)

        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) moveForce.y += 1f
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) moveForce.y -= 1f
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) moveForce.x -= 1f
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveForce.x += 1f

        if (!moveForce.isZero) {
            moveForce.nor().scl(vel.acceleration * world.deltaTime)
            vel.v.add(moveForce)
        }
    }
}

class MoveSystem : IteratingSystem(family { all(PositionComponent, VelocityComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent]
        val vel = entity[VelocityComponent]

        pos.pos.x += vel.v.x * world.deltaTime
        pos.pos.y += vel.v.y * world.deltaTime

        vel.v.scl(1f - (vel.friction * world.deltaTime))
    }
}

class CameraSystem(
    private val camera: OrthographicCamera
) : IteratingSystem(family { all(PlayerComponent, PositionComponent, VelocityComponent) }) {
    private val targetPos = Vector2()
    private val lerpSpeed = 2.5f

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v

        targetPos.set(pos.x + (vel.x * 0.5f), pos.y + (vel.y * 0.5f))

        camera.position.x += (targetPos.x - camera.position.x) * lerpSpeed * world.deltaTime
        camera.position.y += (targetPos.y - camera.position.y) * lerpSpeed * world.deltaTime

        val speedPercent = (vel.len() / 500f).coerceIn(0f, 1f)
        val targetZoom = 1.0f + (speedPercent * 0.4f)
        camera.zoom += (targetZoom - camera.zoom) * world.deltaTime

        camera.update()
    }
}

class RenderSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera,
    private val drawer: ShapeDrawer
) : IteratingSystem(family { all(PositionComponent).any(PlayerComponent, VelocityComponent) }) {

    override fun onTick() {
        batch.use(camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos

        if (entity has PlayerComponent) {
            val vel = entity[VelocityComponent].v
            val angle = if (vel.len2() > 0.1f) vel.angleRad() else MathUtils.PI / 2f
            drawTier1Ship(pos.x, pos.y, angle)
        }
    }

    private fun drawTier1Ship(x: Float, y: Float, rotation: Float) {
        val size = 16f
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        val x1 = x + size * cos
        val y1 = y + size * sin
        val x2 = x + size * MathUtils.cos(rotation + 2.4f)
        val y2 = y + size * MathUtils.sin(rotation + 2.4f)
        val x3 = x + size * MathUtils.cos(rotation - 2.4f)
        val y3 = y + size * MathUtils.sin(rotation - 2.4f)

        drawer.setColor(Color.CYAN)
        drawer.line(x1, y1, x2, y2, 2f)
        drawer.line(x2, y2, x3, y3, 2f)
        drawer.line(x3, y3, x1, y1, 2f)

        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, 3f)
    }
}
