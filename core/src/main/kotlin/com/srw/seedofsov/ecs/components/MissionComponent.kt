package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.MissionStep
import java.util.*
import ktx.ashley.mapperFor

/**
 * Tactical brain for ships and swarms (Epoch 3 Readiness).
 * Holds a queue of MissionSteps to be executed in sequence.
 * Truth on Disk: Implements the "Mission Buffer" requirement.
 */
class MissionComponent : Component, Pool.Poolable {
    val steps: Queue<MissionStep> = LinkedList()
    var currentStep: MissionStep? = null

    // Meta-state for the current step
    var isStepComplete = false

    /**
     * Adds a new tactical objective to the end of the buffer.
     */
    fun addStep(step: MissionStep) {
        steps.add(step)
    }

    /**
     * Clears all current and pending objectives.
     */
    fun clearMissions() {
        steps.clear()
        currentStep = null
        isStepComplete = false
    }

    override fun reset() {
        clearMissions()
    }

    companion object {
        val mapper = mapperFor<MissionComponent>()
    }
}
