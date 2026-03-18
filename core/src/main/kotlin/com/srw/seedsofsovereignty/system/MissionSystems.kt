package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ShipFactory

/**
 * ENEMY SPAWN SYSTEM: Maintains a population of enemies based on Sector difficulty.
 * Targets areas just outside the 400-unit radar range.
 */
class EnemySpawnSystem(
    private val shipFactory: ShipFactory = inject(),
    private val maxEnemiesAtOrigin: Int = 3
) : IntervalSystem(interval = Fixed(3f)) {

    private val radarRange = 400f
    private val spawnBuffer = 50f

    override fun onTick() {
        val player = world.family { all(PlayerComponent, EvolutionComponent, PositionComponent) }.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos
        val playerTier = player[EvolutionComponent].tier

        val distFromOrigin = playerPos.len()
        val maxEnemies = (maxEnemiesAtOrigin + (distFromOrigin / 1000f) * 3).toInt().coerceAtMost(30)

        val currentEnemies = world.family { all(FactionComponent) }.filter {
            it[FactionComponent].type == FactionType.ENEMY
        }.size

        if (currentEnemies < maxEnemies) {
            val tieredDifficulty = (1 + (distFromOrigin / 500f).toInt()).coerceAtMost(playerTier + 2)
            val angle = MathUtils.random(0f, 360f)
            val dist = radarRange + spawnBuffer

            shipFactory.createEnemy(world, tieredDifficulty, isBoss = MathUtils.randomBoolean(0.05f))
                .configure {
                    it[PositionComponent].pos.set(playerPos.x + MathUtils.cosDeg(angle) * dist, playerPos.y + MathUtils.sinDeg(angle) * dist)
                }
        }
    }
}

/**
 * ENEMY AI: Implementation of the "Naval Rhythmic Cycle."
 * Approach -> Orbit -> Pause -> Broadside.
 */
class EnemyAISystem : IteratingSystem(
    family { all(FactionComponent, PositionComponent).none(PlayerComponent, StationComponent) }
) {
    private val tempVec = Vector2()
    private val steerVec = Vector2()
    private var stateTimer = 0f

    override fun onTick() {
        stateTimer += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        if (entity[FactionComponent].type != FactionType.ENEMY) return

        val myPos = entity[PositionComponent].pos
        val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos

        // Calculate Vector to Player
        tempVec.set(playerPos).sub(myPos)
        val dist = tempVec.len()

        // Dynamic Cycle State: 0-4s Closing/Orbit, 4-6s Firing Pause
        val cycleTime = (stateTimer + entity.id) % 6f
        val isFiringPhase = cycleTime > 4f

        // Calculate Tactical Vectors
        val orbitDir = if (((entity.id + (stateTimer / 8f).toInt()) % 2) == 0) 1 else -1

        steerVec.set(0f, 0f)
        if (!isFiringPhase) {
            when {
                dist > 10f -> steerVec.set(tempVec).nor() // Approach
                dist < 5f -> steerVec.set(tempVec).nor().scl(-1f) // Back off
                else -> steerVec.set(tempVec).rotate90(orbitDir).nor().scl(0.5f) // Orbit slowly
            }
        }

        // Bridge to Modules
        world.family { all(AttachmentComponent, ModuleChannelsComponent) }.forEach { module ->
            if (module[AttachmentComponent].parent == entity) {
                val channels = module[ModuleChannelsComponent]

                // 1. Move via Engine Module
                if (channels.type == ModuleType.ENGINE_IMPULSE_T1) {
                    channels.inputs["MOVE"]?.let { it.vector.set(steerVec) }
                }

                // 2. Fire via Weapon Module
                if (channels.inputs.containsKey("AIM")) {
                    channels.inputs["AIM"]?.let {
                        it.vector.set(tempVec).nor()
                        it.triggered = isFiringPhase && dist < 15f
                    }
                }
            }
        }
    }
}
