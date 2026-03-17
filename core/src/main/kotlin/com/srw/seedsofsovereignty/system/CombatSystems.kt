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

        // 1. Update cooldown
        if (weapon.currentCooldown > 0) {
            weapon.currentCooldown -= deltaTime
        }

        // 2. Handle Input per defined Logic
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

        // 1. Check for Trigger
        if (aimInput.triggered && weapon.currentCooldown <= 0) {

            // 2. Determine Fire Angle (Platform Rotation)
            val fireAngle = if (aimInput.vector.len2() > 0.01f) {
                aimInput.vector.angleDeg()
            } else {
                // Default to ship's rotation
                val attach = entity.getOrNull(AttachmentComponent)
                if (attach != null && attach.parent != Entity.NONE && attach.parent in world) {
                    val parentVel = attach.parent.getOrNull(VelocityComponent)
                    parentVel?.v?.angleDeg() ?: 90f
                } else 90f
            }

            // 3. Sequential Firing Logic
            if (weapon.barrelOffsets.isEmpty()) return

            if (weapon.barrelIndex >= weapon.barrelOffsets.size) {
                weapon.barrelIndex = 0
            }

            val barrelOffset = weapon.barrelOffsets[weapon.barrelIndex]

            // Spawn Projectile
            val faction = entity.getOrNull(FactionComponent)?.type
                ?: entity.getOrNull(AttachmentComponent)?.parent?.let { if (it in world) it.getOrNull(FactionComponent)?.type else FactionType.NEUTRAL }
                ?: FactionType.NEUTRAL

            projectileFactory.spawn(world, payload.blueprint, faction, pos, barrelOffset, fireAngle)

            // 4. Update Sequential Index and Cooldown
            weapon.barrelIndex = (weapon.barrelIndex + 1) % weapon.barrelOffsets.size

            /**
             * CUMULATIVE RATE LOGIC:
             * Total cycle time = fireRate (e.g., 0.5s).
             * Delay between sequential shots = fireRate / numBarrels.
             * This ensures 3 barrels fire 3x faster than 1 barrel.
             */
            weapon.currentCooldown = weapon.fireRate / weapon.barrelOffsets.size

            // 5. Update Output Channel (Heat)
            val heatOutput = channels.outputs["HEAT"]
            if (heatOutput != null) {
                heatOutput.status = com.badlogic.gdx.math.MathUtils.clamp(heatOutput.status + 0.05f, 0f, 1f)
            }
        } else {
            // Passive heat dissipation
            val heatOutput = channels.outputs["HEAT"]
            if (heatOutput != null && heatOutput.status > 0) {
                heatOutput.status = com.badlogic.gdx.math.MathUtils.clamp(heatOutput.status - deltaTime * 0.2f, 0f, 1f)
            }
        }
    }
}

/**
 * PROJECTILE SYSTEM: Manages the lifecycle of projectiles.
 */
class ProjectileSystem : IteratingSystem(
    family { all(ProjectileComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val projectile = entity[ProjectileComponent]
        projectile.lifeTime -= deltaTime
        if (projectile.lifeTime <= 0f) {
            world.run { entity.remove() }
        }
    }
}
