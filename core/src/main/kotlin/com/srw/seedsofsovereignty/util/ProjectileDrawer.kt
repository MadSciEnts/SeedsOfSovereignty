package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Projectile Visuals (v2.7.5).
 * FIXES:
 * 1. [GSF 0.1] - Scaled projectile lengths and widths.
 */
object ProjectileDrawer {
    fun draw(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, tier: Int, color: Color, scale: Float = 1f) {
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        drawer.setColor(color)

        // GSF 0.1: All lengths reduced by 10x
        when (tier) {
            1 -> {
                drawer.line(x, y, x + (0.8f * scale) * cos, y + (0.8f * scale) * sin, 0.15f * scale)
            }
            2 -> {
                drawer.line(x, y, x + (1.2f * scale) * cos, y + (1.2f * scale) * sin, 0.3f * scale)
            }
            3 -> {
                drawer.filledCircle(x, y, 0.3f * scale)
                drawer.line(x, y, x + (1.0f * scale) * cos, y + (1.0f * scale) * sin, 0.2f * scale)
            }
            4 -> {
                drawer.filledCircle(x, y, 0.4f * scale)
                drawer.setColor(1f, 1f, 1f, color.a)
                drawer.circle(x, y, 0.6f * scale, 0.1f * scale)
            }
            5 -> {
                val s = 0.5f * scale
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 0.25f * scale)
                drawer.line(x + s * cos + s * sin, y + s * sin - s * cos, x + s * cos - s * sin, y + s * sin + s * cos, 0.25f * scale)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 0.25f * scale)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 0.25f * scale)
            }
            6 -> {
                val s = 0.6f * scale
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 0.35f * scale)
                drawer.line(x + s * cos + s * sin, y + s * sin - s * cos, x + s * cos - s * sin, y + s * sin + s * cos, 0.35f * scale)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 0.35f * scale)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 0.35f * scale)
            }
            7 -> {
                drawer.line(x, y, x + (2.0f * scale) * cos, y + (2.0f * scale) * sin, 0.3f * scale)
                drawer.setColor(1f, 1f, 1f, color.a)
                drawer.filledCircle(x, y, 0.35f * scale)
            }
            8 -> {
                drawer.line(x, y, x + (2.5f * scale) * cos, y + (2.5f * scale) * sin, 0.6f * scale)
                drawer.setColor(1f, 1f, 1f, color.a)
                drawer.line(x + (1.5f * scale) * cos, y + (1.5f * scale) * sin, x + (2.5f * scale) * cos, y + (2.5f * scale) * sin, 0.3f * scale)
            }
            9 -> {
                drawer.line(x, y, x + (3.5f * scale) * cos, y + (3.5f * scale) * sin, 1.0f * scale)
                drawer.setColor(1f, 1f, 1f, color.a)
                drawer.line(x + (1.5f * scale) * cos, y + (1.5f * scale) * sin, x + (3.0f * scale) * cos, y + (3.0f * scale) * sin, 0.4f * scale)
            }
            else -> {
                drawer.line(x, y, x + (0.8f * scale) * cos, y + (0.8f * scale) * sin, 0.15f * scale)
            }
        }
    }
}
