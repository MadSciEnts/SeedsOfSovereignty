package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.badlogic.gdx.math.Vector2

/**
 * MODULE ATTACHMENT SYSTEM: Syncs modules to parent ship in Object Space.
 * Ensures modules rotate and move perfectly with the ship chassis.
 */
class ModuleAttachmentSystem : IteratingSystem(
    family { all(AttachmentComponent, PositionComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val attach = entity[AttachmentComponent]
        val myPos = entity[PositionComponent]

        if (attach.parent != Entity.NONE && attach.parent in world) {
            world.run {
                val parentPos = attach.parent.get(PositionComponent).pos

                // Read rotation from the parent's visual components
                val parentRotation = when {
                    attach.parent.has(ShapeComponent) -> attach.parent.get(ShapeComponent).rotation
                    attach.parent.has(TextureRegionComponent) -> attach.parent.get(TextureRegionComponent).rotation
                    else -> 0f
                }

                // Transform the relative offset into World Space based on parent Hull rotation
                tempVec.set(attach.offset).rotateDeg(parentRotation)
                myPos.pos.set(parentPos).add(tempVec)
            }
        }
    }
}

/**
 * DECOMMISSIONED: Power Throttling is now handled by ShipExecutionSystem efficiency scaling.
 */
