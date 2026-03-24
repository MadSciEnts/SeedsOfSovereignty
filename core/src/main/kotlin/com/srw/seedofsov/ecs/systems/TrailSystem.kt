package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.Viewport
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.CoordinateSpace
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Procedural ribbon trail renderer with temporal dissipation and timed burst support.
 * Truth on Disk: Supports world and screen space rendering with scaled turbulence.
 */
class TrailSystem(private val viewport: Viewport) : IteratingSystem(
    allOf(TrailComponent::class, TransformComponent::class).get(),
    49
), Disposable {
    private val shader = ShaderProgram(
        Gdx.files.internal("shaders/trail.vert"),
        Gdx.files.internal("shaders/trail.frag")
    ).apply {
        if (!isCompiled) {
            Gdx.app.error("TrailSystem", "Shader compilation failed: $log")
        }
    }

    private val renderer = ImmediateModeRenderer20(2000, false, true, 0, shader)
    private val uiMatrix = Matrix4()

    private val tempVec = Vector2()
    private val exhaustPos = Vector2()
    private val leftVec = Vector2()
    private val rightVec = Vector2()
    private val lerpColor = Color()

    override fun update(deltaTime: Float) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        uiMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        super.update(deltaTime)

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val trail = entity[TrailComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val engine = entity[ImpulseEngineComponent.mapper]
        val effect = entity[EffectComponent.mapper]

        if (trail.isTransient && !trail.isEmitting && trail.segments.isEmpty()) {
            getEngine().removeEntity(entity)
            return
        }

        effect?.parentTransform?.let { parent ->
            transform.position.set(
                parent.position.x + effect.relativeOffset.x,
                parent.position.y + effect.relativeOffset.y,
                transform.position.z
            )
        }

        val iter = trail.segments.iterator()
        while (iter.hasNext()) {
            val segment = iter.next()
            segment.position.add(segment.drift.x * deltaTime, segment.drift.y * deltaTime)
            segment.life -= trail.dissipationRate * deltaTime
            if (segment.life <= 0f) {
                iter.remove()
            }
        }

        if (trail.isEmitting) {
            if (trail.emissionDuration > 0) {
                trail.emissionDuration -= deltaTime
                if (trail.emissionDuration <= 0) {
                    trail.isEmitting = false
                }
            }

            if (engine != null) {
                tempVec.set(0f, -transform.size.y * 0.45f).rotateDeg(transform.rotation)
                exhaustPos.set(transform.position.x + tempVec.x, transform.position.y + tempVec.y)
            } else {
                exhaustPos.set(transform.position.x, transform.position.y)
            }

            if (trail.segments.isEmpty() || Vector2.dst(transform.position.x, transform.position.y, trail.lastEmissionPos.x, trail.lastEmissionPos.y) >= trail.emissionDistance) {
                // Truth on Disk: Noise intensity scaled by component property for screen-space visibility
                val noiseIntensity = 1.25f * trail.noiseScale
                var dx = (Math.abs((exhaustPos.x * 123.4f + exhaustPos.y * 567.8f + trail.noiseSeed).hashCode() % 100) / 100f - 0.5f) * noiseIntensity
                var dy = (Math.abs((exhaustPos.x * 432.1f + exhaustPos.y * 876.5f + trail.noiseSeed).hashCode() % 100) / 100f - 0.5f) * noiseIntensity

                if (trail.driftVariance > 0) {
                    dx += Math.abs(MathUtils.random(-trail.driftVariance, trail.driftVariance))
                    dy += Math.abs(MathUtils.random(-trail.driftVariance, trail.driftVariance))
                }

                trail.segments.addFirst(TrailSegment(Vector2(exhaustPos), Vector2(dx, dy), 1.0f))
                trail.lastEmissionPos.set(exhaustPos)

                if (trail.segments.size > trail.maxSegments) {
                    trail.segments.removeLast()
                }
            }
        }

        if (trail.segments.size < 2) return

        val proj = if (trail.space == CoordinateSpace.SCREEN) uiMatrix else viewport.camera.combined
        renderer.begin(proj, GL20.GL_TRIANGLE_STRIP)

        val segments = trail.segments
        for (i in 0 until segments.size - 1) {
            val curr = segments[i]
            val next = segments[i + 1]
            val pos = curr.position

            tempVec.set(next.position).sub(pos).nor()
            val perpX = -tempVec.y
            val perpY = tempVec.x

            val sequencePct = 1f - (i.toFloat() / trail.maxSegments.toFloat())
            val combinedLife = sequencePct * curr.life

            lerpColor.set(trail.targetColor).lerp(trail.color, curr.life)
            val alpha = lerpColor.a * sequencePct
            val width = trail.baseWidth * combinedLife

            renderer.color(lerpColor.r, lerpColor.g, lerpColor.b, alpha)
            renderer.vertex(pos.x + perpX * width, pos.y + perpY * width, 0f)

            renderer.color(lerpColor.r, lerpColor.g, lerpColor.b, alpha)
            renderer.vertex(pos.x - perpX * width, pos.y - perpY * width, 0f)
        }

        renderer.end()
    }

    override fun dispose() {
        renderer.dispose()
        shader.dispose()
    }
}
