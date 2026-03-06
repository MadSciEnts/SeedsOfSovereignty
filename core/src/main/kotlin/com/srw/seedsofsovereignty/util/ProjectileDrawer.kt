package com.srw.seedsofsovereignty.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import space.earlygrey.shapedrawer.ShapeDrawer

object ProjectileDrawer {
    fun draw(drawer: ShapeDrawer, x: Float, y: Float, rotation: Float, tier: Int) {
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)
        when (tier) {
            1 -> {
                drawer.setColor(Color.YELLOW)
                drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1.5f)
            }
            2 -> {
                drawer.setColor(Color.VIOLET)
                drawer.line(x, y, x + 12f * cos, y + 12f * sin, 3f)
            }
            3 -> {
                drawer.setColor(Color.GOLD)
                drawer.filledCircle(x, y, 3f)
                drawer.line(x, y, x + 10f * cos, y + 10f * sin, 2f)
            }
            4 -> {
                drawer.setColor(Color.ORANGE)
                drawer.filledCircle(x, y, 4f)
                drawer.setColor(Color.WHITE)
                drawer.circle(x, y, 6f, 1f)
            }
            5 -> {
                drawer.setColor(Color.CHARTREUSE)
                val s = 5f
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 2.5f)
                drawer.line(x + s * cos + s * sin, y + s * sin - s*cos, x + s * cos - s * sin, y + s * sin + s * cos, 2.5f)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 2.5f)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 2.5f)
            }
            6 -> {
                drawer.setColor(Color.SKY)
                val s = 6f
                drawer.line(x - s * cos + s * sin, y - s * sin - s * cos, x + s * cos + s * sin, y + s * sin - s * cos, 3.5f)
                drawer.line(x + s * cos + s * sin, y + s * sin - s * cos, x + s * cos - s * sin, y + s * sin + s * cos, 3.5f)
                drawer.line(x + s * cos - s * sin, y + s * sin + s * cos, x - s * cos - s * sin, y - s * sin + s * cos, 3.5f)
                drawer.line(x - s * cos - s * sin, y - s * sin + s * cos, x - s * cos + s * sin, y - s * sin - s * cos, 3.5f)
            }
            7 -> {
                drawer.setColor(Color.PURPLE)
                drawer.line(x, y, x + 20f * cos, y + 20f * sin, 3f)
                drawer.setColor(Color.WHITE)
                drawer.filledCircle(x, y, 3.5f)
            }
            8 -> {
                drawer.setColor(Color.LIGHT_GRAY)
                drawer.line(x, y, x + 25f * cos, y + 25f * sin, 6f)
                drawer.setColor(Color.WHITE)
                drawer.line(x + 15f * cos, y + 15f * sin, x + 25f * cos, y + 25f * sin, 3f)
            }
            9 -> {
                drawer.setColor(Color.FIREBRICK)
                drawer.line(x, y, x + 35f * cos, y + 35f * sin, 10f)
                drawer.setColor(Color.WHITE)
                drawer.line(x + 15f * cos, y + 15f * sin, x + 30f * cos, y + 30f * sin, 4f)
            }
            else -> {
                drawer.setColor(Color.WHITE)
                drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1.5f)
            }
        }
    }
}
