package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

class SensorVisualSystem : IteratingSystem(family { all(SensorComponent, PlayerComponent, PositionComponent) }) {
    private val visibleTargets by lazy { world.family { all(VisibleComponent, PositionComponent).none(PlayerComponent) } }
    private val arMarkers by lazy { world.family { all(HUDInstrumentTag, HUDTagComponent) } }

    override fun onTick() {
        // 1. Cleanup old AR markers
        arMarkers.forEach { entity ->
            if (entity[HUDTagComponent].ownerId == "SENSOR_AR") {
                world -= entity
            }
        }

        // 2. Process new markers
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val sensorPos = entity[PositionComponent].pos
        val playerLock = if (entity.has(TacticalLockComponent)) entity[TacticalLockComponent].targetEntity else null

        visibleTargets.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val distance = sensorPos.dst(targetPos).toInt()

            // 1. Primary Marker Entity
            val marker = world.entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(
                    space = HUDSpace.SCREEN,
                    anchor = HUDAnchor.CENTER,
                    layer = HUDLayer.SYSTEM,
                    width = 40f,
                    height = 40f
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    isVisible = true
                )
                it += HUDInteractionComponent(
                    isClickable = true,
                    actionKey = "LOCK_TARGET"
                )
                it += HUDLinkComponent(
                    sourceEntity = target,
                    targetEntity = it
                )
            }

            // 2. Intelligence Pane (Hierarchical - only for locked target)
            if (target == playerLock) {
                val targetType = when {
                    target.has(EnemyComponent) -> "HOSTILE"
                    target.has(StationComponent) -> "STATION"
                    target.has(SalvageComponent) -> "CARGO"
                    else -> "UNKNOWN"
                }

                val health = if (target.has(HealthComponent)) {
                    val hp = target[HealthComponent]
                    (hp.current / hp.max * 100).toInt()
                } else null

                val intelText = if (health != null) "$targetType\nSTRUC: $health%" else targetType

                world.entity {
                    it += HUDInstrumentTag()
                    it += HUDTagComponent(ownerId = "SENSOR_AR")
                    it += HUDElementComponent(
                        space = HUDSpace.SCREEN,
                        anchor = HUDAnchor.CENTER,
                        layer = HUDLayer.SYSTEM,
                        parentId = marker,
                        offsetX = 30f,
                        offsetY = -40f
                    )
                    it += HUDRendererComponent(
                        renderType = HUDRenderType.TEXT,
                        localizationKey = intelText,
                        isVisible = true
                    )
                    it += HUDLinkComponent(
                        sourceEntity = target,
                        targetEntity = it
                    )
                }
            }

            // 3. Telemetry Label Entity (Hierarchical - offset from marker)
            world.entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent(ownerId = "SENSOR_AR")
                it += HUDElementComponent(
                    space = HUDSpace.SCREEN,
                    anchor = HUDAnchor.CENTER,
                    layer = HUDLayer.SYSTEM,
                    parentId = marker, // Anchor to primary marker
                    offsetX = 25f,     // Offset to the right of the marker
                    offsetY = 15f      // Offset slightly up
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.TEXT,
                    localizationKey = "[${distance}u]",
                    isVisible = true
                )
                it += HUDLinkComponent(
                    sourceEntity = target,
                    targetEntity = it
                )
            }
        }
    }
}
