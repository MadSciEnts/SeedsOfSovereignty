package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.floor

class StarfieldRenderSystem(
    private val camera: OrthographicCamera = inject("camera"),
    private val drawer: ShapeDrawer = inject("drawer")
) : IntervalSystem() {

    private val layers = arrayOf(
        StarLayer(parallax = 0.05f, density = 20, size = 0.05f, color = Color(0.4f, 0.4f, 0.6f, 0.5f)),
        StarLayer(parallax = 0.10f, density = 40, size = 0.08f, color = Color(0.6f, 0.6f, 0.8f, 0.7f)),
        StarLayer(parallax = 0.25f, density = 30, size = 0.12f, color = Color(0.8f, 0.8f, 1.0f, 0.9f)),
        StarLayer(parallax = 0.50f, density = 15, size = 0.20f, color = Color(1.0f, 1.0f, 1.0f, 1.0f))
    )

    override fun onTick() {
        layers.forEach { layer ->
            drawer.setColor(layer.color)
            val viewportWidth = camera.viewportWidth * camera.zoom
            val viewportHeight = camera.viewportHeight * camera.zoom
            val offsetX = camera.position.x * (1f - layer.parallax)
            val offsetY = camera.position.y * (1f - layer.parallax)
            val spacing = 15f
            val startX = floor((camera.position.x - viewportWidth / 2f - offsetX) / spacing).toInt() * spacing.toInt()
            val startY = floor((camera.position.y - viewportHeight / 2f - offsetY) / spacing).toInt() * spacing.toInt()
            val endX = startX + viewportWidth.toInt() + spacing.toInt() * 2
            val endY = startY + viewportHeight.toInt() + spacing.toInt() * 2

            for (x in startX..endX step spacing.toInt()) {
                for (y in startY..endY step spacing.toInt()) {
                    val px = x + offsetX + pseudoRandom(x, y, 0f) * spacing
                    val py = y + offsetY + pseudoRandom(x, y, 1f) * spacing
                    drawer.filledCircle(px, py, layer.size)
                }
            }
        }
    }

    private fun pseudoRandom(x: Int, y: Int, seed: Float): Float {
        val n = (x * 37 + y * 71 + seed.toInt() * 127)
        return (Math.abs(n * n * 15731 + 789221) % 1000) / 1000f
    }

    private data class StarLayer(val parallax: Float, val density: Int, val size: Float, val color: Color)
}
