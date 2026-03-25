package com.sovereign.core

import android.graphics.PointF
import android.graphics.Canvas

// 1. HIGH-PRECISION MATH
data class Vector2D(var x: Double, var y: Double)

// 2. THE UNIT CONTRACT
interface IUnit {
    val id: String
    var worldPos: Vector2D
    var velocity: Vector2D
    var rotation: Double // Radians
    val tier: Int
    val modules: MutableList<IModule>

    fun onUpdate(dt: Double)
    fun onRender(canvas: Canvas, camera: Vector2D)
}

// 3. THE MODULE CONTRACT
interface IModule {
    val type: ModuleType
    var powerRatio: Float // 0.0 to 1.0
    val enginePoints: List<PointF> // Max 4 total per Unit

    fun processAction(inputX: Float, inputY: Float)
    fun getPowerDraw(): Float
}

enum class ModuleType {
    PROPULSION, SHIELD, WEAPON, TACTICAL, HANGAR, SALVAGE
}
