package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.srw.seedsofsovereignty.component.FactionType
import com.srw.seedsofsovereignty.component.RenderEffect
import com.srw.seedsofsovereignty.component.ShapeType
import java.util.*

/**
 * SHIP TEXTURE GENERATOR: Procedural layering for tiered ships and massive modular stations.
 * Finalized for Factional Palettes and Effect-Driven Layers.
 */
object ShipTextureGenerator {

    private val regionCache = mutableMapOf<String, TextureRegion>()

    data class VisualLayerData(
        val region: TextureRegion,
        val color: Color,
        val effect: RenderEffect,
        val zIndex: Int
    )

    fun generateLayered(seed: Long, tier: Int, baseColor: Color, faction: FactionType = FactionType.PLAYER, isStation: Boolean = false): List<VisualLayerData> {
        val random = Random(seed + tier)
        val size = if (isStation || tier >= 9) 64 else 32
        val layers = mutableListOf<VisualLayerData>()

        // Faction Palette Selection
        val factionColor = when(faction) {
            FactionType.PLAYER -> Color.CYAN
            FactionType.ENEMY -> Color.ORANGE
            else -> Color.LIME // Neutral / Friendly Stations
        }

        // 1. Hull Layer (Bottom) - STATIC
        val hullColor = factionColor.cpy().mul(0.15f, 0.15f, 0.15f, 1f)
        layers.add(VisualLayerData(generateHull(random, tier, size, true, faction, isStation), hullColor, RenderEffect.STATIC, 0))

        // 2. Details (Middle) - STATIC
        val detailColor = factionColor.cpy().mul(0.4f, 0.4f, 0.4f, 1f)
        layers.add(VisualLayerData(generateGreebles(random, tier, size, faction, isStation), detailColor, RenderEffect.STATIC, 1))

        // 3. Emissive Neon (Top) - STATIC for structural read
        val neonColor = factionColor.cpy().mul(0.8f, 0.8f, 0.8f, 1f)
        layers.add(VisualLayerData(generateHull(random, tier, size, false, faction, isStation), neonColor, RenderEffect.STATIC, 2))

        // 4. Activity Accents - PULSE or FLICKER
        if (faction == FactionType.ENEMY) {
            layers.add(VisualLayerData(generateAggressiveAccents(random, size), Color.RED, RenderEffect.FLICKER, 3))
        } else {
            // Friendly ships/stations have a calm cyan/green pulse
            layers.add(VisualLayerData(generateAggressiveAccents(random, size), factionColor, RenderEffect.PULSE, 3))
        }

        return layers
    }

    fun generateModuleCard(seed: Long, color: Color): TextureRegion {
        val cacheKey = "mod_${seed}_${color.toIntBits()}"
        regionCache[cacheKey]?.let { return it }
        val size = 16
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.drawRectangle(2, 2, size - 4, size - 4)
        val tex = Texture(pixmap); pixmap.dispose()
        val region = TextureRegion(tex)
        regionCache[cacheKey] = region
        return region
    }

    fun getProjectileGlow(type: ShapeType, color: Color): TextureRegion {
        val cacheKey = "glow_${type}_${color.toIntBits()}"
        regionCache[cacheKey]?.let { return it }
        val size = 32
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, 0.7f) // Higher alpha for energy visibility
        when (type) {
            ShapeType.CIRCLE -> pixmap.fillCircle(16, 16, 14)
            ShapeType.SQUARE -> pixmap.fillRectangle(4, 4, 24, 24)
            ShapeType.TRIANGLE -> pixmap.fillTriangle(16, 4, 4, 28, 28, 28)
            else -> pixmap.fillCircle(16, 16, 10)
        }
        val tex = Texture(pixmap); pixmap.dispose()
        val region = TextureRegion(tex)
        regionCache[cacheKey] = region
        return region
    }

    private fun generateHull(random: Random, tier: Int, size: Int, solid: Boolean, faction: FactionType, isStation: Boolean): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val mid = size / 2
        val margin = size / 8
        pixmap.setColor(Color.WHITE)

        if (isStation) {
            val pts = intArrayOf(mid, 0, size, mid, mid, size, 0, mid)
            if (solid) {
                pixmap.fillTriangle(pts[0], pts[1], pts[2], pts[3], mid, mid)
                pixmap.fillTriangle(pts[2], pts[3], pts[4], pts[5], mid, mid)
                pixmap.fillTriangle(pts[4], pts[5], pts[6], pts[7], mid, mid)
                pixmap.fillTriangle(pts[6], pts[7], pts[0], pts[1], mid, mid)
            } else drawOutline(pixmap, pts)
        } else {
            val w = (size / 2) - margin
            when (faction) {
                FactionType.PLAYER -> {
                    val pts = intArrayOf(mid, margin, size - margin, size - margin, margin, size - margin)
                    if (solid) pixmap.fillTriangle(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5])
                    else drawOutline(pixmap, pts)
                }
                FactionType.ENEMY -> {
                    if (solid) {
                        pixmap.fillCircle(mid, mid, w - 2)
                        pixmap.fillTriangle(mid - 4, 0, mid + 4, 0, mid, mid)
                        pixmap.fillTriangle(0, size, margin, mid, mid, size)
                        pixmap.fillTriangle(size, size, size - margin, mid, mid, size)
                    } else {
                        pixmap.drawCircle(mid, mid, w - 2)
                        pixmap.drawLine(mid, 0, mid, mid)
                    }
                }
                else -> {
                    if (solid) pixmap.fillRectangle(margin, mid - 2, size - 2*margin, 4)
                    else pixmap.drawRectangle(margin, mid - 2, size - 2*margin, 4)
                }
            }
        }

        val tex = Texture(pixmap); pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateGreebles(random: Random, tier: Int, size: Int, faction: FactionType, isStation: Boolean): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        repeat(if (isStation) 25 else 5 + tier) {
            val x = random.nextInt(size - 4); val y = random.nextInt(size - 4)
            if (faction == FactionType.ENEMY) pixmap.drawCircle(x, y, 1)
            else pixmap.drawRectangle(x, y, 2, 2)
        }
        val tex = Texture(pixmap); pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateAggressiveAccents(random: Random, size: Int): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        val mid = size / 2
        val margin = size / 6
        pixmap.fillTriangle(mid - 6, margin, mid - 2, margin + 4, mid - 4, margin + 8)
        pixmap.fillTriangle(mid + 6, margin, mid + 2, margin + 4, mid + 4, margin + 8)
        val tex = Texture(pixmap); pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun drawOutline(pixmap: Pixmap, points: IntArray) {
        for (i in 0 until points.size step 2) {
            val next = (i + 2) % points.size
            pixmap.drawLine(points[i], points[i+1], points[next], points[next+1])
        }
    }

    fun generate(seed: Long, tier: Int, outlineColor: Color): TextureRegion {
        return generateHull(Random(seed), tier, 32, false, FactionType.PLAYER, false)
    }
}
