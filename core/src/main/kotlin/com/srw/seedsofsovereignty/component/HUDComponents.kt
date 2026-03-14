package com.srw.seedsofsovereignty.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

enum class HUDSpace { SCREEN, WORLD }

enum class HUDAnchor {
    CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP_CENTER, BOTTOM_CENTER, LEFT_CENTER, RIGHT_CENTER
}

enum class HUDRenderType { BLUEPRINT, ARC, ICON, TEXT, TETHER, PANEL_BG, CROSSHAIR }

enum class HUDBlendingMode { NORMAL, ADDITIVE }

enum class HUDLineStyle { SOLID, DASHED, PULSING }

enum class HUDLayer { FLIGHT, MARKET, MAP, SYSTEM, VFX }

/**
 * Technical Specification: HUDComponents (v1.5.0)
 * v1.2.0: Added HUDTagComponent for dynamic lifecycle management.
 * v1.3.0: Added HUDInstrumentTag for specific instrument cleanup.
 * v1.4.0: Added ReticleLayerComponent and TargetCategory (Phase 23.1).
 * v1.5.0: Verified pivot and parenting support for hierarchical UI (Phase 25.5).
 */
data class HUDElementComponent(
    var space: HUDSpace = HUDSpace.SCREEN,
    var anchor: HUDAnchor = HUDAnchor.CENTER,
    var layer: HUDLayer = HUDLayer.FLIGHT,
    var parentId: Entity? = null,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var localX: Float = 0f,
    var localY: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var pivotX: Float = 0.5f,
    var pivotY: Float = 0.5f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var zIndex: Int = 0
) : Component<HUDElementComponent> {
    override fun type() = HUDElementComponent
    companion object : ComponentType<HUDElementComponent>()
}

data class HUDRendererComponent(
    var renderType: HUDRenderType = HUDRenderType.BLUEPRINT,
    var localizationKey: String? = null,
    var styleId: String? = null,
    var blendingMode: HUDBlendingMode = HUDBlendingMode.NORMAL,
    var shaderId: String? = null,
    var isVisible: Boolean = true
) : Component<HUDRendererComponent> {
    override fun type() = HUDRendererComponent
    companion object : ComponentType<HUDRendererComponent>()
}

data class HUDInteractionComponent(
    var isClickable: Boolean = false,
    var isScrollable: Boolean = false,
    var gestures: Int = 0, // Bitmask: TAP=1, LONG_PRESS=2, DOUBLE_TAP=4
    var actionKey: String = ""
) : Component<HUDInteractionComponent> {
    override fun type() = HUDInteractionComponent
    companion object : ComponentType<HUDInteractionComponent>()
}

data class HUDLinkComponent(
    var sourceEntity: Entity? = null,
    var targetEntity: Entity? = null,
    var lineStyle: HUDLineStyle = HUDLineStyle.SOLID
) : Component<HUDLinkComponent> {
    override fun type() = HUDLinkComponent
    companion object : ComponentType<HUDLinkComponent>()
}

data class HUDAnimationComponent(
    var animationId: String = "",
    var elapsedTime: Float = 0f,
    var intensity: Float = 1f,
    var isLooping: Boolean = true
) : Component<HUDAnimationComponent> {
    override fun type() = HUDAnimationComponent
    companion object : ComponentType<HUDAnimationComponent>()
}

/**
 * Identifies HUD entities belonging to a specific system or module.
 * Used for mass cleanup during module swaps.
 */
data class HUDTagComponent(
    var ownerId: String = ""
) : Component<HUDTagComponent> {
    override fun type() = HUDTagComponent
    companion object : ComponentType<HUDTagComponent>()
}

/**
 * Tag component for identifying dynamically spawned HUD instruments.
 */
class HUDInstrumentTag : Component<HUDInstrumentTag> {
    override fun type() = HUDInstrumentTag
    companion object : ComponentType<HUDInstrumentTag>()
}

/**
 * Tracks the state and type of an active diegetic AR menu.
 */
data class HUDMenuComponent(
    var menuId: String = "NONE",
    var isFocused: Boolean = false,
    var scrollProgress: Float = 0f
) : Component<HUDMenuComponent> {
    override fun type() = HUDMenuComponent
    companion object : ComponentType<HUDMenuComponent>()
}

data class ReticleLayerComponent(
    var category: TargetCategory = TargetCategory.HOSTILE,
    var priority: Int = 0,
    var isVisible: Boolean = true
) : Component<ReticleLayerComponent> {
    override fun type() = ReticleLayerComponent
    companion object : ComponentType<ReticleLayerComponent>()
}

enum class TargetCategory(val bitmask: Long) {
    HOSTILE(1L shl 0),   // Bit 0: Enemy ships, turrets
    NEUTRAL(1L shl 1),   // Bit 1: Debris, salvage, asteroids
    FRIENDLY(1L shl 2),  // Bit 2: Drones, wingmen
    OBJECTIVE(1L shl 3)  // Bit 3: Stations, mission targets
}
