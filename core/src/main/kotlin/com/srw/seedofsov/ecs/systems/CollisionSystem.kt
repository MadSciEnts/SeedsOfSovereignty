package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.EffectType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.entity
import ktx.ashley.with

/**
 * Handles projectile-to-ship collisions using Entry-Point Trajectory analysis.
 * Truth on Disk: Triggers visceral ribbon bursts for high-fidelity impact feedback.
 */
class CollisionSystem(
    private val spatialSystem: SpatialPartitionSystem
) : IteratingSystem(
    allOf(TransformComponent::class, ProjectileComponent::class).get(),
    25
) {
    private val impactVec = Vector2()
    private val nearbyEntities = mutableListOf<Entity>()

    override fun processEntity(projectile: Entity, deltaTime: Float) {
        val projTransform = projectile[TransformComponent.mapper] ?: return
        val projData = projectile[ProjectileComponent.mapper] ?: return

        if (!projData.isInitialized) return

        val p1 = projData.previousPosition
        val p2 = Vector2(projTransform.position.x, projTransform.position.y)
        val projRadius = (projTransform.size.x + projTransform.size.y) / 4f

        nearbyEntities.clear()
        spatialSystem.getNearbyEntities(projTransform.position.x, projTransform.position.y, nearbyEntities)

        for (i in 0 until nearbyEntities.size) {
            val ship = nearbyEntities[i]
            if (ship.hashCode() == projData.sourceEntityId) continue

            val shipTransform = ship[TransformComponent.mapper] ?: continue
            val stats = ship[ShipStatsComponent.mapper] ?: continue

            val center = Vector2(shipTransform.position.x, shipTransform.position.y)
            val hullRadius = Math.max(shipTransform.size.x, shipTransform.size.y) * 0.5f
            val shieldRadius = hullRadius + 1.25f

            // 1. Check Stand-off Shield Collision
            val shieldHit = solveIntersection(p1, p2, center, shieldRadius + projRadius)
            if (shieldHit != null) {
                val quadrant = calculateQuadrant(shieldHit, shipTransform)
                if (!projData.bypassShields && stats.shieldActive[quadrant]) {
                    projTransform.position.set(shieldHit.x, shieldHit.y, projTransform.position.z)
                    handleImpact(projectile, ship, projData, shieldHit, quadrant, stats, shipTransform, isShield = true)
                    engine.removeEntity(projectile)
                    return
                }
            }

            // 2. Check Physical Hull Collision
            val hullHit = solveIntersection(p1, p2, center, hullRadius + projRadius)
            if (hullHit != null) {
                val quadrant = calculateQuadrant(hullHit, shipTransform)
                projTransform.position.set(hullHit.x, hullHit.y, projTransform.position.z)
                handleImpact(projectile, ship, projData, hullHit, quadrant, stats, shipTransform, isShield = false)
                engine.removeEntity(projectile)
                return
            }
        }
    }

    private fun solveIntersection(p1: Vector2, p2: Vector2, center: Vector2, radius: Float): Vector2? {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val fx = p1.x - center.x
        val fy = p1.y - center.y

        val a = dx * dx + dy * dy
        if (a < 0.0001f) return null

        val b = 2f * (fx * dx + fy * dy)
        val c = (fx * fx + fy * fy) - (radius * radius)

        val discriminant = b * b - 4f * a * c
        if (discriminant >= 0) {
            val t = (-b - Math.sqrt(discriminant.toDouble()).toFloat()) / (2f * a)
            if (t in 0f..1f) {
                return Vector2(p1.x + t * dx, p1.y + t * dy)
            }
        }
        return null
    }

    private fun calculateQuadrant(hitPos: Vector2, shipTransform: TransformComponent): Int {
        impactVec.set(hitPos.x - shipTransform.position.x, hitPos.y - shipTransform.position.y)
        impactVec.rotateDeg(-shipTransform.rotation)
        val angle = MathUtils.atan2(impactVec.y, impactVec.x) * MathUtils.radiansToDegrees
        return when {
            angle in 45f..135f -> 0
            angle in 135f..180f || angle in -180f..-135f -> 1
            angle in -135f..-45f -> 2
            else -> 3
        }
    }

    private fun handleImpact(
        projectile: Entity,
        ship: Entity,
        projData: ProjectileComponent,
        hitPos: Vector2,
        quadrant: Int,
        stats: ShipStatsComponent,
        shipTransform: TransformComponent,
        isShield: Boolean
    ) {
        val quadNames = arrayOf("FRONT", "LEFT", "REAR", "RIGHT")
        var damageToApply = projData.damage
        var layerName = "HULL"

        val offsetX = hitPos.x - shipTransform.position.x
        val offsetY = hitPos.y - shipTransform.position.y

        if (isShield) {
            layerName = "SHIELD"
            val shieldDmg = damageToApply * projData.shieldMultiplier
            stats.shieldQuadrants[quadrant] = MathUtils.clamp(stats.shieldQuadrants[quadrant] - shieldDmg, 0f, stats.maxShieldQuadrants[quadrant])
            val rnd = 0.25f
            repeat(5) {
                spawnWhisp(hitPos.x + MathUtils.random(-rnd, rnd), hitPos.y + MathUtils.random(-rnd, rnd), Color.CYAN, 0.25f, 4, 3.5f, shipTransform, offsetX + MathUtils.random(-rnd, rnd), offsetY + MathUtils.random(-rnd, rnd), 0.15f, 0.5f)
            }
        } else {
            if (!projData.isShieldOnly) {
                val hullDmg = damageToApply * projData.hullMultiplier
                val currentHull = stats.hullQuadrants[quadrant]
                if (currentHull >= hullDmg) {
                    stats.hullQuadrants[quadrant] -= hullDmg
                    damageToApply = 0f
                } else {
                    stats.hullQuadrants[quadrant] = 0f
                    damageToApply -= (currentHull / projData.hullMultiplier)
                }

                // Truth on Disk: Short, fiery plumes for hull venting (Red/Orange/Yellow mix)
                val fireColor = when(MathUtils.random(2)) {
                    0 -> Color.RED
                    1 -> Color.ORANGE
                    else -> Color.YELLOW
                }
                repeat(4) {
                    spawnWhisp(hitPos.x, hitPos.y, fireColor, 0.5f, 10, 1.2f, shipTransform, offsetX, offsetY, 0.12f, 0.35f)
                }
            }

            if (damageToApply > 0f && !projData.isShieldOnly) {
                layerName = "CORE"
                val coreDmg = damageToApply * projData.coreMultiplier
                for (i in stats.powerCores.indices) {
                    if (stats.powerCores[i] > 0) {
                        stats.powerCores[i] = MathUtils.clamp(stats.powerCores[i] - coreDmg, 0f, stats.maxPowerCores[i])
                        repeat(12) {
                            spawnWhisp(hitPos.x, hitPos.y, Color.GOLD, 0.4f, 24, 5.0f, shipTransform, offsetX, offsetY, 0.3f, 1.5f)
                        }
                        break
                    }
                }
            }
        }

        ship[CombatLogComponent.mapper]?.log("HIT: ${quadNames[quadrant]} - $layerName (${"%.1f".format(projData.damage)} DMG)")
    }

    private fun spawnWhisp(x: Float, y: Float, color: Color, width: Float, segments: Int, speed: Float, parent: TransformComponent, ox: Float, oy: Float, duration: Float, lifeTime: Float) {
        engine.entity {
            val driftX = MathUtils.random(-1f, 1f) * speed
            val driftY = MathUtils.random(-1f, 1f) * speed

            with<TransformComponent> {
                position.set(x, y, 0.6f)
            }
            with<EffectComponent> {
                type = EffectType.RIBBON
                timer = lifeTime
                maxLifetime = lifeTime
                parentTransform = parent
                relativeOffset.set(ox, oy)
            }
            with<TrailComponent> {
                isTransient = true
                isEmitting = true
                emissionDuration = duration
                driftVariance = 0.4f
                this.maxSegments = segments
                this.baseWidth = width
                this.color = color.cpy().apply { a = 0.8f }
                this.targetColor = color.cpy().apply { a = 0f }
                dissipationRate = 1.0f / lifeTime
                this.emissionDistance = 0f

                this.segments.add(TrailSegment(Vector2(x, y), Vector2(driftX, driftY), 1.0f))
                this.lastEmissionPos.set(x, y)
            }
        }
    }
}
