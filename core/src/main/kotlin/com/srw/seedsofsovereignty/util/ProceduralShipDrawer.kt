package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import space.earlygrey.shapedrawer.ShapeDrawer

object ProceduralShipDrawer {

    fun getShipSize(tier: Int, subTier: Int): Float {
        val tierBase = 8f + (tier - 1) * 7.5f
        return tierBase + (subTier - 1) * 1.875f
    }

    /**
     * Returns a list of relative offsets for weapon mounts based on tier and size.
     */
    fun getMountOffsets(tier: Int, subTier: Int, size: Float, hullRotation: Float, out: MutableList<Vector2>) {
        out.clear()
        val cos = MathUtils.cos(hullRotation)
        val sin = MathUtils.sin(hullRotation)

        if (tier < 8) {
            // Single central mount for lower tiers
            out.add(Vector2(0f, 0f))
        } else {
            // Dreadnought distributed mounts: 1 per sub-tier
            // Pattern: Square-ish formation
            val spread = size * 0.4f
            val count = subTier.coerceIn(1, 4)

            // Local positions for 4 mounts
            val localPositions = arrayOf(
                Vector2(spread, spread),   // Front Right
                Vector2(spread, -spread),  // Front Left
                Vector2(-spread, -spread), // Back Left
                Vector2(-spread, spread)   // Back Right
            )

            for (i in 0 until count) {
                val lp = localPositions[i]
                // Rotate local position by hull rotation
                val rx = lp.x * cos - lp.y * sin
                val ry = lp.x * sin + lp.y * cos
                out.add(Vector2(rx, ry))
            }
        }
    }

    fun drawPlayerShip(drawer: ShapeDrawer, x: Float, y: Float, hullRotation: Float, aimRotation: Float, tier: Int, subTier: Int) {
        val size = getShipSize(tier, subTier)

        // 1. Draw the Hull (Rotates with movement)
        val color = when(tier) {
            1 -> Color.CYAN; 2 -> Color.VIOLET; 3 -> Color.GOLD; 4 -> Color.ORANGE
            5 -> Color.CHARTREUSE; 6 -> Color.SKY; 7 -> Color.PURPLE; 8 -> Color.SLATE
            else -> Color.CYAN
        }
        drawer.setColor(color)

        when (tier) {
            8 -> drawDreadnoughtHull(drawer, x, y, hullRotation, size)
            6 -> drawSentinelPlusHull(drawer, x, y, hullRotation, size)
            7 -> drawWraithHull(drawer, x, y, hullRotation, size, color)
            else -> drawStandardHull(drawer, x, y, hullRotation, size, tier)
        }

        if (tier == 5) drawBulwarkAttachments(drawer, x, y, hullRotation, size)

        // 2. Draw Weapon Mounts (Rotate with aim)
        val mountSize = if (tier >= 8) 12f else size * 0.6f
        val offsets = mutableListOf<Vector2>()
        getMountOffsets(tier, subTier, size, hullRotation, offsets)

        offsets.forEach { offset ->
            drawWeaponMount(drawer, x + offset.x, y + offset.y, aimRotation, mountSize, color, tier, subTier)
        }

        // Hull Core
        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, (size * 0.1f).coerceAtLeast(1.5f))
    }

    private fun drawWeaponMount(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float, color: Color, tier: Int, subTier: Int) {
        // Draw the "Mount Ring"
        drawer.setColor(Color.DARK_GRAY)
        drawer.circle(x, y, size, 2f)

        drawer.setColor(color)
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        // Draw Barrels based on sub-tier logic
        val numBarrels = if (tier >= 8) 1 else (when(subTier) { in 1..2 -> 1; 3 -> 2; else -> 3 })
        val barrelL = size * 1.5f
        val barrelW = (size * 0.4f).coerceAtLeast(2f)
        val spacing = size * 0.6f

        for (i in 0 until numBarrels) {
            val offset = (i - (numBarrels - 1) * 0.5f) * spacing
            val bx = x - offset * sin
            val by = y + offset * cos
            drawer.line(bx, by, bx + barrelL * cos, by + barrelL * sin, barrelW)
        }
    }

    private fun drawDreadnoughtHull(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        val halfL = size * 0.6f; val halfW = size * 0.4f
        val corners = arrayOf(Vector2(-halfL, -halfW), Vector2(halfL * 0.8f, -halfW), Vector2(halfL, -halfW * 0.5f), Vector2(halfL, halfW * 0.5f), Vector2(halfL * 0.8f, halfW), Vector2(-halfL, halfW))
        for (i in corners.indices) {
            val c1 = corners[i]; val c2 = corners[(i + 1) % corners.size]
            drawer.line(x + c1.x * cos - c1.y * sin, y + c1.x * sin + c1.y * cos, x + c2.x * cos - c2.y * sin, y + c2.x * sin + c2.y * cos, (size / 15f).coerceAtLeast(2f))
        }
        drawer.setColor(Color.DARK_GRAY)
        val plateX = x - (halfL * 0.3f) * cos; val plateY = y - (halfL * 0.3f) * sin
        drawer.line(plateX - halfW * sin, plateY + halfW * cos, plateX + halfW * sin, plateY - halfW * cos, 1.5f)
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
            drawer.setColor(1f, 0.8f, 0f, 0.2f)
            drawer.filledCircle(x, y, 12f)
            drawer.setColor(Color.GOLD)
            drawer.circle(x, y, 12f, 1f)
        }

        val alpha = (health / maxHealth).coerceIn(0.3f, 1.0f)
        drawer.setColor(Color.GOLD.r, Color.GOLD.g, Color.GOLD.b, alpha)
        drawer.circle(x, y, 6f, 1.5f)

        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1f)
    }
}
