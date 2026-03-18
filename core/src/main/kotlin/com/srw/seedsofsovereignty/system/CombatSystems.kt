package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ProjectileFactory
import kotlin.math.max

/**
 * MODULE ACTION SYSTEM: Bridges User Input (Channels) to physical module actions.
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

        if (launcher.currentCooldown > 0) launcher.currentCooldown -= deltaTime

        val heatOutput = channels.outputs["HEAT"]
        if (heatOutput != null && heatOutput.status > 0) {
            heatOutput.status = MathUtils.clamp(heatOutput.status - deltaTime * 0.15f, 0f, 1f)
        }

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
        val barrelOffset = launcher.barrelOffsets[launcher.barrelIndex]

        val fireAngle = if (aimInput.vector.len2() > 0.01f) {
            aimInput.vector.angleDeg()
        } else {
            val attach = entity.getOrNull(AttachmentComponent)
            if (attach != null && attach.parent != Entity.NONE && attach.parent in world) {
                world.run { if (attach.parent.has(TextureRegionComponent)) attach.parent[TextureRegionComponent].rotation + 90f else 90f }
            } else 90f
        }

        val faction = entity.getOrNull(FactionComponent)?.type
            ?: entity.getOrNull(AttachmentComponent)?.parent?.let { if (it in world) world.run { it.getOrNull(FactionComponent)?.type } else FactionType.NEUTRAL }
            ?: FactionType.NEUTRAL

        val proj = projectileFactory.spawn(world, payload.blueprint, faction, pos, barrelOffset, fireAngle)

        proj.configure {
            it += DamageComponent(
                amount = payload.blueprint.baseDamage,
                type = payload.blueprint.damageType,
                shieldMultiplier = payload.blueprint.shieldMultiplier,
                impactFX = payload.blueprint.impactFX
            )
        }

        launcher.barrelIndex = (launcher.barrelIndex + 1) % launcher.barrelOffsets.size
        launcher.currentCooldown = launcher.fireRate / launcher.barrelOffsets.size
        heatOutput?.let { it.status = MathUtils.clamp(it.status + 0.04f, 0f, 1f) }
    }
}

/**
 * COLLISION SYSTEM: Detects impacts and applies damage to arced defensive layers.
 */
class CollisionSystem : IteratingSystem(
    family { all(PositionComponent, ProjectileComponent, DamageComponent, FactionComponent) }
) {
    private val tempVec = Vector2()

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projFaction = entity[FactionComponent].type
        val damage = entity[DamageComponent]

        world.family { all(PositionComponent, ArcedDefenseComponent, FactionComponent) }.forEach { target ->
            val targetFaction = target[FactionComponent].type
            if (projFaction != targetFaction) {
                val targetPos = target[PositionComponent].pos
                tempVec.set(projPos).sub(targetPos) // Vector from target center to impact

                val targetRadius = if (target.has(TextureRegionComponent)) target[TextureRegionComponent].scale * 0.5f else 0.5f

                if (tempVec.len2() < targetRadius * targetRadius) {
                    applyDirectionalDamage(target, damage, projPos, tempVec)
                    entity.remove()
                }
            }
        }
    }

    private fun applyDirectionalDamage(target: Entity, damage: DamageComponent, impactPos: Vector2, relativeImpact: Vector2) {
        target.configure { entity ->
            val defense = entity[ArcedDefenseComponent]
            val shipRotation = if (entity.has(TextureRegionComponent)) entity[TextureRegionComponent].rotation else 0f

            // 1. Resolve Angle relative to ship heading
            val impactAngle = relativeImpact.angleDeg() - shipRotation
            val normalizedAngle = (impactAngle + 360f) % 360f

            // 2. Select Arc
            val arc = when {
                normalizedAngle >= 315f || normalizedAngle < 45f -> ImpactTarget.SHIELD // Fore
                normalizedAngle >= 45f && normalizedAngle < 135f -> ImpactTarget.SHIELD // Starboard
                normalizedAngle >= 135f && normalizedAngle < 225f -> ImpactTarget.SHIELD // Aft
                else -> ImpactTarget.SHIELD // Port
            }

            // Note: We'll map the enums SHIELD/HULL to the specific arced properties below

            // 3. Resolve tunneling logic for the specific arc
            val result = when {
                normalizedAngle >= 315f || normalizedAngle < 45f -> { // FORE
                    val newS = max(0f, defense.shieldFore - (damage.amount * damage.shieldMultiplier))
                    val newH = if (newS <= 0f && defense.shieldFore <= 0f) max(0f, defense.hullFore - damage.amount) else defense.hullFore
                    val fx = if (defense.shieldFore > 0f) damage.impactFX[ImpactTarget.SHIELD] else damage.impactFX[ImpactTarget.HULL]
                    spawnImpactFX(impactPos, fx ?: ImpactFXType.SPARK)
                    defense.copy(shieldFore = newS, hullFore = newH)
                }
                normalizedAngle >= 45f && normalizedAngle < 135f -> { // STARBOARD
                    val newS = max(0f, defense.shieldStbd - (damage.amount * damage.shieldMultiplier))
                    val newH = if (newS <= 0f && defense.shieldStbd <= 0f) max(0f, defense.hullStbd - damage.amount) else defense.hullStbd
                    val fx = if (defense.shieldStbd > 0f) damage.impactFX[ImpactTarget.SHIELD] else damage.impactFX[ImpactTarget.HULL]
                    spawnImpactFX(impactPos, fx ?: ImpactFXType.SPARK)
                    defense.copy(shieldStbd = newS, hullStbd = newH)
                }
                normalizedAngle >= 135f && normalizedAngle < 225f -> { // AFT
                    val newS = max(0f, defense.shieldAft - (damage.amount * damage.shieldMultiplier))
                    val newH = if (newS <= 0f && defense.shieldAft <= 0f) max(0f, defense.hullAft - damage.amount) else defense.hullAft
                    val fx = if (defense.shieldAft > 0f) damage.impactFX[ImpactTarget.SHIELD] else damage.impactFX[ImpactTarget.HULL]
                    spawnImpactFX(impactPos, fx ?: ImpactFXType.SPARK)
                    defense.copy(shieldAft = newS, hullAft = newH)
                }
                else -> { // PORT
                    val newS = max(0f, defense.shieldPort - (damage.amount * damage.shieldMultiplier))
                    val newH = if (newS <= 0f && defense.shieldPort <= 0f) max(0f, defense.hullPort - damage.amount) else defense.hullPort
                    val fx = if (defense.shieldPort > 0f) damage.impactFX[ImpactTarget.SHIELD] else damage.impactFX[ImpactTarget.HULL]
                    spawnImpactFX(impactPos, fx ?: ImpactFXType.SPARK)
                    defense.copy(shieldPort = newS, hullPort = newH)
                }
            }

            entity += result
        }
    }

    private fun spawnImpactFX(pos: Vector2, type: ImpactFXType) {
        if (type == ImpactFXType.NONE) return
        world.entity { entity ->
            entity += PositionComponent(Vector2(pos))
            entity += ImpactFXComponent(type = type, lifeTime = 0.2f)
        }
    }
}

/**
 * IMPACT FX SYSTEM: Manages transient visual effects.
 */
class ImpactFXSystem : IteratingSystem(
    family { all(ImpactFXComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val fx = entity[ImpactFXComponent]
        fx.lifeTime -= deltaTime
        if (fx.lifeTime <= 0f) entity.remove()
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
        if (projectile.lifeTime <= 0f) entity.remove()
    }
}
