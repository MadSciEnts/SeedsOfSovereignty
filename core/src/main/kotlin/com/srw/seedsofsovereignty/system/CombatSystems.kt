package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer

class WeaponSystem : IteratingSystem(family { all(WeaponComponent, PositionComponent, VelocityComponent, OrientationComponent) }) {
    private val mountOffsetsBuffer = mutableListOf<Vector2>()

    override fun onTickEntity(entity: Entity) {
        val weapon = entity[WeaponComponent]
        val pos = entity[PositionComponent].pos
        val hullAngle = entity[OrientationComponent].angleRad
        val aimAngle = weapon.aimAngleRad

        val isEnemy = entity.has(EnemyComponent)
        val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
        val tier = if (isEnemy) (if (entity.has(EnemyComponent) && entity[EnemyComponent].type == "Reaper") 2 else 1) else (evo?.currentTier ?: 1)
        val subTier = evo?.subTier ?: 1

        if (weapon.cooldown > 0f) {
            weapon.cooldown -= deltaTime
        }

        if (weapon.isFiring && weapon.cooldown <= 0f) {
            val module = if (entity.has(WeaponModuleComponent)) entity[WeaponModuleComponent] else null
            weapon.cooldown = (module?.fireRate ?: 0.5f) / weapon.barrels.coerceAtLeast(1)

            val shipSize = ProceduralShipDrawer.getShipSize(tier, subTier)
            ProceduralShipDrawer.getMountOffsets(tier, subTier, shipSize, hullAngle, mountOffsetsBuffer)

            mountOffsetsBuffer.forEach { mountOffset ->
                val firePosX = pos.x + mountOffset.x
                val firePosY = pos.y + mountOffset.y

                world.entity { e ->
                    e += PositionComponent().apply {
                        this.pos.set(firePosX, firePosY)
                        val spreadWidth = if (tier >= 8) 30f else if (tier >= 6) 40f else 20f
                        val lateralOffset = if (weapon.barrels <= 1) 0f else {
                            (weapon.currentBarrel - (weapon.barrels - 1) * 0.5f) * (spreadWidth / (weapon.barrels - 1).coerceAtLeast(1))
                        }
                        val ox = MathUtils.cos(aimAngle + MathUtils.PI/2f) * lateralOffset
                        val oy = MathUtils.sin(aimAngle + MathUtils.PI/2f) * lateralOffset
                        this.pos.add(ox, oy)
                    }
                    e += VelocityComponent().apply {
                        val bulletSpeed = module?.projectileSpeed ?: (if (isEnemy) 600f else 1000f)
                        this.v.set(bulletSpeed, 0f).setAngleRad(aimAngle)
                    }
                    // Fix Task #4: Ensure in-game projectiles match module DNA
                    e += ProjectileComponent(
                        tier = module?.projectileTier ?: tier,
                        color = module?.projectileColor ?: Color.YELLOW,
                        effects = module?.effects?.toMutableList() ?: mutableListOf(ProjectileEffect(EffectType.HULL_DAMAGE, 10f)),
                        ownerIsEnemy = isEnemy
                    )
                    e += LifeSpanComponent(remaining = if (isEnemy) 2.5f else 1.5f)
                }
            }

            if (weapon.barrels > 1) {
                weapon.currentBarrel = (weapon.currentBarrel + 1) % weapon.barrels
            }
        }
    }
}

class CombatSystem : IteratingSystem(family { all(ProjectileComponent, PositionComponent) }) {
    private val enemyFamily by lazy { world.family { all(EnemyComponent, PositionComponent) } }
    private val playerFamily by lazy { world.family { all(PlayerComponent, PositionComponent, HealthComponent) } }
    private val droneFamily by lazy { world.family { all(DroneComponent, PositionComponent, HealthComponent) } }

    override fun onTickEntity(entity: Entity) {
        val projPos = entity[PositionComponent].pos
        val projComp = entity[ProjectileComponent]

        if (!projComp.ownerIsEnemy) {
            enemyFamily.forEach { enemy ->
                val enemyPos = enemy[PositionComponent].pos
                if (projPos.dst2(enemyPos) < 1600f) {
                    applyEffects(enemy, projComp.effects, projPos)
                    world -= entity
                    return@forEach
                }
            }
        } else {
            droneFamily.forEach { drone ->
                val dronePos = drone[PositionComponent].pos
                if (projPos.dst2(dronePos) < 600f) {
                    applyEffects(drone, projComp.effects, projPos)
                    world -= entity
                    return@forEach
                }
            }
            playerFamily.firstOrNull()?.let { player ->
                val playerPos = player[PositionComponent].pos
                if (projPos.dst2(playerPos) < 900f) {
                    applyEffects(player, projComp.effects, projPos)
                    world -= entity
                }
            }
        }
    }

    private fun applyEffects(target: Entity, effects: List<ProjectileEffect>, effectPos: Vector2) {
        effects.forEach { effect ->
            spawnVfx(effect.vfx, effectPos.x, effectPos.y)

            when (effect.type) {
                EffectType.HULL_DAMAGE -> {
                    if (target.has(HealthComponent)) {
                        target[HealthComponent].current -= effect.potency
                        if (target[HealthComponent].current <= 0f && (target.has(EnemyComponent) || target.has(DroneComponent))) {
                            handleTargetDestruction(target)
                        }
                    }
                }
                EffectType.SHIELD_DAMAGE -> {
                    if (target.has(ShieldComponent) && target[ShieldComponent].active) {
                        val shield = target[ShieldComponent]
                        shield.current = (shield.current - effect.potency).coerceAtLeast(0f)
                    }
                }
                else -> {}
            }
        }
    }

    private fun handleTargetDestruction(target: Entity) {
        if (target.has(EnemyComponent)) {
            val ePos = target[PositionComponent].pos
            world.entity {
                it += PositionComponent().apply { pos.set(ePos) }
                it += VelocityComponent().apply { friction = 0.5f }
                it += EssenceComponent()
                it += LifeSpanComponent(remaining = 15f)
            }
        }
        world -= target
    }

    private fun spawnVfx(vfxType: ImpactVfxType, x: Float, y: Float) {
        when (vfxType) {
            ImpactVfxType.SPARK_YELLOW -> spawnSparkBurst(x, y, Color.YELLOW, 8f)
            ImpactVfxType.SHIELD_RIPPLE_BLUE -> {
                world.entity {
                    it += PositionComponent().apply { pos.set(x, y) }
                    it += ParticleComponent(type = ParticleComponent.Type.SHIELD_RIPPLE, color = Color.CYAN, size = 40f)
                    it += LifeSpanComponent(remaining = 0.4f)
                }
            }
            ImpactVfxType.ACID_GREEN -> spawnSparkBurst(x, y, Color.GREEN, 10f)
            ImpactVfxType.SPARK_RED -> spawnSparkBurst(x, y, Color.RED, 12f)
            else -> spawnSparkBurst(x, y, Color.WHITE, 6f)
        }
    }

    private fun spawnSparkBurst(x: Float, y: Float, color: Color, baseSize: Float) {
        for (i in 0 until 5) {
            world.entity {
                it += PositionComponent().apply { pos.set(x, y) }
                it += VelocityComponent().apply {
                    v.set(MathUtils.random(-200f, 200f), MathUtils.random(-200f, 200f))
                    friction = 2f
                }
                it += ParticleComponent(type = ParticleComponent.Type.SPARK, color = color, size = baseSize * MathUtils.random(0.5f, 1.5f))
                it += LifeSpanComponent(remaining = MathUtils.random(0.2f, 0.4f))
            }
        }
    }
}

class LifeSpanSystem : IteratingSystem(family { all(LifeSpanComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val life = entity[LifeSpanComponent]
        life.remaining -= deltaTime
        if (life.remaining <= 0f) {
            world -= entity
        }
    }
}
