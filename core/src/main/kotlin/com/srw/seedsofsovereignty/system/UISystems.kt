package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.SectorManager
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Diegetic HUD System (v2.3.4)
 * ROLE: Stabilized AR Orchestrator for Bottom-Left Origin Viewports.
 *
 * FIXES:
 * 1. [SINGULARITY] - Fixed clumping via direct world-to-UI relative translation.
 * 2. [VISIBILITY] - Corrected WORLD space draw coordinates to restore ship visibility.
 */
class DiegeticHUDSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val worldViewport: Viewport,
    private val uiViewport: Viewport,
    private val skin: Skin,
    private val sectorManager: SectorManager
) : IteratingSystem(family { all(HUDElementComponent) }) {

    private val tempVec = Vector3()
    private val outVec = Vector2()
    private val activeLayers = mutableSetOf<HUDLayer>(HUDLayer.FLIGHT, HUDLayer.SYSTEM, HUDLayer.VFX)

    override fun onTick() {
        super.onTick()
        // Pass 1: WORLD Space
        batch.use(worldViewport.camera.combined) { renderSpace(HUDSpace.WORLD) }
        // Pass 2: SCREEN Space
        batch.use(uiViewport.camera.combined) {
            renderSpace(HUDSpace.SCREEN)
            renderAtmosphere()
        }
        renderTethers()
    }

    private fun renderSpace(space: HUDSpace) {
        world.family { all(HUDElementComponent, HUDRendererComponent) }.forEach { entity ->
            val hud = entity[HUDElementComponent]
            val render = entity[HUDRendererComponent]
            if (hud.space == space && render.isVisible && hud.layer in activeLayers) {
                drawElement(entity, hud, render)
            }
        }
    }

    private fun drawElement(entity: Entity, hud: HUDElementComponent, render: HUDRendererComponent) {
        val x: Float
        val y: Float

        if (hud.space == HUDSpace.SCREEN) {
            val base = getAnchorPosition(hud.anchor)
            x = base.x + hud.offsetX + hud.localX
            y = base.y + hud.offsetY + hud.localY
        } else {
            // WORLD Space: Draw directly at raw physics coords
            val wPos = if (entity.has(PositionComponent)) entity[PositionComponent].pos else Vector2.Zero
            x = wPos.x + hud.localX
            y = wPos.y + hud.localY
        }

        drawer.setColor(1f, 1f, 1f, hud.alpha)
        when (render.renderType) {
            HUDRenderType.ARC -> drawer.arc(x, y, hud.width / 2f, hud.rotation * MathUtils.degRad, MathUtils.PI2 * hud.scaleX, 2f)
            HUDRenderType.BLUEPRINT -> drawer.rectangle(x - hud.width / 2f, y - hud.height / 2f, hud.width, hud.height, 1.5f)
            HUDRenderType.TEXT -> render.localizationKey?.let { skin.getFont("default").draw(batch, it, x, y) }
            else -> {}
        }
    }

    private fun getAnchorPosition(anchor: HUDAnchor): Vector2 {
        val w = uiViewport.worldWidth
        val h = uiViewport.worldHeight
        return when (anchor) {
            HUDAnchor.TOP_LEFT -> outVec.set(0f, h)
            HUDAnchor.TOP_RIGHT -> outVec.set(w, h)
            HUDAnchor.BOTTOM_LEFT -> outVec.set(0f, 0f)
            HUDAnchor.BOTTOM_RIGHT -> outVec.set(w, 0f)
            HUDAnchor.CENTER -> outVec.set(w / 2f, h / 2f)
            HUDAnchor.TOP_CENTER -> outVec.set(w / 2f, h)
            HUDAnchor.BOTTOM_CENTER -> outVec.set(w / 2f, 0f)
            HUDAnchor.LEFT_CENTER -> outVec.set(0f, h / 2f)
            HUDAnchor.RIGHT_CENTER -> outVec.set(w, h / 2f)
        }
    }

    private fun renderTethers() {
        batch.use(uiViewport.camera.combined) {
            world.family { all(HUDElementComponent, HUDLinkComponent) }.forEach { entity ->
                val link = entity[HUDLinkComponent]
                val source = link.sourceEntity ?: return@forEach
                val target = link.targetEntity ?: return@forEach
                if (source !in world || target !in world || !source.has(PositionComponent) || !target.has(HUDElementComponent)) return@forEach

                // PROJECT: Relative world offset -> UI Space
                val sPos = source[PositionComponent].pos
                val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull()
                val pPos = if (player != null) player[PositionComponent].pos else Vector2.Zero

                // Map to screen center + relative world difference
                val sx = (uiViewport.worldWidth / 2f) + (sPos.x - pPos.x)
                val sy = (uiViewport.worldHeight / 2f) + (sPos.y - pPos.y)

                val tHud = target[HUDElementComponent]
                val tBase = getAnchorPosition(tHud.anchor)
                val tx = tBase.x + tHud.offsetX + tHud.localX
                val ty = tBase.y + tHud.offsetY + tHud.localY

                drawer.setColor(0f, 1f, 1f, 0.4f)
                drawer.line(sx, sy, tx, ty, 1.2f)
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        val hud = entity[HUDElementComponent]

        // --- DIRECT WORLD-TO-UI TRACKING ---
        if (entity.has(HUDLinkComponent) && hud.space == HUDSpace.SCREEN) {
            val source = entity[HUDLinkComponent].sourceEntity
            val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull()
            if (source != null && source in world && player != null) {
                val sPos = source[PositionComponent].pos
                val pPos = player[PositionComponent].pos

                // Final fix: Set Screen-space offsets based on the relative world unit difference
                hud.offsetX = (sPos.x - pPos.x)
                hud.offsetY = (sPos.y - pPos.y)
            }
        }

        val pId = hud.parentId
        if (pId != null && pId !in world) { world -= entity; return }
        if (entity.has(HUDAnimationComponent)) { entity[HUDAnimationComponent].elapsedTime += deltaTime }
    }

    fun handleTouch(screenX: Float, screenY: Float): Boolean { return false }

    private fun renderAtmosphere() {
        drawer.setColor(0f, 0f, 0f, 0.1f)
        var yp = 0f
        while (yp < uiViewport.worldHeight) {
            drawer.line(0f, yp, uiViewport.worldWidth, yp, 1f)
            yp += 4f
        }
    }
}

class RadarRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PositionComponent, PlayerComponent, SensorComponent) }) {
    private val targetsFamily by lazy { world.family { all(PositionComponent, VisibleComponent).none(PlayerComponent, ProjectileComponent) } }
    private var timeCount = 0f
    override fun onTick() { timeCount += deltaTime; batch.use(uiViewport.camera.combined) { super.onTick() } }
    override fun onTickEntity(entity: Entity) {
        val playerPos = entity[PositionComponent].pos
        val sensor = entity[SensorComponent]
        // v2.3.4: Realigned to Top-Right corner of Bottom-Left origin viewport
        val centerX = uiViewport.worldWidth - 160f
        val centerY = uiViewport.worldHeight - 160f
        drawer.setColor(0f, 0.15f, 0f, 0.5f); drawer.filledCircle(centerX, centerY, 120f)
        targetsFamily.forEach { target ->
            val targetPos = target[PositionComponent].pos
            val rx = centerX + (targetPos.x - playerPos.x) * (120f / sensor.radius)
            val ry = centerY + (targetPos.y - playerPos.y) * (120f / sensor.radius)
            drawer.setColor(if (target.has(EnemyComponent)) Color.RED else Color.CYAN)
            drawer.filledCircle(rx, ry, 3f)
        }
    }
}

class ApertureSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(PlayerComponent, DockingComponent, ApertureComponent) }) {
    override fun onTick() { batch.use(uiViewport.camera.combined) { super.onTick() } }
    override fun onTickEntity(entity: Entity) {
        val ap = entity[ApertureComponent]
        if (entity[DockingComponent].isDocked) ap.openProgress = (ap.openProgress + 2f * deltaTime).coerceAtMost(1f)
        else ap.openProgress = (ap.openProgress - 2f * deltaTime).coerceAtLeast(0f)
        if (ap.openProgress > 0f) {
            val h = (uiViewport.worldHeight / 2f) * (1f - ap.openProgress)
            drawer.setColor(0f, 0f, 0f, 0.8f)
            drawer.filledRectangle(0f, uiViewport.worldHeight - h, uiViewport.worldWidth, h)
            drawer.filledRectangle(0f, 0f, uiViewport.worldWidth, h)
        }
    }
}
