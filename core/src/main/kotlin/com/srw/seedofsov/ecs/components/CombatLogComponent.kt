package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import java.util.*

/**
 * Technical log for real-time combat event validation.
 * Truth on Disk: Maintains a sliding window of the last 5 combat events.
 */
class CombatLogComponent : Component, Pool.Poolable {
    val events = LinkedList<String>()
    val maxEvents = 5

    fun log(event: String) {
        events.addFirst(event)
        if (events.size > maxEvents) {
            events.removeLast()
        }
    }

    override fun reset() {
        events.clear()
    }

    companion object {
        val mapper = mapperFor<CombatLogComponent>()
    }
}
