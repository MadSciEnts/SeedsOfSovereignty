package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ProjectileFactory

/**
 * MODULE ACTION SYSTEM: Bridges User Input (Channels) to physical module actions.
 * Handles Weapon Launchers and modular Projectile Payloads.
 */
class ModuleActionSystem(
    private val projectileFactory: ProjectileFactory = inject()
) : IteratingSystem(
    family { all(ModuleChannelsComponent, WeaponLauncherComponent, ProjectilePayloadComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val launcher = entity[WeaponLauncherComponent]
        val payload = entity[ProjectilePayloadComponent]
        val channels = entity[ModuleChannelsComponent]
        val pos = entity[PositionComponent].pos

        // 1. Cooldown & Burst Logic
        if (launcher.currentCooldown > 0) {
            launcher.currentCooldown -= deltaTime
        }

        // 2. Heat dissipation
        val heatOutput = channels.outputs["HEAT"]
        if (heatOutput != null && heatOutput.status > 0) {
            heatOutput.status = MathUtils.clamp(heatOutput.status - deltaTime * 0.15f, 0f, 1f)
        }

        // 3. Process Fire Logic
        val aimInput = channels.inputs["AIM"] ?: return

        if (aimInput.triggered && launcher.currentCooldown <= 0) {
            fireWeapon(entity, launcher, payload, aimInput, pos, heatOutput)
        }
    }

    private fun fireWeapon(
        entity: Entity,
        launcher: WeaponLauncherComponent,
        payload: ProjectilePayloadComponent,
        aimInput: InputChannelData,
        pos: Vector2,
        heatOutput: OutputChannelData?
    ) {
        if (launcher.barrelOffsets.isEmpty()) return

        // Sequential Firing Logic
        val barrelOffset = launcher.barrelOffsets[launcher.barrelIndex]

        // Determine Rotation (Input or Ship Direction)
        val fireAngle = if (aimInput.vector.len2() > 0.01f) {
            aimInput.vector.angleDeg()
        } else {
            val attach = entity.getOrNull(AttachmentComponent)
            if (attach != null && attach.parent != Entity.NONE && attach.parent in world) {
                world.run {
                    if (attach.parent.has(TextureRegionComponent)) attach.parent.get(TextureRegionComponent).rotation + 90f else 90f
                }
            } else 90f
        }

        // Resolve Faction
        val faction = entity.getOrNull(FactionComponent)?.type
            ?: entity.getOrNull(AttachmentComponent)?.parent?.let { if (it in world) world.run { it.getOrNull(FactionComponent)?.type } else FactionType.NEUTRAL }
            ?: FactionType.NEUTRAL

        // Spawn Projectile
        projectileFactory.spawn(world, payload.blueprint, faction, pos, barrelOffset, fireAngle)

        // Increment Sequential Index
        launcher.barrelIndex = (launcher.barrelIndex + 1) % launcher.barrelOffsets.size

        // Set Cooldown
        launcher.currentCooldown = launcher.fireRate / launcher.barrelOffsets.size

        // Apply Heat
        heatOutput?.let { it.status = MathUtils.clamp(it.status + 0.04f, 0f, 1f) }
    }
}

/**
 * COLLISION SYSTEM: Detects projectile-to-ship impacts and applies damage.
 */
class CollisionSystem : IteratingSystem(
    family { all(PositionComponent, ProjectileComponent, FactionComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projFaction = entity[FactionComponent].type
        val proj = entity[ProjectileComponent]

        // Check against all entities with ArcedDefenseComponent
        world.family { all(PositionComponent, ArcedDefenseComponent, FactionComponent) }.forEach { target ->
            val targetFaction = target[FactionComponent].type
            if (projFaction != targetFaction) {
                val targetPos = target[PositionComponent].pos
                tempVec.set(targetPos).sub(projPos)

                // Simple radius-based collision (can be refined with Hull size later)
                if (tempVec.len2() < 1.0f) {
                    applyDamage(target, 10f) // Hardcoded for now, pull from Projectile later
                    entity.remove() // Destroy projectile on impact
                }
            }
        }
    }

    private fun applyDamage(target: Entity, amount: Float) {
        val defense = target[ArcedDefenseComponent]
        // Simple shield-first damage for now
        // Future: resolve arc based on impact angle
        // defense.shieldFore -= amount
    }
}

/**
 * PROJECTILE SYSTEM: Manages lifecycle.
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
