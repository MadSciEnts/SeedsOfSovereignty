package com.srw.seedsofsovereignty.ui.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * HUD Render Delegate (v1.2.0)
 * ROLE: Specialized AR drawing logic for individual HUD elements.
 */
object HUDRenderDelegate {
    fun drawElement(entity: Entity, world: World, batch: SpriteBatch, drawer: ShapeDrawer, skin: Skin, absPos: Vector2) {
        with(world) {
            val hud = entity[HUDElementComponent]
            val render = entity[HUDRendererComponent]
            val x = absPos.x + hud.offsetX + hud.localX
            val y = absPos.y + hud.offsetY + hud.localY

            val drawColor = when(render.styleId) {
                "CORE_YELLOW" -> Color.YELLOW
                "VANGUARD" -> Color.CYAN
                "SWARM" -> Color.LIME
                "INDUSTRIAL" -> Color.CYAN
                else -> Color.WHITE
            }
            drawer.setColor(drawColor.r, drawColor.g, drawColor.b, hud.alpha)

            when (render.renderType) {
                HUDRenderType.ARC -> drawer.arc(x, y, hud.width / 2f, hud.rotation * MathUtils.degRad, MathUtils.PI2 * hud.scaleX, 2f)
                HUDRenderType.BLUEPRINT -> drawer.rectangle(x - hud.width / 2f, y - hud.height / 2f, hud.width, hud.height, 1.5f)
                HUDRenderType.TEXT -> {
                    var displayText = render.localizationKey ?: ""
                    if (displayText.startsWith("ACTIVE: ")) {
                        val player = world.family { all(PlayerComponent, TacticalHubComponent) }.firstOrNull()
                        player?.let { p ->
                            val hub = p[TacticalHubComponent]
                            val signal = hub.signals["DRONES_ACTIVE_${hub.selectedModuleIndex}"]
                            if (signal != null) displayText = "ACTIVE: ${signal.value.toInt()}"
                        }
                    }
                    skin.getFont("default").draw(batch, displayText, x, y)
                }
                else -> {}
            }
        }
    }
}
