package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.factory.ProjectileFactory
import kotlin.math.min

/**
 * SHIP EXECUTION SYSTEM: The unified hardware bus for all ships.
 * Implements the 3-Phase Hardware Lifecycle: Resource -> Distribution -> Function.
 */
class ShipExecutionSystem(
    private val projectileFactory: ProjectileFactory = inject()
) : IteratingSystem(
    family { all(ShipManifestComponent, ShipStateBusComponent, VelocityComponent) }
) {
    private val tempVec = Vector2()
    private var totalTime = 0f

    override fun onTick() {
        totalTime += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val manifest = entity[ShipManifestComponent]
        val bus = entity[ShipStateBusComponent]
        val velocity = entity[VelocityComponent]

        // --- PHASE 0: MAINTENANCE (Self-Healing) ---
        if (totalTime > manifest.lastMaintenance + 5f) {
            manifest.modules.removeAll { it !in world }
            manifest.lastMaintenance = totalTime
        }

        // --- PHASE 1: RESOURCE GENERATION (Capacity) ---
        bus.totalOutput = 0f
        manifest.modules.forEach { module ->
            if (module !in world) return@forEach
            world.run {
                if (module.has(PowerCoreComponent)) {
                    val core = module[PowerCoreComponent]
                    if (module[ModuleComponent].isActive) {
                        bus.totalOutput += core.totalOutput * core.integrity
                    }
                }
            }
        }

        // --- PHASE 2: DISTRIBUTION (Allocation) ---
        // Calculate categorical power levels using ShipPowerPriorityComponent
        val priorityComp = entity.getOrNull(ShipPowerPriorityComponent)
        if (priorityComp != null) {
            var totalPriority = 0f
            PowerCategory.entries.forEach { cat ->
                if (cat != PowerCategory.NONE) {
                    totalPriority += priorityComp.priorities[cat.ordinal]
                }
            }

            if (totalPriority > 0) {
                PowerCategory.entries.forEach { cat ->
                    if (cat != PowerCategory.NONE) {
                        val share = priorityComp.priorities[cat.ordinal] / totalPriority
                        bus.powerLevels[cat.ordinal] = bus.totalOutput * share
                    }
                }
            } else {
                // Fallback to zero if no priorities set
                bus.powerLevels.fill(0f)
            }
        } else {
            // Default distribution if no priority component (e.g. legacy/simple ships)
            val div = 4.0f
            PowerCategory.entries.forEach { cat ->
                if (cat != PowerCategory.NONE) {
                    bus.powerLevels[cat.ordinal] = (bus.totalOutput / div)
                }
            }
        }

        // --- PHASE 3: FUNCTIONAL EXECUTION ---
        manifest.modules.forEach { module ->
            if (module !in world) return@forEach
            executeModuleLogic(entity, module, bus, velocity)
        }
    }

    private fun executeModuleLogic(ship: Entity, module: Entity, bus: ShipStateBusComponent, velocity: VelocityComponent) {
        world.run {
            val channels = module[ModuleChannelsComponent]
            val modComp = module[ModuleComponent]

            // Resource Efficiency Calculation (Power vs Demand)
            val powerAvailable = bus.powerLevels[modComp.powerCategory.ordinal]
            val demand = 1.0f // T1 baseline demand
            val efficiency = if (demand > 0) min(1.0f, powerAvailable / demand) else 1.0f

            when (channels.type) {
                ModuleType.ENGINE_IMPULSE -> {
                    val input = channels.inputs[StandardChannels.TARGET] ?: return@run
                    val thrust = 25f * efficiency
                    tempVec.set(input.vector).scl(thrust * deltaTime)
                    velocity.v.add(tempVec)
                }

                ModuleType.KINETIC_CANNON -> {
                    val launcher = module.getOrNull(WeaponLauncherComponent) ?: return@run
                    val payload = module.getOrNull(ProjectilePayloadComponent) ?: return@run
                    val trigger = channels.inputs[StandardChannels.TRIGGER]?.triggered ?: false

                    if (launcher.currentCooldown > 0) launcher.currentCooldown -= deltaTime

                    if (trigger && launcher.currentCooldown <= 0) {
                        val faction = ship.getOrNull(FactionComponent)?.type ?: FactionType.NEUTRAL
                        val pos = module.getOrNull(PositionComponent)?.pos ?: ship[PositionComponent].pos
                        val aim = channels.inputs[StandardChannels.TARGET]?.vector ?: Vector2(0f, 1f)

                        projectileFactory.spawn(world, payload.blueprint, faction, pos, Vector2.Zero, aim.angleDeg())
                        launcher.currentCooldown = launcher.fireRate / efficiency
                    }
                }

                ModuleType.SHIELD_GEN -> {
                    val defense = ship.getOrNull(ArcedDefenseComponent) ?: return@run
                    val regenRate = 5f * efficiency * deltaTime

                    // Check for direction overrides (manual bracing)
                    val fore = channels.inputs["FORE"]?.triggered ?: false
                    val aft = channels.inputs["AFT"]?.triggered ?: false
                    val port = channels.inputs["PORT"]?.triggered ?: false
                    val stbd = channels.inputs["STBD"]?.triggered ?: false

                    if (fore || aft || port || stbd) {
                        // Targeted regeneration (focused power)
                        val activeCount = (if(fore) 1 else 0) + (if(aft) 1 else 0) + (if(port) 1 else 0) + (if(stbd) 1 else 0)
                        val focusedRegen = regenRate / activeCount
                        if (fore) defense.shieldFore = min(100f, defense.shieldFore + focusedRegen)
                        if (aft) defense.shieldAft = min(100f, defense.shieldAft + focusedRegen)
                        if (port) defense.shieldPort = min(100f, defense.shieldPort + focusedRegen)
                        if (stbd) defense.shieldStbd = min(100f, defense.shieldStbd + focusedRegen)
                    } else {
                        // Passive omni-regeneration
                        val omniRegen = regenRate / 4f
                        defense.shieldFore = min(100f, defense.shieldFore + omniRegen)
                        defense.shieldAft = min(100f, defense.shieldAft + omniRegen)
                        defense.shieldPort = min(100f, defense.shieldPort + omniRegen)
                        defense.shieldStbd = min(100f, defense.shieldStbd + omniRegen)
                    }

                    channels.outputs["CHARGE"]?.status = (defense.shieldFore + defense.shieldAft + defense.shieldPort + defense.shieldStbd) / 400f
                }

                ModuleType.DRONE_HIVE -> {
                    val launcher = module.getOrNull(WeaponLauncherComponent) ?: return@run
                    val trigger = channels.inputs["LAUNCH"]?.triggered ?: false

                    if (launcher.currentCooldown > 0) launcher.currentCooldown -= deltaTime

                    if (trigger && launcher.currentCooldown <= 0) {
                        // T2 logic: Just increment output for now to simulate "active drones"
                        val countOut = channels.outputs["COUNT"] ?: return@run
                        countOut.status = min(1.0f, countOut.status + 0.2f)
                        launcher.currentCooldown = launcher.fireRate / efficiency
                    }
                }
                else -> {}
            }
        }
    }
}
