package com.srw.seedofsov.ui.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.HudFxComponent
import com.srw.seedofsov.ecs.components.ShieldModuleComponent
import com.srw.seedofsov.model.DataChannel
import com.srw.seedofsov.model.ShipModule

/**
 * Interactive area that maps touch coordinates to a DataChannel.
 * Truth on Disk: Decoupled input widget that drives external HudFx systems with solid "Induction Plate" manifolds.
 */
class Touchpad(
    val game: SovereigntyGame,
    val channel: DataChannel,
    var module: ShipModule? = null,
    var hudFx: HudFxComponent? = null
) : Widget() {

    private val handlePos = Vector2(0.5f, 0.5f)
    private val knobSize = 32f
    private val coreSize = 120f
    private val terminalWidth = 220f
    private val terminalHeight = 48f
    private val tempColor = Color()

    init {
        updateHandleFromChannel()

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                updatePosition(x, y)
                return true
            }
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updatePosition(x, y)
            }
        })
    }

    private fun updateHandleFromChannel() {
        handlePos.set(
            (channel.valueVector2.x + 1f) / 2f,
            (channel.valueVector2.y + 1f) / 2f
        )
    }

    override fun act(delta: Float) {
        super.act(delta)
        updateHandleFromChannel()

        hudFx?.let { fx ->
            val centerPos = localToStageCoordinates(Vector2(width/2f, height/2f))
            fx.screenPosition.set(centerPos)

            val knobStagePos = localToStageCoordinates(Vector2(handlePos.x * width, handlePos.y * height))
            fx.targetPosition.set(knobStagePos)

            val stageBottomLeft = localToStageCoordinates(Vector2(0f, 0f))
            fx.clipBounds.set(stageBottomLeft.x, stageBottomLeft.y, width, height)

            val cx = handlePos.x * 2f - 1f
            val cy = handlePos.y * 2f - 1f

            val dist = Vector2.len(cx, cy)
            if (dist < 0.15f) {
                for (i in 0..3) fx.weights[i] = 0.25f
            } else {
                fx.weights[0] = Math.max(0f, cy)  // Top
                fx.weights[1] = Math.max(0f, -cx) // Left
                fx.weights[2] = Math.max(0f, -cy) // Bottom
                fx.weights[3] = Math.max(0f, cx)  // Right
                val total = fx.weights[0] + fx.weights[1] + fx.weights[2] + fx.weights[3]
                if (total > 0f) {
                    for (i in 0..3) fx.weights[i] /= total
                }
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!game.uiFactory.isLoaded) return
        val skin = game.uiFactory.skin
        val pixel = skin.getRegion("button-up")

        // 1. Ghostly Grid Background
        batch.color = tempColor.set(0.01f, 0.02f, 0.05f, 0.1f * parentAlpha)
        batch.draw(pixel, x, y, width, height)

        if (module is ShieldModuleComponent) {
            // 2. Technical Structural Dividers
            batch.color = tempColor.set(0.2f, 0.4f, 0.6f, 0.15f * parentAlpha)
            batch.draw(pixel, x + width/2f - 1f, y, 2f, height)
            batch.draw(pixel, x, y + height/2f - 1f, width, 2f)

            // 3. THE POWER CORE (Central Anchor)
            batch.color = tempColor.set(0.4f, 0.8f, 1.0f, 0.4f * parentAlpha)
            batch.draw(pixel, x + width/2f - coreSize/2f, y + height/2f - coreSize/2f, coreSize, 2f)
            batch.draw(pixel, x + width/2f - coreSize/2f, y + height/2f + coreSize/2f, coreSize, 2f)
            batch.draw(pixel, x + width/2f - coreSize/2f, y + height/2f - coreSize/2f, 2f, coreSize)
            batch.draw(pixel, x + width/2f + coreSize/2f, y + height/2f - coreSize/2f, 2f, coreSize)

            // 4. THE REINFORCED ABSORBER TERMINALS
            drawTerminal(batch, pixel, x + width/2f, y + height - 25f, true, hudFx?.weights?.get(0) ?: 0.25f, parentAlpha) // Top
            drawTerminal(batch, pixel, x + 25f, y + height/2f, false, hudFx?.weights?.get(1) ?: 0.25f, parentAlpha) // Left
            drawTerminal(batch, pixel, x + width/2f, y + 25f, true, hudFx?.weights?.get(2) ?: 0.25f, parentAlpha) // Bottom
            drawTerminal(batch, pixel, x + width - 25f, y + height/2f, false, hudFx?.weights?.get(3) ?: 0.25f, parentAlpha) // Right
        }

        // 5. Interaction Handle (The Knob)
        val hx = x + (handlePos.x * width)
        val hy = y + (handlePos.y * height)
        batch.color = Color(game.uiFactory.style.palette.highlight).apply { a *= parentAlpha }
        batch.draw(pixel, hx - knobSize/2f, hy - knobSize/2f, knobSize, knobSize)
        batch.color = Color.WHITE
    }

    private fun drawTerminal(batch: Batch, pixel: com.badlogic.gdx.graphics.g2d.TextureRegion, cx: Float, cy: Float, isHorizontal: Boolean, weight: Float, alpha: Float) {
        val intensity = 0.2f + (weight * 0.8f)
        val overdrive = (weight - 0.25f).coerceAtLeast(0f) * 1.33f

        val w = if (isHorizontal) terminalWidth else terminalHeight
        val h = if (isHorizontal) terminalHeight else terminalWidth
        val hw = w / 2f
        val hh = h / 2f

        // Truth on Disk: Heavy Industrial Induction Plate Design

        // 1. SOLID CHASSIS BASE (Metal Plate)
        tempColor.set(0.05f, 0.15f, 0.3f, 0.6f * alpha)
        batch.color = tempColor
        batch.draw(pixel, cx - hw, cy - hh, w, h)

        // 2. BOLTED FRAME (Bold Silhouette)
        tempColor.set(0.2f, 0.5f, 0.9f, (0.4f + overdrive * 0.6f) * alpha)
        batch.color = tempColor
        // Far outer edges
        if (isHorizontal) {
            batch.draw(pixel, cx - hw, cy - hh, w, 3f)
            batch.draw(pixel, cx - hw, cy + hh - 3f, w, 3f)
            batch.draw(pixel, cx - hw, cy - hh, 3f, h)
            batch.draw(pixel, cx + hw - 3f, cy - hh, 3f, h)
        } else {
            batch.draw(pixel, cx - hw, cy - hh, 3f, h)
            batch.draw(pixel, cx + hw - 3f, cy - hh, 3f, h)
            batch.draw(pixel, cx - hw, cy - hh, w, 3f)
            batch.draw(pixel, cx - hw, cy + hh - 3f, w, 3f)
        }

        // 3. INTERNAL VENT / INDUCTOR MESH
        tempColor.set(0.4f, 0.8f, 1.0f, (0.1f + overdrive * 0.4f) * alpha)
        batch.color = tempColor
        val meshCount = 10
        val meshSpacing = (if (isHorizontal) w - 20f else h - 20f) / meshCount
        for (i in 0..meshCount) {
            val offset = (i * meshSpacing) - (if (isHorizontal) (w - 20f) / 2f else (h - 20f) / 2f)
            if (isHorizontal) {
                batch.draw(pixel, cx + offset - 1f, cy - hh + 6f, 2f, h - 12f)
            } else {
                batch.draw(pixel, cx - hw + 6f, cy + offset - 1f, w - 12f, 2f)
            }
        }

        // 4. ACTIVE OVERDRIVE GLOW (The Energy Process)
        if (weight > 0.3f) {
            tempColor.set(Color.WHITE).lerp(Color.SKY, 0.5f).apply { a = overdrive * 0.8f * alpha }
            batch.color = tempColor
            // Solid center glow for active plates
            val glowW = if (isHorizontal) w - 12f else w - 8f
            val glowH = if (isHorizontal) h - 8f else h - 12f
            batch.draw(pixel, cx - glowW / 2f, cy - glowH / 2f, glowW, glowH)
        }

        // 5. CONDUCTIVE BUSBARS (The Internal Core)
        tempColor.set(Color.SKY).lerp(Color.WHITE, overdrive).apply { a = intensity * alpha }
        batch.color = tempColor
        for (railOffset in listOf(-10f, 0f, 10f)) {
            val railW = if (isHorizontal) w - 30f else 3f
            val railH = if (isHorizontal) 3f else h - 30f
            val rx = if (isHorizontal) cx - (w-30f)/2f else cx + railOffset - 1.5f
            val ry = if (isHorizontal) cy + railOffset - 1.5f else cy - (h-30f)/2f
            batch.draw(pixel, rx, ry, railW, railH)
        }

        // 6. END-CAP HARDWARE (Piston / Mounting Brackets)
        tempColor.set(0.6f, 0.9f, 1.0f, 0.9f * alpha)
        batch.color = tempColor
        val bracketW = if (isHorizontal) 14f else 32f
        val bracketH = if (isHorizontal) 32f else 14f
        if (isHorizontal) {
            batch.draw(pixel, cx - hw - 6f, cy - bracketH / 2f, 6f, bracketH) // Left
            batch.draw(pixel, cx + hw, cy - bracketH / 2f, 6f, bracketH) // Right
        } else {
            batch.draw(pixel, cx - bracketW / 2f, cy - hh - 6f, bracketW, 6f) // Bottom
            batch.draw(pixel, cx - bracketW / 2f, cy + hh, bracketW, 6f) // Top
        }
    }

    private fun updatePosition(x: Float, y: Float) {
        var nx = (x / width).coerceIn(0f, 1f)
        var ny = (y / height).coerceIn(0f, 1f)
        val cx = nx * 2f - 1f
        val cy = ny * 2f - 1f
        if (Vector2.len(cx, cy) < 0.18f) {
            nx = 0.5f; ny = 0.5f; channel.valueVector2.set(0f, 0f)
        } else {
            channel.valueVector2.set(cx, cy)
        }
        handlePos.set(nx, ny)
    }
}
