package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.HudFxType
import com.srw.seedofsov.ui.hud.Hud
import ktx.ashley.*

/**
 * Bridges ECS state to the isolated HudFxLayer in the UI.
 * Truth on Disk: Orchestrates high-fidelity "Wide-Flow" energy manifolds with additive brightness.
 */
class HudFxSystem(private val hud: Hud) : IteratingSystem(allOf(HudFxComponent::class).get(), 48) {

    private val tempVec = Vector2()
    private val perpVec = Vector2()
    private val emitPos = Vector2()


    //These are the shield panel colors
    val shield_col_src = Color(0.1f, 0.5f, 1.0f, 0.95f)
    val shield_col_dst = Color(0f, 0.9f, 0.2f, .05f)

    //this is the core color
    val core_col_src = Color(0.1f, 0.3f, 1f, 0f)
    val core_col_dst1 = Color(0.1f, 0.3f, 0f, 1f)
    val core_col_dst2 = Color(0.1f, 0.0f, 0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val hudFx = entity[HudFxComponent.mapper] ?: return

        when (hudFx.type) {
            HudFxType.SHIELD_MANIFOLD -> {
                updatePowerCore(entity, hudFx)
                updateShieldManifold(entity, hudFx)
            }
            else -> {}
        }
    }

    private fun updatePowerCore(parent: Entity, hudFx: HudFxComponent) {
        val parentId = parent.hashCode()
        val bounds = hudFx.clipBounds
        val terminalWidth = 220f // Matches Touchpad.kt structural width


        for (i in 0..3) {
            val weight = hudFx.weights[i]
            if (weight <= 0.05f) continue

            val overdrive = MathUtils.clamp((weight - 0.25f) / 0.75f, 0f, 1f)
            val angle = when(i) { 0 -> 90f; 1 -> 180f; 2 -> 270f; else -> 0f }

            // Perpendicular vector for wide emission (up/down if horiz, left/right if vert)
            perpVec.set(MathUtils.cosDeg(angle + 90f), MathUtils.sinDeg(angle + 90f))

            // Calculate terminal position (the "Absorber Block")
            tempVec.set(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle)).scl(bounds.width / 2.2f).add(hudFx.screenPosition)
            val terminalPos = Vector2(tempVec)

            // 1. THE OUTWARD SURGE (Lightning / Supply)
            val surgeKey = "surge_${parentId}_$i"
            val surge = hud.fxLayer.getRibbon(surgeKey) { r ->
                r.maxSegments = 160
                r.targetColor = core_col_src
            }
            surge.origin.set(hudFx.screenPosition)
            surge.clipBounds = bounds
            surge.magneticTarget = terminalPos
            surge.magneticStrength = -.5f
            surge.dissipationRate = MathUtils.lerp(0.6f, 0.05f, overdrive)
            surge.noiseScale = MathUtils.lerp(1.5f, 4.0f, overdrive)
            surge.color.set(core_col_dst1).lerp(core_col_dst2, overdrive)//.apply { a = .4f }

            // 2. THE WIDE INWARD SETTLEMENT (Inverse / Collecting)
            // Emits across the entire width of the terminal bracket
            val settleKey = "settle_${parentId}_$i"
            val settle = hud.fxLayer.getRibbon(settleKey) { r ->
                r.maxSegments = 256
                r.targetColor = shield_col_dst
                r.color = shield_col_src
            }
            settle.clipBounds = bounds
            settle.magneticTarget = hudFx.screenPosition
            settle.magneticStrength = 9.2f
            settle.dissipationRate = 3.0f
            settle.noiseScale = 1.5f

            // Emission Logic
            val surgeCount = (weight * 8).toInt().coerceAtLeast(1)
            val settleCount = (weight * 10).toInt().coerceAtLeast(2)

            repeat(surgeCount) {
                val scatter = angle + MathUtils.random(-12f, 12f)
                val velocity = MathUtils.lerp(2.0f, 6.0f, overdrive)
                surge.emit(MathUtils.cosDeg(scatter) * velocity, MathUtils.sinDeg(scatter) * velocity)
            }

            repeat(settleCount) {
                // Randomize origin along the wide manifold bracket
                val offset = MathUtils.random(-terminalWidth/2.2f, terminalWidth/2.2f)
                emitPos.set(terminalPos).add(perpVec.x * offset, perpVec.y * offset)

                settle.origin.set(emitPos)
                val scatter = (angle + 180f) + MathUtils.random(-25f, 25f)
                settle.emit(MathUtils.cosDeg(scatter) * 0.7f, MathUtils.sinDeg(scatter) * 0.7f)
            }
        }
    }

    private fun updateShieldManifold(parent: Entity, hudFx: HudFxComponent) {
        val parentId = parent.hashCode()
        tempVec.set(hudFx.targetPosition).sub(hudFx.screenPosition)
        val distToTarget = tempVec.len()

        if (distToTarget > 20f) {
            val surgeKey = "interact_${parentId}"
            val ribbon = hud.fxLayer.getRibbon(surgeKey) { r ->
                r.maxSegments = 256
                r.dissipationRate = .7f
                r.color = Color.CYAN.cpy().apply { a = 0.5f }
                r.targetColor = Color.SKY.cpy().apply { a = 0.1f }
                r.noiseScale = 1.2f
            }

            ribbon.origin.set(hudFx.screenPosition)
            ribbon.clipBounds = hudFx.clipBounds
            ribbon.magneticTarget = hudFx.targetPosition
            ribbon.magneticStrength = 3.0f

            repeat(4) {
                val scatter = MathUtils.random(360f)
                ribbon.emit(MathUtils.cosDeg(scatter) * 1.5f, MathUtils.sinDeg(scatter) * 1.5f)
            }
        }
    }
}
