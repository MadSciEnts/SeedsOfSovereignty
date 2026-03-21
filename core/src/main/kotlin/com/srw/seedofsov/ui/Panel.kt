package com.srw.seedofsov.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

/**
 * A custom UI Panel that supports precise positioning and a procedural background.
 * Avoids Scene2D Table layout logic.
 * Truth on Disk: background is mutable to support asynchronous procedural asset generation.
 */
open class Panel(
    var background: Drawable? = null
) : Group() {

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (background != null) {
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
            background?.draw(batch, x, y, width, height)
        }
        super.draw(batch, parentAlpha)
    }

    /**
     * Adds an actor with a precise offset from the panel's origin.
     */
    fun addActorAt(actor: com.badlogic.gdx.scenes.scene2d.Actor, offsetX: Float, offsetY: Float) {
        actor.setPosition(offsetX, offsetY)
        addActor(actor)
    }
}
