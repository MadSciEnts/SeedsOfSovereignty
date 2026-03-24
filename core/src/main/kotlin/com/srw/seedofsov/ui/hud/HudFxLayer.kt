package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.srw.seedofsov.ui.render.RibbonPath

/**
 * A dedicated Scene2D layer for high-fidelity ribbon effects.
 * Truth on Disk: Uses additive blending and a dedicated manifold shader for high-energy "burn through".
 * Touchable is disabled to allow input to pass through to elements beneath.
 */
class HudFxLayer : Actor() {
    private val shader = ShaderProgram(
        Gdx.files.internal("shaders/trail.vert"),
        Gdx.files.internal("shaders/manifold.frag")
    ).apply {
        if (!isCompiled) Gdx.app.error("HudFxLayer", "Shader failed: $log")
    }

    private val renderer = ImmediateModeRenderer20(5000, false, true, 0, shader)
    private val activeRibbons = mutableMapOf<String, RibbonPath>()

    private val tempVec = Vector2()
    private val lerpColor = Color()

    init {
        touchable = Touchable.disabled
    }

    fun getRibbon(key: String, config: (RibbonPath) -> Unit = {}): RibbonPath {
        return activeRibbons.getOrPut(key) {
            RibbonPath().also(config)
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        val iter = activeRibbons.entries.iterator()
        while (iter.hasNext()) {
            val ribbon = iter.next().value
            ribbon.update(delta)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (activeRibbons.isEmpty()) return

        batch.end()

        Gdx.gl.glEnable(GL20.GL_BLEND)
        // Truth on Disk: Use ADDITIVE BLENDING for high-energy "white-hot" overlap
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)

        activeRibbons.values.forEach { ribbon ->
            val segments = ribbon.segments
            if (segments.size < 2) return@forEach

            renderer.begin(batch.projectionMatrix, GL20.GL_TRIANGLE_STRIP)

            val iter = segments.iterator()
            var prev: RibbonPath.Segment? = null
            var curr = iter.next()
            var next: RibbonPath.Segment? = if (iter.hasNext()) iter.next() else null

            var i = 0
            while (true) {
                if (next != null) {
                    tempVec.set(next.position).sub(curr.position)
                } else if (prev != null) {
                    tempVec.set(curr.position).sub(prev.position)
                }

                if (tempVec.len2() < 0.0001f) {
                    tempVec.set(curr.initialDir)
                } else {
                    tempVec.nor()
                }

                val perpX = -tempVec.y
                val perpY = tempVec.x

                val sequencePct = MathUtils.clamp(1f - (i.toFloat() / (ribbon.maxSegments - 1).coerceAtLeast(1).toFloat()), 0f, 1f)
                val combinedLife = MathUtils.clamp(sequencePct * curr.life, 0f, 1f)

                lerpColor.set(ribbon.targetColor).lerp(ribbon.color, curr.life)
                val alpha = lerpColor.a * sequencePct * parentAlpha
                val width = ribbon.baseWidth * combinedLife

                renderer.color(lerpColor.r, lerpColor.g, lerpColor.b, alpha)
                renderer.vertex(curr.position.x + perpX * width, curr.position.y + perpY * width, 0f)

                renderer.color(lerpColor.r, lerpColor.g, lerpColor.b, alpha)
                renderer.vertex(curr.position.x - perpX * width, curr.position.y - perpY * width, 0f)

                if (next == null) break

                prev = curr
                curr = next
                next = if (iter.hasNext()) iter.next() else null
                i++
            }

            renderer.end()
        }

        // Restore standard blending
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        batch.begin()
    }

    fun clearRibbons() {
        activeRibbons.clear()
    }

    fun dispose() {
        renderer.dispose()
        shader.dispose()
    }
}
