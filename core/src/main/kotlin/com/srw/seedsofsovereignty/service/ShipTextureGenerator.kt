package com.srw.seedsofsovereignty.service

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.srw.seedsofsovereignty.component.FactionType
import com.srw.seedsofsovereignty.component.ShapeType
import java.util.*

/**
 * SHIP TEXTURE GENERATOR: Procedural layering for tiered ships and modular cards.
 * Now handles Faction-specific aesthetics:
 * - PLAYER: Geometrical (Triangles, Squares)
 * - ENEMY: Aggressive Curves, Sharp Points, Red/Yellow Danger Overlays
 * - NEUTRAL: Symmetrical Geometry
 */
object ShipTextureGenerator {

    private val regionCache = mutableMapOf<String, TextureRegion>()

    data class VisualLayerData(
        val region: TextureRegion,
        val color: Color,
        val pulse: Boolean,
        val zIndex: Int
    )

    fun generateLayered(seed: Long, tier: Int, baseColor: Color, faction: FactionType = FactionType.PLAYER): List<VisualLayerData> {
        val random = Random(seed + tier)
        val size = if (tier < 9) 32 else 64
        val layers = mutableListOf<VisualLayerData>()

        // 1. Hull Layer (Bottom)
        val hullColor = baseColor.cpy().mul(0.2f, 0.2f, 0.2f, 1f)
        layers.add(VisualLayerData(generateHull(random, tier, size, true, faction), hullColor, false, 0))

        // 2. Greeble Layer (Middle)
        val detailColor = baseColor.cpy().mul(0.5f, 0.5f, 0.5f, 1f)
        layers.add(VisualLayerData(generateGreebles(random, tier, size, faction), detailColor, false, 1))

        // 3. Neon Layer (Top)
        layers.add(VisualLayerData(generateHull(random, tier, size, false, faction), baseColor, true, 2))

        // 4. Enemy "Dangerous Intensity" Layer
        if (faction == FactionType.ENEMY) {
            layers.add(VisualLayerData(generateDangerLines(random, size), Color.YELLOW, true, 3))
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
        pixmap.drawRectangle(4, 4, size - 8, size - 8)

        val tex = Texture(pixmap)
        pixmap.dispose()
        val region = TextureRegion(tex)
        regionCache[cacheKey] = region
        return region
    }

    fun getProjectileGlow(type: ShapeType, color: Color): TextureRegion {
        val cacheKey = "glow_${type}_${color.toIntBits()}"
        regionCache[cacheKey]?.let { return it }

        val size = 32
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, 0.4f)

        when (type) {
            ShapeType.CIRCLE -> pixmap.fillCircle(16, 16, 14)
            ShapeType.SQUARE -> pixmap.fillRectangle(4, 4, 24, 24)
            ShapeType.TRIANGLE -> pixmap.fillTriangle(16, 4, 4, 28, 28, 28)
            else -> pixmap.fillCircle(16, 16, 10)
        }

        val tex = Texture(pixmap)
        pixmap.dispose()
        val region = TextureRegion(tex)
        regionCache[cacheKey] = region
        return region
    }

    private fun generateHull(random: Random, tier: Int, size: Int, solid: Boolean, faction: FactionType): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val mid = size / 2
        val margin = size / 8
        pixmap.setColor(Color.WHITE)

        val w = (size / 2) - margin
        val h = (size / 2) - margin

        when (faction) {
            FactionType.PLAYER -> {
                if (tier <= 4) { // Interceptor
                    val pts = intArrayOf(mid, margin, mid + w, size - margin, mid - w, size - margin)
                    if (solid) pixmap.fillTriangle(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5])
                    else drawOutline(pixmap, pts)
                } else { // Capital
                    if (solid) pixmap.fillRectangle(margin, mid - h/4, size - 2*margin, h/2)
                    else pixmap.drawRectangle(margin, mid - h/4, size - 2*margin, h/2)
                }
            }
            FactionType.ENEMY -> {
                // Aggressive curves and sharp spikes
                if (solid) {
                    pixmap.fillCircle(mid, mid, w)
                    pixmap.fillRectangle(mid - 2, margin, 4, size - 2*margin)
                } else {
                    pixmap.drawCircle(mid, mid, w)
                    pixmap.drawRectangle(mid - 1, margin, 2, size - 2*margin)
                }
            }
            FactionType.NEUTRAL -> {
                // Symmetrical Diamond
                val pts = intArrayOf(mid, margin, size - margin, mid, mid, size - margin, margin, mid)
                if (solid) {
                    pixmap.fillTriangle(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5])
                    pixmap.fillTriangle(pts[4], pts[5], pts[6], pts[7], pts[0], pts[1])
                } else {
                    drawOutline(pixmap, pts)
                }
            }
        }

        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateGreebles(random: Random, tier: Int, size: Int, faction: FactionType): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        val density = 5 + tier
        for (i in 0 until density) {
            val x = random.nextInt(size - 4) + 2
            val y = random.nextInt(size - 4) + 2
            if (faction == FactionType.ENEMY) {
                pixmap.drawCircle(x, y, random.nextInt(3) + 1)
            } else {
                pixmap.drawRectangle(x, y, random.nextInt( size / 4) + 2, random.nextInt(2) + 1)
            }
        }
        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun generateDangerLines(random: Random, size: Int): TextureRegion {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        val mid = size / 2
        // Rotating dangerous red/yellow intensity lines (Horizontal bars)
        pixmap.fillRectangle(0, mid - 4, size, 2)
        pixmap.fillRectangle(0, mid + 2, size, 2)

        val tex = Texture(pixmap)
        pixmap.dispose()
        return TextureRegion(tex)
    }

    private fun drawOutline(pixmap: Pixmap, points: IntArray) {
        for (i in 0 until points.size step 2) {
            val next = (i + 2) % points.size
            pixmap.drawLine(points[i], points[i+1], points[next], points[next+1])
        }
    }

    fun generate(seed: Long, tier: Int, outlineColor: Color): TextureRegion {
        return generateHull(Random(seed), tier, 32, false, FactionType.PLAYER)
    }
}
