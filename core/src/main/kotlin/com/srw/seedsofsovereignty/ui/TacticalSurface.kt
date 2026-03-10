package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Pools

/**
 * Technical Specification: TacticalSurface (v0.7.1)
 * ROLE: A non-centering touch area for precise persistent input.
 *
 * DESIGN PRINCIPLES:
 * 1. [ZERO SNAP] - Unlike a Touchpad, this actor never resets its value to (0,0) automatically.
 * 2. [NORMALIZED OUTPUT] - Outputs a Vector2 in the range of [-1, 1].
 * 3. [SNAP TO ORIGIN] - v0.7.1: Returns to (0,0) if input is within 10% of center.
 * 4. [ENGINE STABILITY] - No reflection required. Values are stored locally and
 *    persist across layout passes.
 */
class TacticalSurface(
    private val skin: Skin,
    styleName: String = "default"
) : Widget() {

    private val style: TacticalSurfaceStyle = skin.get(styleName, TacticalSurfaceStyle::class.java)

    // Internal state in normalized coordinates (-1 to 1)
    private var internalValue = Vector2(0f, 0f)

    // Deadzone for snapping to center (10% of radius)
    private val centerSnapThresholdSq = 0.01f

    /**
     * Restoration property to sync with ECS persistence on creation.
     */
    var positionRestore: Vector2? = null
        set(value) {
            field = value
            value?.let { internalValue.set(it) }
        }

    init {
        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                calculateAndFire(x, y)
                return true
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                calculateAndFire(x, y)
            }
        })
    }

    private fun calculateAndFire(x: Float, y: Float) {
        val bg = style.background
        val padL = bg.leftWidth
        val padR = bg.rightWidth
        val padB = bg.bottomHeight
        val padT = bg.topHeight

        val innerW = width - padL - padR
        val innerH = height - padB - padT

        // Normalize coordinates relative to center
        val centerX = padL + innerW / 2f
        val centerY = padB + innerH / 2f

        var nx = ((x - centerX) / (innerW / 2f)).coerceIn(-1f, 1f)
        var ny = ((y - centerY) / (innerH / 2f)).coerceIn(-1f, 1f)

        // [SNAP TO ORIGIN] - Return to center if close enough (0.1 threshold)
        if (nx * nx + ny * ny < centerSnapThresholdSq) {
            nx = 0f
            ny = 0f
        }

        if (internalValue.x != nx || internalValue.y != ny) {
            internalValue.set(nx, ny)
            val changeEvent = Pools.obtain(ChangeListener.ChangeEvent::class.java)
            fire(changeEvent)
            Pools.free(changeEvent)
        }
    }

    fun getValue(): Vector2 = internalValue

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val color = color
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        val x = x
        val y = y
        val width = width
        val height = height

        style.background.draw(batch, x, y, width, height)

        val reticle = style.reticle
        val bg = style.background
        val padL = bg.leftWidth
        val padR = bg.rightWidth
        val padB = bg.bottomHeight
        val padT = bg.topHeight

        val innerW = width - padL - padR
        val innerH = height - padB - padT
        val centerX = x + padL + innerW / 2f
        val centerY = y + padB + innerH / 2f

        val rx = centerX + (internalValue.x * (innerW / 2f)) - (reticle.minWidth / 2f)
        val ry = centerY + (internalValue.y * (innerH / 2f)) - (reticle.minHeight / 2f)

        reticle.draw(batch, rx, ry, reticle.minWidth, reticle.minHeight)
    }

    override fun getPrefWidth(): Float = style.background.minWidth
    override fun getPrefHeight(): Float = style.background.minHeight

    /**
     * Style definition for TacticalSurface.
     */
    class TacticalSurfaceStyle {
        lateinit var background: Drawable
        lateinit var reticle: Drawable
    }
}
