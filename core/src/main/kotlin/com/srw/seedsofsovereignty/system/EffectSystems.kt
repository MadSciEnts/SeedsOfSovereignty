package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.ParticleComponent

class EffectSystem : IteratingSystem(family { all(ParticleComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val particle = entity[ParticleComponent]

        // Fading logic could go here if we added an alpha field
        // For now, we'll just handle the size/life in RenderSystem
    }
}
