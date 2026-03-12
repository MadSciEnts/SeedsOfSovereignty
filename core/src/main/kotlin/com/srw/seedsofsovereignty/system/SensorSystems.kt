package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Technical Logic pass for Player Sensors (v2.7.7).
 * ROLE: Primary handshake for entity visibility and categorical initialization.
 */
class SensorSystem : IteratingSystem(family { all(SensorComponent, PlayerComponent, PositionComponent) }) {
    private val detectables by lazy { world.family { all(PositionComponent).none(PlayerComponent, VisibleComponent) } }
    private val visibleEntities by lazy { world.family { all(VisibleComponent, PositionComponent).none(PlayerComponent) } }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val sensor = entity[SensorComponent]

        // 1. DISCOVERY: Entities entering range gain VisibleComponent and ReticleLayer (v2.7.7)
        detectables.forEach { detectable ->
            if (playerPos.dst(detectable[PositionComponent].pos) <= sensor.radius) {
                detectable.configure {
                    it += VisibleComponent()
                    // Categorical HUD Initialization: Assign target category based on components
                    if (!detectable.has(ReticleLayerComponent)) {
                        val cat = when {
                            detectable.has(EnemyComponent) -> TargetCategory.HOSTILE
                            detectable.has(DroneComponent) -> TargetCategory.FRIENDLY
                            detectable.has(StationComponent) -> TargetCategory.OBJECTIVE
                            else -> TargetCategory.NEUTRAL
                        }
                        it += ReticleLayerComponent(category = cat)
                    }
                }
            }
        }

        // 2. CULLING: Entities leaving range lose VisibleComponent
        visibleEntities.forEach { visible ->
            if (playerPos.dst(visible[PositionComponent].pos) > sensor.radius) {
                visible.configure { it -= VisibleComponent }
            }
        }
    }
}

/**
 * Diegetic AR Marker System (v2.7.7).
 * ROLE: Manages persistent AR markers for detected entities with distance-based falloff.
 */
class SensorVisualSystem : IteratingSystem(family { all(SensorComponent, PlayerComponent, PositionComponent) }) {
    private val visibleTargets by lazy { world.family { all(VisibleComponent, PositionComponent).none(PlayerComponent) } }
    private val arMarkers by lazy { world.family { all(HUDInstrumentTag, HUDTagComponent) } }

    override fun onTick() {
        // CLEANUP: frame-clear markers (State-sync planned for Phase 23)
        arMarkers.forEach { entity ->
            if (entity[HUDTagComponent].ownerId == "SENSOR_AR") {
                world -= entity
            }
        }
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val sensorPos = entity[PositionComponent].pos
        val playerLock = if (entity.has(TacticalLockComponent)) entity[TacticalLockComponent].targetEntity else null

        // GSF 0.1 HUD Filtering: Default to [ENEMY-ONLY] (Bitmask 0x1)
        val hub = if (entity.has(TacticalHubComponent)) entity[TacticalHubComponent] else null
        val filterMask = hub?.scalars?.get("HUD_FILTER")?.toLong() ?: TargetCategory.HOSTILE.bitmask

        visibleTargets.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val dist = sensorPos.dst(targetPos)

            // 1. Categorical Filtering: Check if target's category is enabled in the filter bitmask
            if (!target.has(ReticleLayerComponent)) {
                // Fallback initialization if missed by SensorSystem
                val cat = when {
                    target.has(EnemyComponent) -> TargetCategory.HOSTILE
                    target.has(DroneComponent) -> TargetCategory.FRIENDLY
                    target.has(StationComponent) -> TargetCategory.OBJECTIVE
                    else -> TargetCategory.NEUTRAL
                }
                target.configure { it += ReticleLayerComponent(category = cat) }
            }
            val category = target[ReticleLayerComponent].category
            if ((filterMask and category.bitmask) == 0L) return@forEach

            // 2. Engagement Alpha: 1.0 (64u) to 0.0 (96u) falloff logic (v2.7.7)
            val alpha = 1.0f - MathUtils.clamp((dist - 64f) / (96f - 64f), 0f, 1f)
            if (alpha <= 0.05f) return@forEach

            val distance = dist.toInt()

            // Primary Marker (Z: 2 per HUD_ARCHITECTURE.md)
            val marker = world.entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, width = 40f, height = 40f, zIndex = 2, alpha = alpha)
                it += HUDRendererComponent(renderType = HUDRenderType.BLUEPRINT, isVisible = true)
                it += HUDInteractionComponent(isClickable = true, actionKey = "LOCK_TARGET")
                it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
                it += ReticleLayerComponent(category = category)
            }

            // Intelligence Pane (Only for locked target)
            if (target == playerLock) {
                val targetType = when {
                    target.has(EnemyComponent) -> "HOSTILE"
                    target.has(StationComponent) -> "STATION"
                    else -> "OBJECT"
                }
                world.entity {
                    it += HUDInstrumentTag()
                    it += HUDTagComponent(ownerId = "SENSOR_AR")
                    it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, parentId = marker, offsetX = 30f, offsetY = -40f, zIndex = 2, alpha = alpha)
                    it += HUDRendererComponent(renderType = HUDRenderType.TEXT, localizationKey = targetType, isVisible = true)
                    it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
                }
            }

            // Telemetry Label
            world.entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, parentId = marker, offsetX = 25f, offsetY = 15f, zIndex = 2, alpha = alpha)
                it += HUDRendererComponent(renderType = HUDRenderType.TEXT, localizationKey = "[${distance}u]", isVisible = true)
                it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
            }
        }
    }
}
