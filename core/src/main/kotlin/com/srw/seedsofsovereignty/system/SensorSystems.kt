package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Technical Logic pass for Player Sensors (v2.4.4).
 * ROLE: Primary handshake for entity visibility.
 */
class SensorSystem : IteratingSystem(family { all(SensorComponent, PlayerComponent, PositionComponent) }) {
    private val detectables by lazy { world.family { all(PositionComponent).none(PlayerComponent, VisibleComponent) } }
    private val visibleEntities by lazy { world.family { all(VisibleComponent, PositionComponent).none(PlayerComponent) } }

    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val sensor = entity[SensorComponent]

        // 1. DISCOVERY: Entities entering range gain VisibleComponent
        detectables.forEach { detectable ->
            if (playerPos.dst(detectable[PositionComponent].pos) <= sensor.radius) {
                detectable.configure { it += VisibleComponent() }
            }
        }

        // 2. CULLING: Entities leaving range lose VisibleComponent
        visibleEntities.forEach { visible ->
            if (playerPos.dst(visible[PositionComponent].pos) > sensor.radius) {
                // FIXED: Use ComponentType for removal
                visible.configure { it -= VisibleComponent }
            }
        }
    }
}

/**
 * Diegetic AR Marker System (v2.4.4).
 * ROLE: Manages persistent AR markers for detected entities.
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

        visibleTargets.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val distance = sensorPos.dst(targetPos).toInt()

            // Primary Marker (Z: 2 per HUD_ARCHITECTURE.md)
            val marker = world.entity {
                it += HUDInstrumentTag(); it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, width = 40f, height = 40f, zIndex = 2)
                it += HUDRendererComponent(renderType = HUDRenderType.BLUEPRINT, isVisible = true)
                it += HUDInteractionComponent(isClickable = true, actionKey = "LOCK_TARGET")
                it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
            }

            // Intelligence Pane (Only for locked target)
            if (target == playerLock) {
                val targetType = when {
                    target.has(EnemyComponent) -> "HOSTILE"
                    target.has(StationComponent) -> "STATION"
                    else -> "OBJECT"
                }
                world.entity {
                    it += HUDInstrumentTag(); it += HUDTagComponent(ownerId = "SENSOR_AR")
                    it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, parentId = marker, offsetX = 30f, offsetY = -40f, zIndex = 2)
                    it += HUDRendererComponent(renderType = HUDRenderType.TEXT, localizationKey = targetType, isVisible = true)
                    it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
                }
            }

            // Telemetry Label
            world.entity {
                it += HUDInstrumentTag(); it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(space = HUDSpace.SCREEN, anchor = HUDAnchor.CENTER, parentId = marker, offsetX = 25f, offsetY = 15f, zIndex = 2)
                it += HUDRendererComponent(renderType = HUDRenderType.TEXT, localizationKey = "[${distance}u]", isVisible = true)
                it += HUDLinkComponent(sourceEntity = target, targetEntity = it)
            }
        }
    }
}
