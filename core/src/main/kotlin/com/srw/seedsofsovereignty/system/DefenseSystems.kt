package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.AttachmentComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.PowerCoreComponent

// 4. ATTACHMENT SYSTEM: Syncs modules to parent ship
class ModuleAttachmentSystem : IteratingSystem(
    family { all(AttachmentComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val attach = entity[AttachmentComponent]
        val myPos = entity[PositionComponent]

        // Find parent position
        if (attach.parent != Entity.NONE) {
            if (attach.parent in world) {
                // Correct way to access component of another entity in Fleks 2.x
                val parentPos = attach.parent.get(PositionComponent).pos
                myPos.pos.set(parentPos).add(attach.offset)
            }
        }
    }
}

// 5. POWER THROTTLING SYSTEM: Applies penalty if core is damaged
class PowerThrottlingSystem : IteratingSystem(
    family { all(PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val core = entity[PowerCoreComponent]
        val availablePower = core.baseOutput * core.integrity
        val requestedPower = core.weaponPower + core.shieldPower + core.enginePower + core.utilityPower

        // If we are overdrawing, everything slows down proportionally
        core.throttle = if (requestedPower > availablePower) availablePower / requestedPower else 1.0f
    }
}
