package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import space.earlygrey.shapedrawer.ShapeDrawer

object ProjectileDrawer {
    fun draw(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, tier: Int, scale: Float = 1f) {
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        when (tier) {
            1 -> {
                drawer.setColor(Color.YELLOW)
                drawer.line(x, y, x + (8f * scale) * cos, y + (8f * scale) * sin, 1.5f * scale)
            }
            2 -> {
                drawer.setColor(Color.VIOLET)
                drawer.line(x, y, x + (12f * scale) * cos, y + (12f * scale) * sin, 3f * scale)
            }
            3 -> {
                drawer.setColor(Color.GOLD)
                drawer.filledCircle(x, y, 3f * scale)
                drawer.line(x, y, x + (10f * scale) * cos, y + (10f * scale) * sin, 2f * scale)
            }
            4 -> {
                drawer.setColor(Color.ORANGE)
                drawer.filledCircle(x, y, 4f * scale)
                drawer.setColor(Color.WHITE)
                drawer.circle(x, y, 6f * scale, 1f * scale)
            }
            5 -> {
                drawer.setColor(Color.CHARTREUSE)
                val s = 5f * scale
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 2.5f * scale)
                drawer.line(x + s * cos + s * sin, y + s * sin - s * cos, x + s * cos - s * sin, y + s * sin + s * cos, 2.5f * scale)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 2.5f * scale)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 2.5f * scale)
            }
            6 -> {
                drawer.setColor(Color.SKY)
                val s = 6f * scale
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 3.5f * scale)
                drawer.line(x + s * cos + s * sin, y + s * sin - s * cos, x + s * cos - s * sin, y + s * sin + s * cos, 3.5f * scale)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 3.5f * scale)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 3.5f * scale)
            }
            7 -> {
                drawer.setColor(Color.PURPLE)
                drawer.line(x, y, x + (20f * scale) * cos, y + (20f * scale) * sin, 3f * scale)
                drawer.setColor(Color.WHITE)
                drawer.filledCircle(x, y, 3.5f * scale)
            }
            8 -> {
                drawer.setColor(Color.LIGHT_GRAY)
                drawer.line(x, y, x + (25f * scale) * cos, y + (25f * scale) * sin, 6f * scale)
                drawer.setColor(Color.WHITE)
                drawer.line(x + (15f * scale) * cos, y + (15f * scale) * sin, x + (25f * scale) * cos, y + (25f * scale) * sin, 3f * scale)
            }
            9 -> {
                drawer.setColor(Color.FIREBRICK)
                drawer.line(x, y, x + (35f * scale) * cos, y + (35f * scale) * sin, 10f * scale)
                drawer.setColor(Color.WHITE)
                drawer.line(x + (15f * scale) * cos, y + (15f * scale) * sin, x + (30f * scale) * cos, y + (30f * scale) * sin, 4f * scale)
            }
            else -> {
                drawer.setColor(Color.WHITE)
                drawer.line(x, y, x + (8f * scale) * cos, y + (8f * scale) * sin, 1.5f * scale)
            }
        }
    }
}
