package com.sovereign.core

import android.graphics.PointF
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Bitmap

/**
 * SOVEREIGN CORE BLUEPRINT V4: PURE KOTLIN / ANDROID CANVAS
 * This file represents the immutable technical contracts of the engine.
 */

object ViewportManager {
    const val LOGICAL_H = 360f
    var logicalW = 640f // Calculated based on device aspect ratio
    var scaleFactor = 1f

    fun update(physicalW: Float, physicalH: Float) {
        scaleFactor = physicalH / LOGICAL_H
        logicalW = physicalW / scaleFactor
    }

    fun toLogicalX(screenX: Float) = screenX / scaleFactor
    fun toLogicalY(screenY: Float) = screenY / scaleFactor
    fun toPhysical(logicalVal: Float) = logicalVal * scaleFactor
}

enum class Quadrant(val index: Int) {
    FORE(0), AFT(1), PORT(2), STAR(3)
}

interface IModule {
    val id: String
    val tier: Int
    val powerDraw: Float
    val placement: Quadrant
    var tacticalPosition: PointF // Persistent coordinate handle
    var cachedTexture: Bitmap?   // Phase 09 requirement

    /**
     * Efficiency = The weakest link in the metabolism.
     */
    fun calculateEfficiency(coreHealth: Float, quadrantHealth: Float, powerAllocation: Float): Float {
        return minOf(coreHealth, quadrantHealth, powerAllocation)
    }

    fun update(deltaTime: Float)
    fun draw(canvas: Canvas, paint: Paint)
}

data class ShipIntegrity(
    var coreHealth: Float = 1.0f,
    var quadrantHull: FloatArray = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f),
    var categoryPower: MutableMap<String, Float> = mutableMapOf(
        "NAV" to 1.0f, "WEA" to 1.0f, "SHI" to 1.0f, "TAC" to 1.0f
    )
)
