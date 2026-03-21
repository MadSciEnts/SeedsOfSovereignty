package com.srw.seedofsov.model

import com.badlogic.gdx.math.Vector2

/**
 * Standardized data container for binding UI/AI to Modules.
 * Truth on Disk: Supports Float, Vector2, Boolean, and Enum types.
 */
class DataChannel(val name: String) {
    var valueFloat: Float = 0f
    val valueVector2 = Vector2()
    var valueBoolean: Boolean = false
    var valueEnum: Any? = null

    fun reset() {
        valueFloat = 0f
        valueVector2.set(0f, 0f)
        valueBoolean = false
        valueEnum = null
    }
}
