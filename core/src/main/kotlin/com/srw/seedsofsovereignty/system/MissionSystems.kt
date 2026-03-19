package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.blueprint.BlueprintRegistry
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.UniversalFactory

/**
 * ENEMY SPAWN SYSTEM: Maintains population based on difficulty.
 */
class EnemySpawnSystem(
    private val maxEnemiesAtOrigin: Int = 3
) : IntervalSystem(interval = Fixed(3f)) {

    private val radarRange = 400f
    private val spawnBuffer = 50f
    private val tempSpawnPos = Vector2()
    private val playerFamily = family { all(PlayerComponent, PositionComponent) }
    private val enemyFamily = family { all(FactionComponent) }

    override fun onTick() {
        val player = playerFamily.firstOrNull() ?: return
        val playerPos = player[PositionComponent].pos

        // Count enemies efficiently without allocation
        var currentEnemies = 0
        enemyFamily.forEach { if (it[FactionComponent].type == FactionType.ENEMY) currentEnemies++ }

        if (currentEnemies < 30) {
            val angle = MathUtils.random(0f, 360f)
            val dist = radarRange + spawnBuffer
            tempSpawnPos.set(playerPos.x + MathUtils.cosDeg(angle) * dist, playerPos.y + MathUtils.sinDeg(angle) * dist)

            val enemyDNA = BlueprintRegistry.getUnit(1)
            UniversalFactory.spawnUnit(world, enemyDNA, FactionType.ENEMY, tempSpawnPos)
        }
    }
}

/**
 * ENEMY AI: Manifest-Driven Signal Bridge.
 * Refactored to support Tactical Squad Orders.
 * Prioritizes OrderComponent instructions over internal "Maintain distance" heuristics.
 */
class EnemyAISystem : IteratingSystem(
    family { all(FactionComponent, PositionComponent, ShipManifestComponent).none(PlayerComponent, StationComponent) }
) {
    private val tempVec = Vector2()
    private val steerVec = Vector2()
    private val aimVec = Vector2()
    private var stateTimer = 0f

    private val playerFamily = family { all(PlayerComponent, PositionComponent) }
    private var cachedPlayerPos: Vector2? = null

    override fun onTick() {
        stateTimer += deltaTime
        // Cache player pos once per tick for all AI units
        cachedPlayerPos = playerFamily.firstOrNull()?.get(PositionComponent)?.pos
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        if (entity[FactionComponent].type != FactionType.ENEMY) return

        val myPos = entity[PositionComponent].pos
        val playerPos = cachedPlayerPos

        var isFiring = false
        // Fleks 2.x: Get component or null without double-lookup/has()
        val order = entity.getOrNull(OrderComponent)

        // Reset vectors
        steerVec.set(0f, 0f)
        aimVec.set(1f, 0f)

        // 1. DETERMINE INTENT (Order vs Heuristic)
        if (order != null && order.type != OrderType.IDLE) {
            when (order.type) {
                OrderType.MOVE_TO -> {
                    steerVec.set(order.targetPos).sub(myPos)
                    if (steerVec.len2() > 0.1f) steerVec.nor()
                    aimVec.set(steerVec)
                }
                OrderType.ATTACK_TARGET -> {
                    val target = order.targetEntity
                    if (target != Entity.NONE && target in world && target.has(PositionComponent)) {
                        val targetPos = target[PositionComponent].pos
                        tempVec.set(targetPos).sub(myPos)
                        val distSq = tempVec.len2()
                        aimVec.set(tempVec).nor()

                        // Tactical approach: close in but stay at range
                        if (distSq > 100f) steerVec.set(tempVec).nor() // 10^2
                        else if (distSq < 25f) steerVec.set(tempVec).nor().scl(-1f) // 5^2

                        isFiring = distSq < 225f // 15^2
                    }
                }
                OrderType.DEFEND_POSITION -> {
                    tempVec.set(order.targetPos).sub(myPos)
                    val distSq = tempVec.len2()
                    if (distSq > 4f) steerVec.set(tempVec).nor() // 2^2

                    // Aim at player if available while defending
                    if (playerPos != null) aimVec.set(playerPos).sub(myPos).nor()
                }
                else -> {}
            }
        } else if (playerPos != null) {
            // DEFAULT HEURISTICS (Maintain Distance)
            tempVec.set(playerPos).sub(myPos)
            val distSq = tempVec.len2()
            aimVec.set(tempVec).nor()

            val cycleTime = (stateTimer + entity.id) % 6f
            val isFiringPhase = cycleTime > 4f
            val orbitDir = if (((entity.id + (stateTimer / 8f).toInt()) % 2) == 0) 1 else -1

            if (!isFiringPhase) {
                when {
                    distSq > 144f -> steerVec.set(tempVec).nor() // 12^2
                    distSq < 36f -> steerVec.set(tempVec).nor().scl(-1f) // 6^2
                    else -> steerVec.set(tempVec).rotate90(orbitDir).nor().scl(0.7f) // Circle
                }
            }
            isFiring = isFiringPhase && distSq < 324f // 18^2
        }

        // 2. BRIDGE SIGNALS TO MANIFEST (O(1) Access, Zero-Allocation Loop)
        val manifest = entity[ShipManifestComponent]
        val modules = manifest.modules
        for (i in 0 until modules.size) {
            val module = modules[i]
            world.run {
                if (module !in world) return@run

                val modComp = module[ModuleComponent]
                val channels = module[ModuleChannelsComponent]

                when (modComp.category) {
                    ModuleCategory.ENGINE -> {
                        channels.inputs[StandardChannels.TARGET]?.vector?.set(steerVec)
                    }
                    ModuleCategory.PRIMARY, ModuleCategory.SECONDARY -> {
                        channels.inputs[StandardChannels.TARGET]?.vector?.set(aimVec)
                        channels.inputs[StandardChannels.TRIGGER]?.triggered = isFiring
                    }
                    else -> {}
                }
            }
        }
    }
}
