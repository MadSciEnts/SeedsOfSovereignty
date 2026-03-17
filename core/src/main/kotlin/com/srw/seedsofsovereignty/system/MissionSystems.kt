package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ShipFactory

/**
 * ENEMY SPAWN SYSTEM: Maintains a population of enemies based on Sector difficulty and distance.
 * Spawns enemies outside radar range (400 units).
 */
class EnemySpawnSystem(
    private val shipFactory: ShipFactory = inject(),
    private val maxEnemiesAtOrigin: Int = 3
) : IntervalSystem(interval = Fixed(3f)) {

    private val radarRange = 400f
    private val spawnBuffer = 50f // Spawn slightly outside radar

    override fun onTick() {
        val player = world.family { all(PlayerComponent, EvolutionComponent, PositionComponent) }.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val playerTier = player[EvolutionComponent].tier

        // 1. Calculate dynamic max population based on distance from sector origin
        val distFromOrigin = playerPos.len()

        // Scale population from origin (min) to sector limit (max)
        val maxEnemies = (maxEnemiesAtOrigin + (distFromOrigin / 1000f) * 10).toInt().coerceAtMost(30)

        val currentEnemies = world.family { all(FactionComponent) }.filter {
            it[FactionComponent].type == FactionType.ENEMY
        }.size

        if (currentEnemies < maxEnemies) {
            val sectorBaseTier = 1 // Future: Pull from SectorComponent
            val tieredDifficulty = (sectorBaseTier + (distFromOrigin / 500f).toInt()).coerceAtMost(playerTier + 2)

            // Random position just outside radar range
            val angle = MathUtils.random(0f, 360f)
            val dist = radarRange + spawnBuffer

            val spawnX = playerPos.x + MathUtils.cosDeg(angle) * dist
            val spawnY = playerPos.y + MathUtils.sinDeg(angle) * dist

            shipFactory.createEnemy(world, tieredDifficulty, isBoss = MathUtils.randomBoolean(0.05f))
                .configure {
                    it[PositionComponent].pos.set(spawnX, spawnY)
                }
        }
    }
}

/**
 * ENEMY AI: Implementation of the "Goldilocks Zone" and Dynamic Orbiting.
 */
class EnemyAISystem : IteratingSystem(
    family { all(FactionComponent, VelocityComponent, PositionComponent, ModuleChannelsComponent).none(PlayerComponent) }
) {
    private val tempVec = Vector2()
    private val orbitVec = Vector2()
    private var stateTimer = 0f

    override fun onTick() {
        stateTimer += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        if (entity[FactionComponent].type != FactionType.ENEMY) return

        val myPos = entity[PositionComponent].pos
        val vel = entity[VelocityComponent]
        val channels = entity[ModuleChannelsComponent]

        // Find Target (Player)
        val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos

        // Vector to player
        tempVec.set(playerPos).sub(myPos)
        val dist = tempVec.len()

        // 1. Goldilocks Movement (Maintain 5-10 units)
        // Adjust orbit direction every 5 seconds or based on ID
        val orbitDir = if (((entity.id + stateTimer.toInt() / 5) % 2) == 0) 1 else -1

        when {
            dist > 15f -> { // Pursuit: Close the gap
                tempVec.nor().scl(vel.acceleration * deltaTime)
                vel.v.add(tempVec)
            }
            dist < 6f -> { // Flee: Back off
                tempVec.nor().scl(-vel.acceleration * deltaTime)
                vel.v.add(tempVec)
            }
            else -> { // Orbit: Circle the player
                orbitVec.set(tempVec).rotate90(orbitDir).nor().scl(vel.acceleration * 0.6f * deltaTime)
                vel.v.add(orbitVec)
            }
        }

        // 2. Aggressive Firing: Trigger weapons if within 12 units
        channels.inputs["AIM"]?.let { input ->
            input.vector.set(tempVec).nor()
            input.triggered = dist < 12f
        }
    }
}
