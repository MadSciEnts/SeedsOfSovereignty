package com.srw.seedofsov.ui.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.assets.TextureRegistry
import com.srw.seedofsov.model.*

/**
 * Modularized renderer for Tactical Overlay elements.
 * Truth on Disk: Visualizes directional shield health and cooldown/recharge states.
 * Feature: Cardinal alignment (0: Front, 1: Left, 2: Rear, 3: Right) with hull rotation sync.
 */
class TacticalRenderer(private val batch: Batch) {

    private val tempVec = Vector2()
    private val hubPos = Vector2()
    private val layerPos = Vector2()
    private val extrusionVec = Vector2()
    private val orbitPoint = Vector2()
    private val ringVertices = FloatArray(20)
    private var stateTime = 0f

    private val plasmaColors = arrayOf(Color.GOLD, Color.CYAN, Color.PURPLE, Color.WHITE)

    fun update(dt: Float) {
        stateTime += dt
    }

    fun drawModuleHUD(module: ShipModule, shipPos: Vector2, shipRadius: Float, shipRotation: Float = 0f) {
        val state = module.tacticalState
        if (state.layoutOrbitIndex < 0) return

        val attributes = module.getTacticalAttributes()
        val idleRadius = TacticalHUDConstants.getRingRadius(shipRadius, state.layoutOrbitIndex)

        val bloomScale = 1.0f + (state.firePulse * 0.25f)
        val projectedRadius = state.currentRadius * bloomScale
        val projectedRotation = state.currentRotation

        val jitterX = if (state.firePulse > 0.1f) MathUtils.random(-0.05f, 0.05f) * state.firePulse else 0f
        val jitterY = if (state.firePulse > 0.1f) MathUtils.random(-0.05f, 0.05f) * state.firePulse else 0f
        hubPos.set(state.worldTargetPos.x + jitterX, state.worldTargetPos.y + jitterY)

        val alphaBase = if (state.isSelected) 0.5f + (0.35f * state.selectionAnimation) else 0.35f
        val baseColor = getCategoryColor(module.category)

        val isActivelyProjecting = state.aimTransition > 0.01f

        if (isActivelyProjecting) {
            val projectionAlpha = (alphaBase + state.firePulse * 0.4f) * state.aimTransition
            val rotOuter = (stateTime * TacticalHUDConstants.ROTATION_SPEED_OUTER) % 360f

            batch.color = baseColor.cpy().apply { a = projectionAlpha * 0.15f }
            drawPolygonRing(hubPos, state.lastStableAwayAngle, projectedRadius + 0.2f, state.heightSquish, 360f, rotOuter, 0.05f, 64)
            drawCalibrationTicks(hubPos, state.lastStableAwayAngle, projectedRadius + 0.2f, state.heightSquish, rotOuter, baseColor, projectionAlpha * 0.4f)

            batch.color = baseColor.cpy().apply { a = projectionAlpha * (0.2f + state.firePulse * 0.3f) }
            val segments = if (module.category == ModuleCategory.SHIELD) TacticalHUDConstants.HEX_PATTERN_DENSITY else 16
            drawVolumetricRing(hubPos, state.lastStableAwayAngle, projectedRadius, state.heightSquish, 360f, projectedRotation, TacticalHUDConstants.RING_VISUAL_DEPTH, segments)

            if (attributes.any { it.label == "AIM" }) {
                drawAimCorridor(shipPos, hubPos, state.lastStableAwayAngle, projectedRadius, state.heightSquish, state.aimTransition, baseColor, alphaBase + state.firePulse * 0.5f)
            }
        } else {
            val pulse = (MathUtils.sin(stateTime * 2f) + 1f) * 0.5f
            batch.color = baseColor.cpy().apply { a = alphaBase * (0.2f + 0.2f * pulse) }
            drawPolygonRing(shipPos, state.lastStableAwayAngle, idleRadius, 1.0f, 360f, projectedRotation, 0.1f, 64)
        }

        attributes.forEach { attr ->
            if (attr.label == "AIM") return@forEach
            val isProjected = attr.projection == TacticalProjection.PROJECTED
            val targetPos = if (isProjected) hubPos else shipPos
            val targetAngle = if (isProjected) state.lastStableAwayAngle else 90f
            val targetSquish = if (isProjected) state.heightSquish else 1.0f
            val attrRadius = if (isProjected) projectedRadius else idleRadius
            val attrRotation = if (isProjected) projectedRotation else 0f
            val attrColor = attr.colorOverride ?: baseColor

            when (attr.type) {
                TacticalAttributeType.CONTINUOUS -> drawContinuousAttr(targetPos, targetAngle, attrRadius, targetSquish, attrRotation, attr, attrColor, alphaBase, shipRotation)
                TacticalAttributeType.DISCRETE -> drawDiscreteAttr(targetPos, targetAngle, attrRadius, targetSquish, attrRotation, attr, attrColor, alphaBase)
                else -> {}
            }
        }
        batch.color = Color.WHITE
    }

    private fun drawCalibrationTicks(pos: Vector2, angle: Float, radius: Float, squish: Float, rotation: Float, color: Color, alpha: Float) {
        val pixel = TextureRegistry.voidTexture
        batch.color = color.cpy().apply { a = alpha }
        val tickCount = 12
        val step = 360f / tickCount
        for (i in 0 until tickCount) {
            val tickAngle = rotation + (i * step)
            orbitPoint.set(0f, radius).rotateDeg(tickAngle)
            orbitPoint.scl(1.0f, squish)
            orbitPoint.rotateDeg(angle - 90f)
            val wx = pos.x + orbitPoint.x
            val wy = pos.y + orbitPoint.y
            tempVec.set(-MathUtils.sinDeg(tickAngle) * squish, MathUtils.cosDeg(tickAngle)).nor()
            tempVec.rotateDeg(angle - 90f)
            val segmentRot = tempVec.angleDeg() - 90f
            batch.draw(pixel, wx - TacticalHUDConstants.TICK_THICKNESS/2f, wy - TacticalHUDConstants.TICK_RADIAL_LENGTH/2f,
                TacticalHUDConstants.TICK_THICKNESS/2f, TacticalHUDConstants.TICK_RADIAL_LENGTH/2f,
                TacticalHUDConstants.TICK_THICKNESS, TacticalHUDConstants.TICK_RADIAL_LENGTH,
                1f, 1f, segmentRot, 0, 0, 1, 1, false, false)
        }
    }

    private fun drawContinuousAttr(pos: Vector2, angle: Float, radius: Float, squish: Float, rotation: Float, attr: TacticalAttribute, color: Color, alpha: Float, shipRotation: Float = 0f) {
        val progress = MathUtils.clamp(attr.current / attr.max, 0f, 1f)
        val arcSpan = 80f

        // Truth on Disk: Aligned to cardinal quadrants (0: Front/Top, 1: Left, 2: Rear, 3: Right).
        // 0 index is Top (0° in this local-up system). CCW order matches the ship system.
        val arcStart = (attr.preferredQuadrant * 90f) - (arcSpan / 2f) + rotation + shipRotation

        val isShield = attr.label.startsWith("QUAD_")
        val isCharging = isShield && color.equals(Color.GOLD)

        // 1. Draw "Ghost" Background
        batch.color = color.cpy().apply { a = alpha * 0.1f }
        drawPolygonRing(pos, angle, radius, squish, arcSpan, arcStart, TacticalHUDConstants.RING_RADIAL_THICKNESS * 0.4f, 32)

        // 2. Draw Current Value
        var drawColor = color
        var drawAlpha = alpha * 0.9f
        var thicknessMult = 0.6f

        if (isCharging) {
            val roll = MathUtils.random()
            if (roll > 0.8f) {
                drawColor = plasmaColors[MathUtils.random(plasmaColors.size - 1)]
                drawAlpha *= 0.4f
            } else if (roll > 0.6f) {
                drawAlpha *= 1.2f
            }
            thicknessMult = 0.4f
        }

        batch.color = drawColor.cpy().apply { a = drawAlpha }
        drawPolygonRing(pos, angle, radius, squish, arcSpan * progress, arcStart, TacticalHUDConstants.RING_RADIAL_THICKNESS * thicknessMult, 32)
    }

    private fun drawDiscreteAttr(pos: Vector2, angle: Float, radius: Float, squish: Float, rotation: Float, attr: TacticalAttribute, color: Color, alpha: Float) {
        val count = attr.current.toInt()
        val max = attr.max.toInt()
        val arcSpan = 80f
        val arcStart = (attr.preferredQuadrant * 90f) + 5f + rotation
        val step = arcSpan / max.coerceAtLeast(1)
        for (i in 0 until max) {
            val isFilled = i < count
            batch.color = color.cpy().apply { a = if (isFilled) alpha * 0.7f else alpha * 0.2f }
            drawPolygonRing(pos, angle, radius, squish, step * 0.8f, arcStart + (i * step), TacticalHUDConstants.RING_RADIAL_THICKNESS * 0.8f, 8)
        }
    }

    private fun drawAimCorridor(shipPos: Vector2, targetPos: Vector2, angle: Float, radius: Float, squish: Float, transition: Float, color: Color, alpha: Float) {
        extrusionVec.set(1.5f, 0f).rotateDeg(angle)
        val totalDist = targetPos.dst(shipPos)
        val layers = 8
        for (i in 3 until layers) {
            val ratio = i.toFloat() / (layers - 1).coerceAtLeast(1)
            val layerRadius = radius * MathUtils.lerp(1f, 0.5f, ratio * 0.85f * transition)
            val layerAlpha = alpha * MathUtils.lerp(0.2f, 1.0f, 1f - ratio) * transition
            orbitPoint.set(extrusionVec).scl(ratio * totalDist * transition)
            layerPos.set(shipPos).add(orbitPoint)
            batch.color = color.cpy().apply { a = layerAlpha * 0.8f }
            drawPolygonRing(layerPos, angle, layerRadius, squish, 360f, stateTime * 10f, TacticalHUDConstants.RING_RADIAL_THICKNESS * 0.2f, 64)
            val arcSpan = MathUtils.lerp(140f, 30f, ratio)
            batch.color = color.cpy().apply { a = layerAlpha }
            drawVolumetricRing(layerPos, angle, layerRadius, squish, arcSpan, 90f - arcSpan/2f, TacticalHUDConstants.RING_VISUAL_DEPTH * 0.5f, 32)
            drawVolumetricRing(layerPos, angle, layerRadius, squish, arcSpan, 270f - arcSpan/2f, TacticalHUDConstants.RING_VISUAL_DEPTH * 0.5f, 32)
        }
    }

    private fun drawPolygonRing(pos: Vector2, lastStableAngle: Float, radius: Float, squish: Float, arcDegrees: Float, startAngle: Float, radialThickness: Float, segments: Int) {
        val pixel = TextureRegistry.voidTexture
        val step = arcDegrees / segments
        val colorBits = batch.color.toFloatBits()
        val innerR = radius - radialThickness / 2f
        val outerR = radius + radialThickness / 2f
        for (i in 0 until segments) {
            val a1 = startAngle + (i * step)
            val a2 = startAngle + ((i + 1) * step)
            setRingVertex(0, pos.x, pos.y, a1, innerR, squish, lastStableAngle, colorBits, 0f, 0f)
            setRingVertex(5, pos.x, pos.y, a1, outerR, squish, lastStableAngle, colorBits, 1f, 0f)
            setRingVertex(10, pos.x, pos.y, a2, outerR, squish, lastStableAngle, colorBits, 1f, 1f)
            setRingVertex(15, pos.x, pos.y, a2, innerR, squish, lastStableAngle, colorBits, 0f, 1f)
            batch.draw(pixel, ringVertices, 0, 20)
        }
    }

    private fun setRingVertex(offset: Int, cx: Float, cy: Float, angle: Float, r: Float, squish: Float, rot: Float, color: Float, u: Float, v: Float) {
        val rad = angle * MathUtils.degreesToRadians
        val lx = -MathUtils.sin(rad) * r
        val ly = MathUtils.cos(rad) * r
        tempVec.set(lx, ly * squish)
        tempVec.rotateDeg(rot - 90f)
        ringVertices[offset + 0] = cx + tempVec.x
        ringVertices[offset + 1] = cy + tempVec.y
        ringVertices[offset + 2] = color
        ringVertices[offset + 3] = u
        ringVertices[offset + 4] = v
    }

    private fun drawVolumetricRing(pos: Vector2, lastStableAngle: Float, radius: Float, squish: Float, arcDegrees: Float, startAngle: Float, depth: Float, segments: Int) {
        val pixel = TextureRegistry.voidTexture
        val step = arcDegrees / segments
        val lengthFactor = 0.6f
        for (i in 0 until segments) {
            val angle = startAngle + (i * step)
            orbitPoint.set(0f, radius).rotateDeg(angle)
            orbitPoint.scl(1.0f, squish)
            orbitPoint.rotateDeg(lastStableAngle - 90f)
            val wx = pos.x + orbitPoint.x
            val wy = pos.y + orbitPoint.y
            tempVec.set(-MathUtils.cosDeg(angle), -MathUtils.sinDeg(angle) * squish).nor()
            tempVec.rotateDeg(lastStableAngle - 90f)
            val segmentRot = tempVec.angleDeg()
            val segmentLength = ((radius * MathUtils.PI * 2f * (arcDegrees / 360f)) / segments) * lengthFactor
            batch.draw(pixel, wx - segmentLength/2f, wy - depth/2f, segmentLength/2f, depth/2f, segmentLength, depth, 1f, 1f, segmentRot, 0, 0, 1, 1, false, false)
        }
    }

    private fun getCategoryColor(category: ModuleCategory): Color = when (category) {
        ModuleCategory.POWER_CORE -> Color.ORANGE
        ModuleCategory.SHIELD -> Color.SKY
        ModuleCategory.ENGINE -> Color.LIME
        ModuleCategory.WEAPON_PRIMARY -> Color.RED
        ModuleCategory.BAY -> Color.CYAN
        else -> Color.GRAY
    }
}
