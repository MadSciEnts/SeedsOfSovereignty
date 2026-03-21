package com.srw.seedofsov.model

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2

/**
 * Types of tactical orders for ships and swarms (Epoch 3).
 */
enum class OrderType {
    MOVE_TO,            // Go to position (Vector2)
    ATTACK_TARGET,      // Engage Entity
    DEFEND_ENTITY,      // Circle/Guard Entity
    DEFEND_POSITION,    // Stay near position
    RETREAT             // Move away from nearest enemy/parent
}

/**
 * A single objective in a ship's mission buffer.
 * Combines an order with its required target (Position or Entity).
 */
data class MissionStep(
    val type: OrderType,
    val targetPos: Vector2 = Vector2(),
    val targetSpace: CoordinateSpace = CoordinateSpace.WORLD,
    var targetEntity: Entity? = null
)
