package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.UiAssetGenerator

/**
 * Procedural circular radar display.
 * Renders technical sweeps and dynamic blips for entities.
 */
class RadarDisplay(val game: SovereigntyGame) : Actor() {
    private val bgTexture: Texture
    private val playerBlip: Texture
    private val enemyBlip: Texture

    // List of relative coordinates for blips (x, y, isEnemy)
    val blips = mutableListOf<BlipData>()

    data class BlipData(val x: Float, val y: Float, val isEnemy: Boolean)

    init {
        val radius = 100
        setSize(radius * 2f, radius * 2f)
        val assetGen = UiAssetGenerator(game.uiFactory.style)

        bgTexture = Texture(assetGen.generateRadarBackground(radius))
        playerBlip = Texture(assetGen.generateRadarPoint(4, Color.CYAN))
        enemyBlip = Texture(assetGen.generateRadarPoint(4, Color.ORANGE))
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        // 1. Draw Background
        batch.draw(bgTexture, x, y, width, height)

        // 2. Draw dynamic blips
        val centerX = x + width / 2f
        val centerY = y + height / 2f
        val radarRadius = width / 2f

        blips.forEach { blip ->
            val texture = if (blip.isEnemy) enemyBlip else playerBlip
            // Blip coordinates are -1 to 1 relative to radar center
            val drawX = centerX + (blip.x * radarRadius) - (texture.width / 2f)
            val drawY = centerY + (blip.y * radarRadius) - (texture.height / 2f)
            batch.draw(texture, drawX, drawY)
        }
    }

    fun dispose() {
        bgTexture.dispose()
        playerBlip.dispose()
        enemyBlip.dispose()
    }
}
