package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.*
import com.srw.seedofsov.ui.hud.Hud
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages the procedural animations and layout of the Tactical HUD.
 * Truth on Disk: Synchronizes world position and orientation to prevent visual shearing.
 */
class TacticalSystem(private val hud: Hud) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class).get(),
    45
) {
    private val tacticalCategories = setOf(
        ModuleCategory.WEAPON_PRIMARY,
        ModuleCategory.WEAPON_SECONDARY,
        ModuleCategory.BAY,
        ModuleCategory.POINT_DEFENSE,
        ModuleCategory.SHIELD
    )

    private val categoryPriority = mapOf(
        ModuleCategory.WEAPON_PRIMARY to 0,
        ModuleCategory.WEAPON_SECONDARY to 1,
        ModuleCategory.BAY to 2,
        ModuleCategory.POINT_DEFENSE to 3,
        ModuleCategory.SHIELD to 4
    )

    private val tempVec = Vector2()
    private val currentIntentOffset = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val activeSlot = hud.getTacticalActiveSlot()

        val tacticalModules = ship.slots
            .mapNotNull { it.equippedModule }
            .filter { it.category in tacticalCategories }
            .sortedBy { categoryPriority[it.category] ?: 99 }

        ship.slots.forEach { slot ->
            val module = slot.equippedModule ?: return@forEach
            if (module.category !in tacticalCategories) {
                module.tacticalState.layoutOrbitIndex = -1
                module.tacticalState.selectionAnimation = 0f
                module.tacticalState.aimTransition = 0f
            }
        }

        tacticalModules.forEachIndexed { index, module ->
            val state = module.tacticalState
            val isSelected = (ship.slots.find { it.equippedModule === module } === activeSlot)

            state.isSelected = isSelected
            state.layoutOrbitIndex = index

            val targetAnim = if (state.isSelected) 1f else 0f
            state.selectionAnimation = MathUtils.lerp(state.selectionAnimation, targetAnim, deltaTime * 5f)

            if (state.firePulse > 0f) {
                state.firePulse = MathUtils.clamp(state.firePulse - deltaTime * 4f, 0f, 1f)
            }

            updateIntentProjection(module, transform, deltaTime)
        }
    }

    private fun updateIntentProjection(module: ShipModule, shipTransform: TransformComponent, deltaTime: Float) {
        val state = module.tacticalState
        val shipRadius = TacticalHUDConstants.calculateShipRadius(shipTransform.size)
        val tacticalRange = shipRadius * TacticalHUDConstants.TACTICAL_RANGE_MULTIPLIER

        val commandPos = module.commandPosition

        // TRUTH ON DISK FIX: If the controller is at origin (threshold 0.05), we ALWAYS use WORLD (Camera) space.
        // This prevents ship rotation from dragging the center or shearing the lerp.
        val atOrigin = commandPos.len() < 0.05f
        val effectiveSpace = if (atOrigin) CoordinateSpace.WORLD else module.commandSpace

        // 1. POSITION SYNC: Smoothed intent for visual fluidity
        state.lerpedOffset.lerp(commandPos, 14f * deltaTime)
        tempVec.set(state.lerpedOffset).scl(tacticalRange)

        if (effectiveSpace == CoordinateSpace.OBJECT) {
            currentIntentOffset.set(tempVec).rotateDeg(shipTransform.rotation)
        } else {
            currentIntentOffset.set(tempVec)
        }

        state.worldTargetPos.set(shipTransform.position.x + currentIntentOffset.x, shipTransform.position.y + currentIntentOffset.y)

        // 2. ORIENTATION SYNC: Must match the position's temporal state
        val intentLen = state.lerpedOffset.len()
        val pushMag = MathUtils.clamp(intentLen, 0f, 1f)

        val hardwareAngle = when (module.orientationReality) {
            TacticalOrientationReality.WORLD_FIXED -> module.worldAimAngle
            TacticalOrientationReality.SHIP_RELATIVE -> shipTransform.rotation + 90f
            TacticalOrientationReality.AWAY_FROM_SHIP -> {
                // Spherical Projection: Faces away from ship center.
                // Force CAMERA ORIENTATION (90f) when at origin.
                if (intentLen > 0.05f) {
                    currentIntentOffset.angleDeg()
                } else {
                    90f
                }
            }
        }

        state.lastStableAwayAngle = MathUtils.lerpAngleDeg(state.lastStableAwayAngle, hardwareAngle, 14f * deltaTime)
        state.localAwayAngle = state.lastStableAwayAngle - shipTransform.rotation

        state.heightSquish = MathUtils.lerp(1.0f, 0.35f, pushMag)
        state.aimTransition = pushMag

        val idleRadius = TacticalHUDConstants.getRingRadius(shipRadius, state.layoutOrbitIndex)
        val projectedRadius = module.getProjectedRadius(shipRadius)

        state.currentRadius = MathUtils.lerp(idleRadius, projectedRadius, state.aimTransition)

        // 3. ROTATION LOCK: Flat rings (idle) MUST NEVER rotate.
        // We use the same 0.05 threshold to match the orientation and space logic.
        if (intentLen <= 0.05f) {
            state.currentRotation = 0f
        } else {
            val targetRotation = module.getTacticalRotation()
            state.currentRotation = MathUtils.lerpAngleDeg(state.currentRotation, targetRotation, 14f * deltaTime)
        }
    }
}
