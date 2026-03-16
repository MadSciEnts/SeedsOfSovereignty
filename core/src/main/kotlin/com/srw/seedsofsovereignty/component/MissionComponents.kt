package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.badlogic.gdx.math.Vector2

/**
 * MISSIONS & OBJECTIVES
 */

data class MissionComponent(
    val activeMissions: MutableList<MissionData> = mutableListOf()
) : Component<MissionComponent> {
    override fun type() = MissionComponent
    companion object : ComponentType<MissionComponent>()
}

data class MissionData(
    val id: String,
    val title: String,
    val type: MissionType,
    val targetPos: Vector2 = Vector2(),
    var isCompleted: Boolean = false,
    val rewardCredits: Long = 0
)

enum class MissionType { EXTERMINATION, ACQUISITION, ESCORT, EXPLORATION }

// Attached to entities that are targets of a mission
data class MissionObjectiveComponent(
    val missionId: String,
    val objectiveType: String
) : Component<MissionObjectiveComponent> {
    override fun type() = MissionObjectiveComponent
    companion object : ComponentType<MissionObjectiveComponent>()
}
