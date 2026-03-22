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
 * Truth on Disk: Filters out non-combat modules (Shields, Engines, Cores) to reduce visual clutter.
 */
class TacticalSystem(private val hud: Hud) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class).get(),
    45 // Priority before physics/rendering
) {
    private val tacticalCategories = setOf(
        ModuleCategory.WEAPON_PRIMARY,
        ModuleCategory.WEAPON_SECONDARY,
        ModuleCategory.BAY,
        ModuleCategory.POINT_DEFENSE
    )

    private val categoryPriority = mapOf(
        ModuleCategory.WEAPON_PRIMARY to 0,
        ModuleCategory.WEAPON_SECONDARY to 1,
        ModuleCategory.BAY to 2,
        ModuleCategory.POINT_DEFENSE to 3
    )

    private val tempVec = Vector2()
    private val currentIntentOffset = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val activeSlot = hud.getTacticalActiveSlot()

        // 1. Identify and Filter Tactical Modules
        val tacticalModules = ship.slots
            .mapNotNull { it.equippedModule }
            .filter { it.category in tacticalCategories }
            .sortedBy { categoryPriority[it.category] ?: 99 }

        // 2. Clear state for non-tactical modules (ensure they don't render)
        ship.slots.forEach { slot ->
            val module = slot.equippedModule ?: return@forEach
            if (module.category !in tacticalCategories) {
                module.tacticalState.layoutOrbitIndex = -1 // Disable rendering
                module.tacticalState.selectionAnimation = 0f
                module.tacticalState.aimTransition = 0f
            }
        }

        // 3. Update Procedural State for Permitted Modules
        tacticalModules.forEachIndexed { index, module ->
            val state = module.tacticalState
            val isSelected = (ship.slots.find { it.equippedModule === module } === activeSlot)

            state.isSelected = isSelected
            state.layoutOrbitIndex = index

            // Selection Animation (Breathe/Highlight)
            val targetAnim = if (state.isSelected) 1f else 0f
            state.selectionAnimation = MathUtils.lerp(state.selectionAnimation, targetAnim, deltaTime * 5f)

            // 4. Aim/Intent Projection & Module-Driven Scaling Logic
            updateIntentProjection(module, transform, deltaTime)
        }
    }

    private fun updateIntentProjection(module: ShipModule, shipTransform: TransformComponent, deltaTime: Float) {
        val state = module.tacticalState
        val shipRadius = TacticalHUDConstants.calculateShipRadius(shipTransform.size)
        val tacticalRange = shipRadius * TacticalHUDConstants.TACTICAL_RANGE_MULTIPLIER

        // Fetch Command Position based on module type
        val commandPos = when (module) {
            is WeaponModuleComponent -> module.commandPosition
            is BayModuleComponent -> module.commandPosition
            else -> Vector2.Zero
        }

        val commandSpace = when (module) {
            is BayModuleComponent -> module.commandSpace
            is WeaponModuleComponent -> module.commandSpace
            else -> CoordinateSpace.OBJECT
        }
        // A. Resolve Local-Space Intent (Smoothed)
        state.lerpedOffset.lerp(commandPos, 12f * deltaTime)
        tempVec.set(state.lerpedOffset).scl(tacticalRange)

        // B. Resolve Target Hub Position (Immediate World Lock)
        if (commandSpace == CoordinateSpace.OBJECT) {
            currentIntentOffset.set(tempVec).rotateDeg(shipTransform.rotation)
        } else {
            currentIntentOffset.set(tempVec)
        }

        state.worldTargetPos.set(shipTransform.position.x + currentIntentOffset.x, shipTransform.position.y + currentIntentOffset.y)

        // C. Resolve STABLE Orbit Orientation
        val intentMag = state.lerpedOffset.len()
        if (intentMag > 0.05f) {
            val targetLocalAngle = state.lerpedOffset.angleDeg()
            state.localAwayAngle = MathUtils.lerpAngleDeg(state.localAwayAngle, targetLocalAngle, 12f * deltaTime)

            if (commandSpace == CoordinateSpace.OBJECT) {
                state.lastStableAwayAngle = state.localAwayAngle + shipTransform.rotation
            } else {
                state.lastStableAwayAngle = state.localAwayAngle
            }
        }

        // D. Resolve dynamic scaling & rotation (Module Driven)
        val pushMag = MathUtils.clamp(intentMag, 0f, 1f)
        state.heightSquish = MathUtils.lerp(1.0f, 0.35f, pushMag)
        state.aimTransition = pushMag

        // AUTHORITY: The module defines the projected radius and rotation
        val idleRadius = TacticalHUDConstants.getRingRadius(shipRadius, state.layoutOrbitIndex)
        val projectedRadius = module.getProjectedRadius(shipRadius)

        state.currentRadius = MathUtils.lerp(idleRadius, projectedRadius, state.aimTransition)
        state.currentRotation = module.getTacticalRotation()
    }
}
