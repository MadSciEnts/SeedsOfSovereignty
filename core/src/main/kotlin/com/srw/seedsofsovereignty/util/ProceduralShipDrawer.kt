package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import space.earlygrey.shapedrawer.ShapeDrawer

object ProceduralShipDrawer {

    fun getShipSize(tier: Int, subTier: Int): Float {
        val tierBase = 8f + (tier - 1) * 7.5f
        val finalSize = if (tier >= 8) tierBase + 45f else tierBase
        return finalSize + (subTier - 1) * 2.5f
    }

    fun getMountOffsets(tier: Int, subTier: Int, size: Float, hullRotation: Float, out: MutableList<Vector2>) {
        out.clear()
        val cos = MathUtils.cos(hullRotation); val sin = MathUtils.sin(hullRotation)

        if (tier < 8) {
            out.add(Vector2(0f, 0f))
        } else if (tier == 8) {
            val spread = size * 0.42f
            val count = subTier.coerceIn(1, 4)
            val locals = arrayOf(Vector2(spread, spread), Vector2(spread, -spread), Vector2(-spread, -spread), Vector2(-spread, spread))
            for (i in 0 until count) {
                out.add(Vector2(locals[i].x * cos - locals[i].y * sin, locals[i].x * sin + locals[i].y * cos))
            }
        } else {
            val spreadX = size * 0.7f
            val spreadY = size * 0.35f
            val count = subTier.coerceIn(1, 4)
            val locals = arrayOf(Vector2(spreadX, spreadY), Vector2(spreadX, -spreadY), Vector2(spreadX * 0.8f, spreadY * 1.5f), Vector2(spreadX * 0.8f, -spreadY * 1.5f))
            for (i in 0 until count) {
                out.add(Vector2(locals[i].x * cos - locals[i].y * sin, locals[i].x * sin + locals[i].y * cos))
            }
        }
    }

    fun drawPlayerShip(
        drawer: ShapeDrawer,
        x: Float, y: Float,
        hullRotation: Float, aimRotation: Float,
        tier: Int, subTier: Int,
        colorOverride: Color? = null
    ) {
        val size = getShipSize(tier, subTier)
        val color = colorOverride ?: when(tier) {
            1 -> Color.CYAN; 2 -> Color.VIOLET; 3 -> Color.GOLD; 4 -> Color.ORANGE
            5 -> Color.CHARTREUSE; 6 -> Color.SKY; 7 -> Color.PURPLE; 8 -> Color.SLATE
            9 -> Color.FIREBRICK
            else -> Color.CYAN
        }
        drawer.setColor(color)

        when (tier) {
            9 -> drawVanguardHull(drawer, x, y, hullRotation, size)
            8 -> drawDreadnoughtHull(drawer, x, y, hullRotation, size)
            6 -> drawSentinelPlusHull(drawer, x, y, hullRotation, size)
            7 -> drawWraithHull(drawer, x, y, hullRotation, size, color)
            else -> drawStandardHull(drawer, x, y, hullRotation, size, tier)
        }

        if (tier == 5) drawBulwarkAttachments(drawer, x, y, hullRotation, size)

        val mountSize = if (tier >= 8) 18f else size * 0.6f
        val offsets = mutableListOf<Vector2>()
        getMountOffsets(tier, subTier, size, hullRotation, offsets)
        offsets.forEach { drawWeaponMount(drawer, x + it.x, y + it.y, aimRotation, mountSize, color, subTier) }

        drawer.setColor(Color.WHITE); drawer.filledCircle(x, y, (size * 0.1f).coerceAtLeast(1.5f))
    }

    private fun drawWeaponMount(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float, color: Color, subTier: Int) {
        drawer.setColor(Color.DARK_GRAY); drawer.circle(x, y, size, 2f)
        drawer.setColor(color)
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)

        val numBarrels = subTier.coerceIn(1, 4)
        val barrelL = size * 1.5f
        val barrelW = (size * 0.4f).coerceAtLeast(2f)
        val spacing = size * 0.6f

        for (i in 0 until numBarrels) {
            val offset = (i - (numBarrels - 1) * 0.5f) * spacing
            drawer.line(x - offset * sin, y + offset * cos, x + (barrelL * cos) - offset * sin, y + (barrelL * sin) + offset * cos, barrelW)
        }
    }

    private fun drawVanguardHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        val boomL = size * 1.2f; val boomW = size * 0.25f; val boomGap = size * 0.4f
        val p1x = x - (size * 0.2f) * cos - (boomGap) * sin; val p1y = y - (size * 0.2f) * sin + (boomGap) * cos
        drawer.line(p1x, p1y, p1x + boomL * cos, p1y + boomL * sin, boomW)
        val p2x = x - (size * 0.2f) * cos + (boomGap) * sin; val p2y = y - (size * 0.2f) * sin - (boomGap) * cos
        drawer.line(p2x, p2y, p2x + boomL * cos, p2y + boomL * sin, boomW)
        val cx1 = x - (size * 0.5f) * cos; val cy1 = y - (size * 0.5f) * sin
        val cx2 = x + (size * 0.3f) * cos
        drawer.line(cx1 - boomGap * sin, cy1 + boomGap * cos, cx1 + boomGap * sin, cy1 - boomGap * cos, 4f)
        drawer.line(cx1 - boomGap * sin, cy1 + boomGap * cos, cx2 - (boomGap*0.5f) * sin, y + (size * 0.3f) * sin + (boomGap*0.5f) * cos, 2f)
        drawer.line(cx1 + boomGap * sin, cy1 - boomGap * cos, cx2 + (boomGap*0.5f) * sin, y + (size * 0.3f) * sin - (boomGap*0.5f) * cos, 2f)
    }

    private fun drawDreadnoughtHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        val halfL = size * 0.6f; val halfW = size * 0.4f
        val corners = arrayOf(Vector2(-halfL, -halfW), Vector2(halfL * 0.8f, -halfW), Vector2(halfL, -halfW * 0.5f), Vector2(halfL, halfW * 0.5f), Vector2(halfL * 0.8f, halfW), Vector2(-halfL, halfW))
        for (i in corners.indices) {
            val c1 = corners[i]; val c2 = corners[(i + 1) % corners.size]
            drawer.line(x + c1.x * cos - c1.y * sin, y + c1.x * sin + c1.y * cos, x + c2.x * cos - c2.y * sin, y + c2.x * sin + c2.y * cos, (size / 25f).coerceAtLeast(3f))
        }
        drawer.setColor(Color.DARK_GRAY)
        val plateX = x - (halfL * 0.3f) * cos; val plateY = y - (halfL * 0.3f) * sin
        drawer.line(plateX - halfW * sin, plateY + halfW * cos, plateX + halfW * sin, plateY - halfW * cos, 2.5f)
    }

    private fun drawSentinelPlusHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        drawer.filledCircle(x, y, size * 0.4f); drawer.circle(x, y, size * 0.7f, 3f)
        val wx1 = x + (size * 1.2f) * MathUtils.cos(rotation + 2.1f); val wy1 = y + (size * 1.2f) * MathUtils.sin(rotation + 2.1f)
        val wx2 = x + (size * 1.2f) * MathUtils.cos(rotation - 2.1f); val wy2 = y + (size * 1.2f) * MathUtils.sin(rotation - 2.1f)
        drawer.line(x + size * cos, y + size * sin, wx1, wy1, (size/10f).coerceAtLeast(2f))
        drawer.line(x + size * cos, y + size * sin, wx2, wy2, (size/10f).coerceAtLeast(2f))
    }

    private fun drawWraithHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float, color: Color) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        val tipX = x + size * 1.5f * cos; val tipY = y + size * 1.5f * sin
        val wx1 = x + size * 0.8f * MathUtils.cos(rotation + 2.5f); val wy1 = y + size * 0.8f * MathUtils.sin(rotation + 2.5f)
        val wx2 = x + size * 0.8f * MathUtils.cos(rotation - 2.5f); val wy2 = y + size * 0.8f * MathUtils.sin(rotation - 2.5f)
        drawer.line(tipX, tipY, wx1, wy1, 2f); drawer.line(tipX, tipY, wx2, wy2, 2f)
        drawer.setColor(color.r, color.g, color.b, 0.5f + 0.5f * MathUtils.sin(Gdx.graphics.frameId.toFloat() * 0.2f))
        drawer.filledCircle(x, y, size * 0.15f)
    }

    private fun drawStandardHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float, tier: Int) {
        val x1 = x + size * MathUtils.cos(rotation); val y1 = y + size * MathUtils.sin(rotation)
        val offset = if (tier >= 2) 2.2f else 2.4f
        val x2 = x + size * MathUtils.cos(rotation + offset); val y2 = y + size * MathUtils.sin(rotation + offset)
        val x3 = x + size * MathUtils.cos(rotation - offset); val y3 = y + size * MathUtils.sin(rotation - offset)
        drawer.line(x1, y1, x2, y2, 2f); drawer.line(x2, y2, x3, y3, 2f); drawer.line(x3, y3, x1, y1, 2f)
    }

    private fun drawBulwarkAttachments(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        val x2 = x + size * MathUtils.cos(rotation + 2.2f); val y2 = y + size * MathUtils.sin(rotation + 2.2f)
        val x3 = x + size * MathUtils.cos(rotation - 2.2f); val y3 = y + size * MathUtils.sin(rotation - 2.2f)
        drawer.line(x2, y2, x2 - (size * 0.3f) * cos, y2 - (size * 0.3f) * sin, 3f)
        drawer.line(x3, y3, x3 - (size * 0.3f) * cos, y3 - (size * 0.3f) * sin, 3f)
    }

    fun drawDrone(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, health: Float, maxHealth: Float, shield: Float) {
        if (shield > 0) {
            drawer.setColor(1f, 0.8f, 0f, 0.2f); drawer.filledCircle(x, y, 12f)
            drawer.setColor(Color.GOLD); drawer.circle(x, y, 12f, 1f)
        }
        val alpha = (health / maxHealth).coerceIn(0.3f, 1.0f)
        drawer.setColor(Color.GOLD.r, Color.GOLD.g, Color.GOLD.b, alpha)
        drawer.circle(x, y, 6f, 1.5f)
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1f)
    }

    fun drawStation(drawer: ShapeDrawer, x: Float, y: Float, radius: Float, skin: Skin, batch: com.badlogic.gdx.graphics.g2d.Batch) {
        // 1. Atmosphere/Field
        drawer.setColor(0.1f, 0.5f, 0.9f, 0.15f)
        drawer.filledCircle(x, y, radius)

        // 2. Structural Rings
        drawer.setColor(Color.CYAN)
        drawer.circle(x, y, radius, 4f)
        drawer.setColor(0.4f, 0.9f, 1f, 0.6f)
        drawer.circle(x, y, radius * 0.7f, 2f)

        // 3. Technical Core (Using specimen frame if available for "texture")
        if (skin.has("specimen-frame", com.badlogic.gdx.scenes.scene2d.utils.Drawable::class.java)) {
            val frame = skin.getDrawable("specimen-frame")
            val s = radius * 0.4f
            frame.draw(batch, x - s/2f, y - s/2f, s, s)
        } else {
            drawer.setColor(Color.WHITE)
            drawer.rectangle(x - 20f, y - 20f, 40f, 40f, 2f)
        }

        // 4. Power Core
        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, 15f)
    }
}
