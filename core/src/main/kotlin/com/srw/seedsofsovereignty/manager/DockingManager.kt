package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.badlogic.gdx.math.Vector2

class DockingManager(private val world: World) {
    fun undock() {
        val playerFamily = world.family { all(PlayerComponent, DockingComponent, PositionComponent) }
        playerFamily.firstOrNull()?.let { player ->
            with(world) {
                val docking = player[DockingComponent]
                val pos = player[PositionComponent].pos
                val vel = player[VelocityComponent].v

                docking.isDocked = false
                docking.undockCooldown = 3f

                docking.nearestStation?.let { station ->
                    val sPos = station[PositionComponent].pos
                    val pushDir = Vector2(pos).sub(sPos).nor()
                    if (pushDir.isZero) pushDir.set(1f, 1f).nor()
                    vel.set(pushDir).scl(500f)
                } ?: run {
                    vel.set(-300f, -300f)
                }
            }
        }
    }
}
