package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
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

    private val starHues = arrayOf(
        Color(0.70f, 0.80f, 1.00f, 1f), // Blue
        Color(0.85f, 0.90f, 1.00f, 1f), // Blue-White
        Color(1.00f, 1.00f, 1.00f, 1f), // White
        Color(1.00f, 1.00f, 0.90f, 1f), // Yellow-White
        Color(1.00f, 1.00f, 0.80f, 1f), // Yellow
        Color(1.00f, 0.90f, 0.70f, 1f), // Orange
        Color(1.00f, 0.70f, 0.70f, 1f)  // Red
    )

    private val layers = arrayOf(
        // Distant layers: Denser, smaller, greyish/dimmer
        StarLayer(parallax = 0.05f, spacing = 8f, baseBrightness = 0.25f, saturation = 0.2f, seed = 1.1f),
        StarLayer(parallax = 0.10f, spacing = 12f, baseBrightness = 0.45f, saturation = 0.4f, seed = 2.2f),
        StarLayer(parallax = 0.25f, spacing = 20f, baseBrightness = 0.65f, saturation = 0.7f, seed = 3.3f),
        // Near layer: Sparse, brighter, colorful
        StarLayer(parallax = 0.50f, spacing = 45f, baseBrightness = 0.95f, saturation = 1.0f, seed = 4.4f)
    )

    private val tempColor = Color()

    override fun onTick() {
        val viewportWidth = camera.viewportWidth * camera.zoom
        val viewportHeight = camera.viewportHeight * camera.zoom

        val pxPerUnit = Gdx.graphics.width / viewportWidth
        val minSize = 1.5f / pxPerUnit
        val maxSize = 4.0f / pxPerUnit

        layers.forEach { layer ->
            val offsetX = camera.position.x * (1f - layer.parallax)
            val offsetY = camera.position.y * (1f - layer.parallax)

            val spacing = layer.spacing
            val startX = floor((camera.position.x - viewportWidth / 2f - offsetX) / spacing).toInt() * spacing.toInt()
            val startY = floor((camera.position.y - viewportHeight / 2f - offsetY) / spacing).toInt() * spacing.toInt()
            val endX = startX + viewportWidth.toInt() + spacing.toInt() * 2
            val endY = startY + viewportHeight.toInt() + spacing.toInt() * 2

            for (x in startX..endX step spacing.toInt()) {
                for (y in startY..endY step spacing.toInt()) {
                    val rand1 = pseudoRandom(x, y, layer.seed)
                    val rand2 = pseudoRandom(x, y, layer.seed + 10f)
                    val rand3 = pseudoRandom(x, y, layer.seed + 20f)

                    val px = x + offsetX + rand1 * spacing
                    val py = y + offsetY + rand2 * spacing

                    val size = minSize + (maxSize - minSize) * rand3

                    // Natural hue dampened by layer saturation
                    val hueIdx = (rand1 * starHues.size).toInt().coerceIn(0, starHues.lastIndex)
                    tempColor.set(starHues[hueIdx])

                    // Desaturate distant stars (lerp towards grey)
                    val grey = (tempColor.r + tempColor.g + tempColor.b) / 3f
                    tempColor.r = tempColor.r * layer.saturation + grey * (1f - layer.saturation)
                    tempColor.g = tempColor.g * layer.saturation + grey * (1f - layer.saturation)
                    tempColor.b = tempColor.b * layer.saturation + grey * (1f - layer.saturation)

                    tempColor.a = layer.baseBrightness * (0.4f + rand2 * 0.6f)

                    drawer.setColor(tempColor)
                    drawer.filledCircle(px, py, size / 2f)
                }
            }
        }
    }

    private fun pseudoRandom(x: Int, y: Int, seed: Float): Float {
        val n = (x * 37 + y * 71 + (seed * 100).toInt() * 127)
        return (Math.abs(n * n * 15731 + 789221) % 1000) / 1000f
    }

    private data class StarLayer(
        val parallax: Float,
        val spacing: Float,
        val baseBrightness: Float,
        val saturation: Float,
        val seed: Float
    )
}
