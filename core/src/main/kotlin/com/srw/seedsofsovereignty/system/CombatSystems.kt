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
import kotlin.math.min

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
 * COLLISION SYSTEM: Detects impacts and buffers them via ImpactComponent.
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
                tempVec.set(projPos).sub(targetPos)

                val targetRadius = if (target.has(TextureRegionComponent)) target[TextureRegionComponent].scale * 0.5f else 0.5f

                if (tempVec.len2() < targetRadius * targetRadius) {
                    target.configure { ctx ->
                        ctx += ImpactComponent(
                            target = target,
                            projectileDamage = damage,
                            impactPos = Vector2(projPos),
                            relativeImpact = Vector2(tempVec)
                        )
                        ctx -= ResolvedImpactComponent
                    }
                    entity.remove()
                }
            }
        }
    }
}

/**
 * GEOMETRY SYSTEM: Classifies impacts into quadrants.
 */
class GeometrySystem : IteratingSystem(
    family { all(ImpactComponent, TextureRegionComponent).none(ResolvedImpactComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val impact = entity[ImpactComponent]
        val shipRotation = entity[TextureRegionComponent].rotation
        val hitAngle = impact.relativeImpact.angleDeg()
        val normalizedAngle = (hitAngle - shipRotation + 360f) % 360f

        val quadrant = when {
            normalizedAngle >= 315f || normalizedAngle < 45f -> ImpactQuadrant.FORE
            normalizedAngle >= 45f && normalizedAngle < 135f -> ImpactQuadrant.STBD
            normalizedAngle >= 135f && normalizedAngle < 225f -> ImpactQuadrant.AFT
            else -> ImpactQuadrant.PORT
        }

        entity.configure { it += ResolvedImpactComponent(quadrant) }
    }
}

/**
 * BALLISTICS SYSTEM: Resolves damage and applies status effects.
 */
class BallisticsSystem : IteratingSystem(
    family { all(ImpactComponent, ResolvedImpactComponent, ArcedDefenseComponent, PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val impact = entity[ImpactComponent]
        val resolved = entity[ResolvedImpactComponent]
        val defense = entity[ArcedDefenseComponent]
        val core = entity[PowerCoreComponent]
        val damageComp = impact.projectileDamage

        var remainingDamage = damageComp.amount
        val quadrant = resolved.quadrant

        // 1. Shield Depletion
        val shieldAmount = when (quadrant) {
            ImpactQuadrant.FORE -> defense.shieldFore
            ImpactQuadrant.AFT -> defense.shieldAft
            ImpactQuadrant.PORT -> defense.shieldPort
            ImpactQuadrant.STBD -> defense.shieldStbd
        }

        if (shieldAmount > 0) {
            spawnImpactFX(impact.impactPos, ImpactFXType.FLARE)
            val shieldDamage = remainingDamage * damageComp.shieldMultiplier
            if (shieldDamage <= shieldAmount) {
                applyShieldDamage(defense, quadrant, shieldDamage)
                remainingDamage = 0f
            } else {
                applyShieldDamage(defense, quadrant, shieldAmount)
                remainingDamage = (shieldDamage - shieldAmount) / damageComp.shieldMultiplier
            }
        }

        // 2. Hull Depletion
        if (remainingDamage > 0) {
            spawnImpactFX(impact.impactPos, ImpactFXType.SPARK)
            val hullAmount = when (quadrant) {
                ImpactQuadrant.FORE -> defense.hullFore
                ImpactQuadrant.AFT -> defense.hullAft
                ImpactQuadrant.PORT -> defense.hullPort
                ImpactQuadrant.STBD -> defense.hullStbd
            }

            if (remainingDamage <= hullAmount) {
                applyHullDamage(defense, quadrant, remainingDamage)
                remainingDamage = 0f
            } else {
                applyHullDamage(defense, quadrant, hullAmount)
                remainingDamage -= hullAmount
            }
        }

        // 3. Power Core (Overflow)
        if (remainingDamage > 0) {
            core.integrity = MathUtils.clamp(core.integrity - (remainingDamage / 100f), 0f, 1.0f)
        }

        // 4. Status Effects (Acid/Electrical Lingering)
        if (damageComp.type == DamageType.ACID || damageComp.type == DamageType.ELECTRICAL) {
            val existing = entity.getOrNull(StatusEffectComponent)
            if (existing != null) {
                existing.duration = 5f // Refresh
                existing.intensity = max(existing.intensity, damageComp.amount * 0.15f)
                existing.lastQuadrant = quadrant
            } else {
                entity.configure {
                    it += StatusEffectComponent(
                        type = damageComp.type,
                        duration = 5f,
                        intensity = damageComp.amount * 0.15f,
                        lastQuadrant = quadrant
                    )
                }
            }
        }

        // CLEANUP
        entity.configure {
            it -= ImpactComponent
            it -= ResolvedImpactComponent
        }
    }

    private fun applyShieldDamage(defense: ArcedDefenseComponent, quadrant: ImpactQuadrant, amount: Float) {
        when (quadrant) {
            ImpactQuadrant.FORE -> defense.shieldFore -= amount
            ImpactQuadrant.AFT -> defense.shieldAft -= amount
            ImpactQuadrant.PORT -> defense.shieldPort -= amount
            ImpactQuadrant.STBD -> defense.shieldStbd -= amount
        }
    }

    private fun applyHullDamage(defense: ArcedDefenseComponent, quadrant: ImpactQuadrant, amount: Float) {
        when (quadrant) {
            ImpactQuadrant.FORE -> defense.hullFore -= amount
            ImpactQuadrant.AFT -> defense.hullAft -= amount
            ImpactQuadrant.PORT -> defense.hullPort -= amount
            ImpactQuadrant.STBD -> defense.hullStbd -= amount
        }
    }

    private fun spawnImpactFX(pos: Vector2, type: ImpactFXType) {
        world.entity {
            it += PositionComponent(Vector2(pos))
            it += ImpactFXComponent(type = type)
        }
    }
}

/**
 * STATUS EFFECT SYSTEM: Processes lingering damage over time.
 */
class StatusEffectSystem : IteratingSystem(
    family { all(StatusEffectComponent, ArcedDefenseComponent, PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val effect = entity[StatusEffectComponent]
        val defense = entity[ArcedDefenseComponent]
        val core = entity[PowerCoreComponent]

        effect.duration -= deltaTime
        effect.timer += deltaTime

        if (effect.timer >= 1.0f) {
            effect.timer -= 1.0f
            applyTickDamage(defense, core, effect)
        }

        if (effect.duration <= 0f) {
            entity.configure { it -= StatusEffectComponent }
        }
    }

    private fun applyTickDamage(defense: ArcedDefenseComponent, core: PowerCoreComponent, effect: StatusEffectComponent) {
        val damage = effect.intensity
        val quadrant = effect.lastQuadrant

        val currentHull = when (quadrant) {
            ImpactQuadrant.FORE -> defense.hullFore
            ImpactQuadrant.AFT -> defense.hullAft
            ImpactQuadrant.PORT -> defense.hullPort
            ImpactQuadrant.STBD -> defense.hullStbd
        }

        if (currentHull > 0) {
            val applied = min(damage, currentHull)
            when (quadrant) {
                ImpactQuadrant.FORE -> defense.hullFore -= applied
                ImpactQuadrant.AFT -> defense.hullAft -= applied
                ImpactQuadrant.PORT -> defense.hullPort -= applied
                ImpactQuadrant.STBD -> defense.hullStbd -= applied
            }
            if (damage > applied) {
                core.integrity = MathUtils.clamp(core.integrity - ((damage - applied) / 100f), 0f, 1.0f)
            }
        } else {
            core.integrity = MathUtils.clamp(core.integrity - (damage / 100f), 0f, 1.0f)
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
