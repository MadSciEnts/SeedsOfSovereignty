package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.ui.render.HUDRenderDelegate
import com.srw.seedsofsovereignty.ui.util.HUDCoordinateResolver
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Diegetic HUD System (v3.0.0)
 * ROLE: The "Gold Standard" AR Orchestrator.
 * UPDATES:
 * 1. [GSF 0.1] - Scaled world-to-UI projection for physical units.
 * 2. [MODULAR] - Extracted RenderDelegate and CoordinateResolver (Phase 25.3 Recovery).
 */
class DiegeticHUDSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val worldViewport: Viewport,
    private val uiViewport: Viewport,
    private val skin: Skin,
    private val sectorManager: SectorManager
) : IteratingSystem(family { all(HUDElementComponent) }) {

    private val absPosVec = Vector2()
    private val activeLayers = mutableSetOf<HUDLayer>(HUDLayer.FLIGHT, HUDLayer.SYSTEM, HUDLayer.VFX)

    override fun onTick() {
        super.onTick()
        batch.use(uiViewport.camera.combined) {
            drawer.update()
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
                HUDCoordinateResolver.getAbsolutePosition(entity, world, uiViewport, absPosVec)
                HUDRenderDelegate.drawElement(entity, world, batch, drawer, skin, absPosVec)
            }
        }
    }

    fun handleTouch(screenX: Float, screenY: Float): Boolean {
        val touchX = screenX
        val touchY = Gdx.graphics.height - screenY

        var hitHandled = false
        world.family { all(HUDElementComponent, HUDInteractionComponent, HUDRendererComponent) }.forEach { entity ->
            if (hitHandled) return@forEach

            val hud = entity[HUDElementComponent]
            val interact = entity[HUDInteractionComponent]
            val render = entity[HUDRendererComponent]

            if (render.isVisible && interact.isClickable) {
                HUDCoordinateResolver.getAbsolutePosition(entity, world, uiViewport, absPosVec)
                val ax = absPosVec.x + hud.offsetX + hud.localX
                val ay = absPosVec.y + hud.offsetY + hud.localY

                val halfW = hud.width / 2f
                val halfH = hud.height / 2f

                if (touchX >= ax - halfW && touchX <= ax + halfW &&
                    touchY >= ay - halfH && touchY <= ay + halfH) {
                    processDiegeticHit(world, interact.actionKey)
                    hitHandled = true
                }
            }
        }
        return hitHandled
    }

    private fun renderTethers() {
        batch.use(uiViewport.camera.combined) {
            drawer.update()
            val zoom = (worldViewport.camera as OrthographicCamera).zoom
            val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull()
            val pPos = player?.get(PositionComponent)?.pos ?: Vector2.Zero

            world.family { all(HUDElementComponent, HUDLinkComponent) }.forEach { entity ->
                val link = entity[HUDLinkComponent]
                val source = link.sourceEntity ?: return@forEach
                val target = link.targetEntity ?: return@forEach
                if (source !in world || target !in world || !source.has(PositionComponent)) return@forEach

                val sPos = source[PositionComponent].pos
                val sx = (uiViewport.worldWidth / 2f) + ((sPos.x - pPos.x) * 10f) / zoom
                val sy = (uiViewport.worldHeight / 2f) + ((sPos.y - pPos.y) * 10f) / zoom

                HUDCoordinateResolver.getAbsolutePosition(target, world, uiViewport, absPosVec)
                val tx = absPosVec.x + target[HUDElementComponent].offsetX + target[HUDElementComponent].localX
                val ty = absPosVec.y + target[HUDElementComponent].offsetY + target[HUDElementComponent].localY

                drawer.setColor(1f, 1f, 1f, 0.4f)
                drawer.line(sx, sy, tx, ty, 1.2f)
            }
            drawer.setColor(Color.WHITE)
        }
    }

    override fun onTickEntity(entity: Entity) {
        val hud = entity[HUDElementComponent]

        if (entity.has(HUDLinkComponent) && hud.space == HUDSpace.SCREEN) {
            val source = entity[HUDLinkComponent].sourceEntity
            val player = world.family { all(PlayerComponent, PositionComponent) }.firstOrNull()
            if (source != null && source in world && source.has(PositionComponent) && player != null) {
                val zoom = (worldViewport.camera as OrthographicCamera).zoom
                val sPos = source[PositionComponent].pos
                val pPos = player[PositionComponent].pos
                hud.offsetX = ((sPos.x - pPos.x) * 10f) / zoom
                hud.offsetY = ((sPos.y - pPos.y) * 10f) / zoom
            }
        }

        val pId = hud.parentId
        if (pId != null && pId !in world) { world -= entity; return }
        if (entity.has(HUDAnimationComponent)) { entity[HUDAnimationComponent].elapsedTime += deltaTime }
    }

    private fun renderAtmosphere() {
        val oldSrc = batch.blendSrcFunc; val oldDst = batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        drawer.setColor(0f, 0.2f, 0.3f, 0.05f)
        var yp = 0f
        while (yp < uiViewport.worldHeight) {
            drawer.line(0f, yp, uiViewport.worldWidth, yp, 1f); yp += 4f
        }
        batch.setBlendFunction(oldSrc, oldDst)
        drawer.setColor(Color.WHITE)
    }
}
