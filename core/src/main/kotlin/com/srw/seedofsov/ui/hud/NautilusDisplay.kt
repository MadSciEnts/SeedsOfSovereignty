package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.UiAssetGenerator

/**
 * Advanced technical integrity display.
 * Shell Hierarchy: Shield (Blue, Outer) -> Hull (White, Inner) -> Power Core (Center).
 */
class NautilusDisplay(val game: SovereigntyGame) : Actor() {

    // Status Percentages per quadrant (0.0 to 1.0)
    val shieldPcts = floatArrayOf(1f, 1f, 1f, 1f)
    val hullPcts = floatArrayOf(1f, 1f, 1f, 1f)

    // Core 'Lives' percentages (sequential list)
    val corePcts = mutableListOf<Float>()

    private val shieldQuads = mutableListOf<Texture>()
    private val hullQuads = mutableListOf<Texture>()
    private val coreTexture: Texture

    private val arcGap = 15f
    private val arcLen = (360f / 4f) - arcGap

    init {
        setSize(200f, 200f)
        val assetGen = UiAssetGenerator(game.uiFactory.style)

        // Shell 1 (Outer): Shield
        generateShell(shieldQuads, assetGen, 95, 12)
        // Shell 2 (Inner): Hull
        generateShell(hullQuads, assetGen, 75, 10)

        // Power Core Core (Center)
        val corePixmap = assetGen.generateRadarPoint(45, Color.WHITE)
        coreTexture = Texture(corePixmap)
        corePixmap.dispose()
    }

    private fun generateShell(list: MutableList<Texture>, gen: UiAssetGenerator, radius: Int, thickness: Int) {
        for (i in 0 until 4) {
            val startAngle = (i * 90f) + (arcGap / 2f)
            val pixmap = gen.generateNautilusArc(radius, thickness, startAngle, arcLen, Color.WHITE)
            list.add(Texture(pixmap))
            pixmap.dispose()
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val oldColor = batch.color.cpy()

        // 1. Draw Shield Layer (Blue, Directional)
        drawDirectionalShell(batch, shieldQuads, shieldPcts, Color.CYAN, parentAlpha)

        // 2. Draw Hull Layer (White, Directional)
        drawDirectionalShell(batch, hullQuads, hullPcts, Color.WHITE, parentAlpha)

        // 3. Draw Active Power Core (Center, Sequental Lives)
        val activeCorePct = corePcts.firstOrNull { it > 0 } ?: 0f

        // Dynamic Health Color logic
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
            drawColor.a = 0.1f + (fill * 0.9f) // Outline at 10%, solid at 100%

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
