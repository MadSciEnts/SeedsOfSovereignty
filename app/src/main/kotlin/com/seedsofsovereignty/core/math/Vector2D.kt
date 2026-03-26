package com.seedsofsovereignty.core.math

import kotlin.math.*

/**
 * High-performance 2D Vector for space physics and coordinate mapping.
 * Uses Floats for mobile GPU and performance compatibility.
 */
data class Vector2D(val x: Float = 0f, val y: Float = 0f) {

    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D()

    fun length() = sqrt(x * x + y * y)

    fun normalize(): Vector2D {
        val len = length()
        return if (len != 0f) this / len else Vector2D()
    }

    fun distanceTo(other: Vector2D) = (other - this).length()

    fun rotate(degrees: Float): Vector2D {
        val radians = Math.toRadians(degrees.toDouble()).toFloat()
        val cosR = cos(radians)
        val sinR = sin(radians)
        return Vector2D(
            x * cosR - y * sinR,
            x * sinR + y * cosR
        )
    }

    fun dot(other: Vector2D) = x * other.x + y * other.y

    companion object {
        val Zero = Vector2D(0f, 0f)
        val Up = Vector2D(0f, -1f)
        val Right = Vector2D(1f, 0f)
    }
}
