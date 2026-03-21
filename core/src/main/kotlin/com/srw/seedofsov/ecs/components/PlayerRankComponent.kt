package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.srw.seedofsov.model.Subtier
import ktx.ashley.mapperFor

/**
 * Stores the player's current advancement level.
 * The ship and modules should inherit their subtier from here.
 */
class PlayerRankComponent : Component, Pool.Poolable {
    var subtier: Subtier = Subtier.A

    override fun reset() {
        subtier = Subtier.A
    }

    companion object {
        val mapper = mapperFor<PlayerRankComponent>()
    }
}
