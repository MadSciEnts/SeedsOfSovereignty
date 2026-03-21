package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.MissionComponent
import com.srw.seedofsov.ecs.components.SeekingComponent
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.model.OrderType
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Translates MissionSteps into actionable steering targets for SeekingComponent.
 * Truth on Disk: Implements the sequential mission buffer for Epoch 3.
 */
class OrderSystem : IteratingSystem(
    allOf(MissionComponent::class, SeekingComponent::class, TransformComponent::class).get()
) {
    private val tempVec = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val mission = entity[MissionComponent.mapper] ?: return
        val seeking = entity[SeekingComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return

        // 1. Manage the Mission Queue
        if (mission.currentStep == null || mission.isStepComplete) {
            mission.currentStep = mission.steps.poll()
            mission.isStepComplete = false
        }

        val step = mission.currentStep ?: return // No mission currently

        // 2. Process Current Step
        when (step.type) {
            OrderType.MOVE_TO -> {
                seeking.targetPos.set(step.targetPos)
                seeking.targetSpace = step.targetSpace
                seeking.target = null

                // Check for completion (Within 0.5 units of target)
                tempVec.set(transform.position.x, transform.position.y)
                if (tempVec.dst(step.targetPos) < 0.5f) {
                    mission.isStepComplete = true
                }
            }
            OrderType.ATTACK_TARGET -> {
                seeking.target = step.targetEntity
                // Attack completion logic would typically be handled by health systems
                // or when the target entity is destroyed.
            }
            OrderType.DEFEND_ENTITY -> {
                seeking.target = step.targetEntity
                seeking.targetSpace = step.targetSpace
                // Stay close to target
            }
            else -> {}
        }
    }
}
