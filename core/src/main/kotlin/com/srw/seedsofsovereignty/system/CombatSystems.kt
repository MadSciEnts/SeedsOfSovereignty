package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ProjectileFactory

/**
 * MODULE ACTION SYSTEM: Bridges User Input (Channels) to physical module actions.
 * Handles Sequential Firing and other module-specific behaviors.
 */
class ModuleActionSystem(
    private val projectileFactory: ProjectileFactory = inject()
) : IteratingSystem(
    family { all(ModuleChannelsComponent, WeaponModuleComponent, ProjectilePayloadComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val channels = entity[ModuleChannelsComponent]
        val weapon = entity[WeaponModuleComponent]
        val payload = entity[ProjectilePayloadComponent]
        val pos = entity[PositionComponent].pos

        // Update cooldown
        if (weapon.currentCooldown > 0) {
            weapon.currentCooldown -= deltaTime
        }

        // Handle Input per defined Logic
        when (channels.type) {
            ModuleType.KINETIC_CANNON -> handleKineticCannon(entity, channels, weapon, payload, pos)
            else -> {}
        }
    }

    private fun handleKineticCannon(
        entity: Entity,
        channels: ModuleChannelsComponent,
        weapon: WeaponModuleComponent,
        payload: ProjectilePayloadComponent,
        pos: Vector2
    ) {
        val aimInput = channels.inputs["AIM"] ?: return

        // 1. Check for Trigger (from Joystick or Button)
        if (aimInput.triggered && weapon.currentCooldown <= 0) {

            // 2. Determine Fire Vector (Default to "Forward" if no vector provided)
            val fireAngle = if (aimInput.vector.len2() > 0.1f) {
                aimInput.vector.angleDeg()
            } else {
                // Look for parent ship's rotation (stored in its Velocity vector angle)
                val attach = entity.getOrNull(AttachmentComponent)
                if (attach != null && attach.parent != Entity.NONE && attach.parent in world) {
                    val parentVel = attach.parent.getOrNull(VelocityComponent)
                    parentVel?.v?.angleDeg() ?: 90f
                } else 90f
            }

            // 3. Determine Faction (Inherit from parent if possible)
            val faction = entity.getOrNull(FactionComponent)?.type
                ?: entity.getOrNull(AttachmentComponent)?.parent?.let { if (it in world) it.getOrNull(FactionComponent)?.type else FactionType.NEUTRAL }
                ?: FactionType.NEUTRAL

            // 4. Sequential Firing Logic with safety check
            if (weapon.barrelOffsets.isEmpty()) return

            // Safety: Ensure index is always within bounds (prevents IndexOutOfBoundsException)
            if (weapon.barrelIndex >= weapon.barrelOffsets.size) {
                weapon.barrelIndex = 0
            }

            val barrelOffset = weapon.barrelOffsets[weapon.barrelIndex]

            // Spawn Projectile via Factory
            projectileFactory.spawn(world, payload.blueprint, faction, pos, barrelOffset, fireAngle)

            // Increment barrel index
            weapon.barrelIndex = (weapon.barrelIndex + 1) % weapon.barrelOffsets.size

            // Reset Cooldown
            weapon.currentCooldown = weapon.fireRate
        }
    }
}

/**
 * PROJECTILE SYSTEM: Manages the lifecycle of projectiles.
 * Culls entities when their lifetime expires.
 */
class ProjectileSystem : IteratingSystem(
    family { all(ProjectileComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val projectile = entity[ProjectileComponent]

        // Decrement lifetime
        projectile.lifeTime -= deltaTime

        // Cull expired projectiles using correct Fleks 2.x syntax
        if (projectile.lifeTime <= 0f) {
            world.run { entity.remove() }
        }
    }
}
