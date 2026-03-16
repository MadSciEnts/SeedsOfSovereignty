package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent

// 3. CAMERA SYSTEM: Follows the player
class CameraFollowSystem(
    private val camera: OrthographicCamera = inject()
) : IteratingSystem(
    family { all(PlayerComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        camera.position.x += (pos.x - camera.position.x) * 0.1f
        camera.position.y += (pos.y - camera.position.y) * 0.1f
    }
}
