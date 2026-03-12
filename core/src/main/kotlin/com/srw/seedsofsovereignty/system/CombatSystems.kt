package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer

/**
 * High-fidelity combat kernel (v3.2.1).
 * ROLE: Categorical Material Simulation & CCD Stabilization.
 *
 * FIXES:
 * 1. [CCD] - Implemented Segment-Circle Intersection for frame-perfect collisions.
 * 2. [ACCURACY] - Dynamic ship radius based on Tier/SubTier.
 * 3. [WAKES] - Normalized projectile wakes (maxLength 16, LifeSpan 1.0s).
 */
class WeaponSystem : IteratingSystem(family {
    all(WeaponComponent, TacticalStatsComponent, PositionComponent, VelocityComponent, OrientationComponent)
}) {
    private val mountOffsetsBuffer = mutableListOf<Vector2>()

    override fun onTickEntity(entity: Entity) {
        val weapon = entity[WeaponComponent]
        val stats = entity[TacticalStatsComponent]
        val shipPos = entity[PositionComponent].pos
        val hullAngle = entity[OrientationComponent].angleRad

        val isEnemy = entity.has(EnemyComponent)
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = if (isEnemy) 1 else (evo?.currentTier ?: 1)
        val subTier = if (isEnemy) 1 else (evo?.subTier ?: 1)

        weapon.barrels = subTier.coerceIn(1, 3)

        var isFiring = weapon.isFiring
        if (entity.has(PlayerComponent) && entity.has(TacticalHubComponent)) {
            val hub = entity[TacticalHubComponent]
            val aimVector = hub.vectors["AIM"]
            isFiring = (hub.activeBitmask and 1L != 0L) || (aimVector != null && aimVector.len() > 0.1f)

            if (tier < 4) weapon.aimAngleRad = hullAngle
            else aimVector?.let { if (it.len2() > 0.01f) weapon.aimAngleRad = it.angleRad() }
        }

        if (weapon.cooldown > 0f) weapon.cooldown -= deltaTime

        if (isFiring && weapon.cooldown <= 0f) {
            val module = if (entity.has(WeaponModuleComponent)) entity[WeaponModuleComponent] else null
            val type = module?.type ?: ProjectileType.KINETIC
            val (speedMult, mass) = when(type) {
                ProjectileType.KINETIC -> 1.0f to 1.0f
                ProjectileType.ION -> 2.0f to 0.5f
                ProjectileType.PHOTON -> 5.0f to 0.01f
                ProjectileType.PLASMA -> 0.4f to 4.0f
            }

            weapon.cooldown = ((module?.fireRate ?: 0.5f) / weapon.barrels) * (1.0f / stats.fireRateMult.coerceAtLeast(0.01f))

            val shipSize = ProceduralShipDrawer.getShipSize(tier, subTier)
            ProceduralShipDrawer.getMountOffsets(tier, subTier, shipSize, hullAngle, mountOffsetsBuffer)

            mountOffsetsBuffer.forEach { mountOffset ->
                world.entity { e ->
                    e += PositionComponent().apply { this.pos.set(shipPos).add(mountOffset) }
                    e += VelocityComponent().apply {
                        val bulletSpeed = (module?.projectileSpeed?.let { it * 0.1f } ?: 100f) * speedMult
                        this.v.set(bulletSpeed, 0f).setAngleRad(weapon.aimAngleRad)
                    }
                    e += OrientationComponent().apply { angleRad = weapon.aimAngleRad }
                    e += TrailComponent(
                        color = module?.projectileColor?.cpy() ?: Color.YELLOW.cpy(),
                        width = mass * 0.3f,
                        maxLength = 16
                    )
                    e += ProjectileComponent(
                        type = type,
                        tier = module?.projectileTier ?: tier,
                        mass = mass,
                        color = module?.projectileColor ?: Color.YELLOW,
                        ownerIsEnemy = isEnemy
                    )
                    e += LifeSpanComponent(remaining = if (type == ProjectileType.PLASMA) 2.0f else 1.0f)
                }
            }
            if (weapon.barrels > 1) weapon.currentBarrel = (weapon.currentBarrel + 1) % weapon.barrels
        }
    }
}

class CombatSystem : IteratingSystem(family { all(ProjectileComponent, PositionComponent, VelocityComponent, TrailComponent) }) {
    private val targetShips by lazy { world.family { all(PositionComponent).any(PlayerComponent, EnemyComponent) } }

    // CCD Temp Math Vectors
    private val p0 = Vector2()
    private val p1 = Vector2()
    private val closestPoint = Vector2()
    private val diff = Vector2()

    override fun onTickEntity(entity: Entity) {
        val proj = entity[ProjectileComponent]
        val pos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent].v
        val trail = entity[TrailComponent]

        // History update
        if (trail.history.size >= trail.maxLength) trail.history.removeAt(0)
        trail.history.add(Vector2(pos))

        // 1. Define Segment [P0, P1] for this frame
        p0.set(pos)
        p1.set(pos).add(vel.x * deltaTime, vel.y * deltaTime)

        // 2. Iterate Potential Targets
        var projectileConsumed = false
        targetShips.forEach { target ->
            if (projectileConsumed) return@forEach

            // Skip friendly fire
            val isTargetEnemy = target.has(EnemyComponent)
            if (proj.ownerIsEnemy == isTargetEnemy) return@forEach

            // Get target physical radius
            val tPos = target[PositionComponent].pos
            val tTier = if (isTargetEnemy) 1 else target[EvolutionComponent].currentTier
            val tSubTier = if (isTargetEnemy) 1 else target[EvolutionComponent].subTier
            val tRadius = ProceduralShipDrawer.getShipSize(tTier, tSubTier) * 0.9f // Shield/Hull buffer

            // 3. Segment-Circle Intersection (CCD)
            val dx = p1.x - p0.x
            val dy = p1.y - p0.y
            val lenSq = dx * dx + dy * dy
            val t = if (lenSq < 0.0001f) 0f else ((tPos.x - p0.x) * dx + (tPos.y - p0.y) * dy) / lenSq
            val clampedT = MathUtils.clamp(t, 0f, 1f)
            closestPoint.set(p0.x + clampedT * dx, p0.y + clampedT * dy)

            if (closestPoint.dst2(tPos) <= tRadius * tRadius) {
                resolveHit(target, proj, entity, closestPoint)
                projectileConsumed = true
            }
        }
    }

    private fun resolveHit(target: Entity, proj: ProjectileComponent, projectile: Entity, hitPos: Vector2) {
        // Mass-Based Knockback Impulse
        if (target.has(VelocityComponent) && projectile.has(VelocityComponent)) {
            val projVel = projectile[VelocityComponent].v
            val impulse = diff.set(projVel).nor().scl(proj.mass * 1.5f)
            target[VelocityComponent].v.add(impulse)
        }

        applyEffects(target, proj.effects, hitPos)
        world -= projectile
    }

    private fun applyEffects(target: Entity, effects: List<ProjectileEffect>, effectPos: Vector2) {
        effects.forEach { effect ->
            if (effect.type == EffectType.HULL_DAMAGE && target.has(HealthComponent)) {
                val health = target[HealthComponent]
                health.current -= effect.potency
                if (health.current <= 0f && target.has(EnemyComponent)) handleTargetDestruction(target)
            }
        }
    }

    private fun handleTargetDestruction(target: Entity) {
        val ePos = Vector2(target[PositionComponent].pos)
        world.entity {
            it += PositionComponent().apply { pos.set(ePos) }
            it += VelocityComponent().apply { friction = 0.5f }
            it += SalvageComponent(type = SalvageType.ESSENCE, amount = 1)
            it += VisibleComponent()
            it += LifeSpanComponent(remaining = 15f)
        }
        world -= target
    }
}

class LifeSpanSystem : IteratingSystem(family { all(LifeSpanComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val life = entity[LifeSpanComponent]
        life.remaining -= deltaTime
        if (life.remaining <= 0f) world -= entity
    }
}
