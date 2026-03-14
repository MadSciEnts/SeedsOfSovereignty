# SURGICAL IMPLEMENTATION PLAN (v2.6.0)

## MISSION: Phase 25.3 Recovery - Modular HUD Refactor
**Status:** [ 0 / 3 : INITIATED ]

## TASK 1: CREATE HUDCOORDINATERESOLVER
**Target File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/util/HUDCoordinateResolver.kt`
**Action:** Physically create the utility for hierarchical AR positioning.

### 1.1 Implementation Detail
Extract the coordinate resolution logic from `UISystems.kt`.

```kotlin
package com.srw.seedsofsovereignty.ui.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.HUDAnchor
import com.srw.seedsofsovereignty.component.HUDElementComponent

object HUDCoordinateResolver {
    private val outVec = Vector2()

    fun getAnchorPosition(anchor: HUDAnchor, viewport: Viewport): Vector2 {
        val w = viewport.worldWidth; val h = viewport.worldHeight
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

    fun getAbsolutePosition(entity: Entity, world: World, viewport: Viewport, result: Vector2) {
        result.set(0f, 0f)
        accumulatePosition(entity, world, viewport, result)
    }

    private fun accumulatePosition(entity: Entity, world: World, viewport: Viewport, result: Vector2) {
        with(world) {
            val hud = entity[HUDElementComponent]
            val pId = hud.parentId
            if (pId != null && pId in world) {
                accumulatePosition(pId, world, viewport, result)
                val p = pId[HUDElementComponent]
                result.add(p.offsetX + p.localX, p.offsetY + p.localY)
            } else {
                val anchorPos = getAnchorPosition(hud.anchor, viewport)
                result.set(anchorPos.x, anchorPos.y)
            }
        }
    }
}
```

## TASK 2: CREATE HUDRENDERDELEGATE
**Target File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/render/HUDRenderDelegate.kt`
**Action:** Physically create the delegate for specialized AR drawing.

### 2.1 Implementation Detail
Extract the `drawElement` logic from `UISystems.kt`.

```kotlin
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

object HUDRenderDelegate {
    fun drawElement(entity: Entity, world: World, batch: SpriteBatch, drawer: ShapeDrawer, skin: Skin, absPos: Vector2) {
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
                    player?.get(TacticalHubComponent)?.let { hub ->
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
```

## TASK 3: REFACTOR UISYSTEMS
**Target File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/UISystems.kt`
**Action:** Replace extracted logic with calls to the Resolver and Delegate.
