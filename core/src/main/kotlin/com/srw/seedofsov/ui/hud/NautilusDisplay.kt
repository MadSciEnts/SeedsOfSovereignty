package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.UiAssetGenerator

/**
 * Advanced technical integrity display.
 * Shell Hierarchy: Shield (Blue, Outer) -> Hull (White, Inner) -> Power Core (Center).
 * Truth on Disk: Visualizes active shields, priority highlighting, and plasma cooldowns.
 */
class NautilusDisplay(val game: SovereigntyGame) : Actor() {

    // Status Percentages per quadrant (0.0 to 1.0)
    val shieldPcts = floatArrayOf(1f, 1f, 1f, 1f)
    val hullPcts = floatArrayOf(1f, 1f, 1f, 1f)

    val shieldActive = booleanArrayOf(true, true, true, true)
    val shieldBufferPcts = floatArrayOf(0f, 0f, 0f, 0f)

    /**
     * Currently reinforced quadrant (0: Front, 1: Left, 2: Rear, 3: Right).
     */
    var priorityQuadrant: Int = -1

    val corePcts = mutableListOf<Float>()

    private val shieldQuads = mutableListOf<Texture>()
    private val hullQuads = mutableListOf<Texture>()
    private val coreTexture: Texture

    private val arcGap = 15f
    private val arcLen = (360f / 4f) - arcGap

    private val plasmaColors = arrayOf(Color.GOLD, Color.CYAN, Color.PURPLE, Color.WHITE)
    private var stateTime = 0f

    init {
        setSize(200f, 200f)
        val assetGen = UiAssetGenerator(game.uiFactory.style)

        generateShell(shieldQuads, assetGen, 95, 12)
        generateShell(hullQuads, assetGen, 75, 10)

        val corePixmap = assetGen.generateRadarPoint(45, Color.WHITE)
        coreTexture = Texture(corePixmap)
        corePixmap.dispose()
    }

    private fun generateShell(list: MutableList<Texture>, gen: UiAssetGenerator, radius: Int, thickness: Int) {
        for (i in 0 until 4) {
            val startAngle = (i * 90f) + 90f - (arcLen / 2f)
            val pixmap = gen.generateNautilusArc(radius, thickness, startAngle, arcLen, Color.WHITE)
            list.add(Texture(pixmap))
            pixmap.dispose()
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val oldColor = batch.color.cpy()

        // 1. Draw Shield Layer
        shieldQuads.forEachIndexed { index, texture ->
            val isActive = shieldActive[index]
            val fill = if (isActive) shieldPcts[index] else shieldBufferPcts[index]
            val isPriority = (index == priorityQuadrant)

            var drawColor = if (isActive) Color.CYAN else Color.GOLD
            var alphaFactor = 0.1f + (fill * 0.9f)

            if (!isActive) {
                val roll = MathUtils.random()
                if (roll > 0.8f) {
                    drawColor = plasmaColors[MathUtils.random(plasmaColors.size - 1)]
                    alphaFactor *= 0.4f
                } else if (roll > 0.6f) {
                    alphaFactor *= 1.2f
                }
            }

            // PRIORITY HIGHLIGHT: Pulsing glow for the reinforced quadrant
            if (isPriority) {
                alphaFactor *= 1.5f
                val pulse = (MathUtils.sin(stateTime * 10f) + 1f) * 0.5f
                val glowAlpha = (0.1f + pulse * 0.2f) * parentAlpha
                batch.setColor(drawColor.r, drawColor.g, drawColor.b, glowAlpha)
                // Draw slightly larger for glow effect
                batch.draw(texture, x + (width - texture.width)/2f - 3f, y + (width - texture.width)/2f - 3f, texture.width + 6f, texture.width + 6f)
            }

            batch.setColor(drawColor.r, drawColor.g, drawColor.b, drawColor.a * alphaFactor * parentAlpha)
            val offset = (width - texture.width) / 2f
            batch.draw(texture, x + offset, y + offset)
        }

        drawDirectionalShell(batch, hullQuads, hullPcts, Color.WHITE, parentAlpha)

        val activeCorePct = corePcts.firstOrNull { it > 0 } ?: 0f
        val coreColor = when {
            activeCorePct >= 0.75f -> Color.GREEN
            activeCorePct >= 0.45f -> Color.YELLOW
            else -> Color.RED
        }

        batch.setColor(coreColor.r, coreColor.g, coreColor.b, (0.2f + (activeCorePct * 0.8f)) * parentAlpha)
        val coreOffset = (width - coreTexture.width) / 2f
        batch.draw(coreTexture, x + coreOffset, y + coreOffset)

        batch.setColor(oldColor)
    }

    private fun drawDirectionalShell(batch: Batch, quads: List<Texture>, pcts: FloatArray, baseColor: Color, parentAlpha: Float) {
        quads.forEachIndexed { index, texture ->
            val fill = pcts[index]
            val drawColor = baseColor.cpy()
            drawColor.a = 0.1f + (fill * 0.9f)

            batch.setColor(drawColor.r, drawColor.g, drawColor.b, drawColor.a * parentAlpha)
            val offset = (width - texture.width) / 2f
            batch.draw(texture, x + offset, y + offset)
        }
    }

    fun dispose() {
        shieldQuads.forEach { it.dispose() }
        hullQuads.forEach { it.dispose() }
        coreTexture.dispose()
    }
}
