package com.srw.seedofsov.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * Stores the visual representation of the rotating weapon barrels.
 */
class WeaponPlatformComponent : Component, Pool.Poolable {
    var barrelTexture: Texture? = null

    override fun reset() {
        // Texture lifecycle is managed elsewhere (e.g. System or AssetGen)
        barrelTexture = null
    }

    companion object {
        val mapper = mapperFor<WeaponPlatformComponent>()
    }
}
