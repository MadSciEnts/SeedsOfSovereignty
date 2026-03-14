package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.ApertureComponent
import com.srw.seedsofsovereignty.component.DockingComponent
import com.srw.seedsofsovereignty.component.PlayerComponent
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class ApertureSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent, DockingComponent, ApertureComponent) }) {
    override fun onTick() {
        batch.use(uiViewport.camera.combined) {
            drawer.update(); super.onTick(); drawer.setColor(Color.WHITE)
        }
    }
    override fun onTickEntity(entity: Entity) {
        val ap = entity[ApertureComponent]
        if (entity[DockingComponent].isDocked) ap.openProgress = (ap.openProgress - 2f * deltaTime).coerceAtLeast(0f)
        else ap.openProgress = (ap.openProgress + 2f * deltaTime).coerceAtMost(1f)
        if (ap.openProgress < 1f) {
            val h = (uiViewport.worldHeight / 2f) * (1f - ap.openProgress)
            drawer.setColor(0f, 0f, 0f, 0.8f)
            drawer.filledRectangle(0f, uiViewport.worldHeight - h, uiViewport.worldWidth, h)
            drawer.filledRectangle(0f, 0f, uiViewport.worldWidth, h)
        }
    }
}
