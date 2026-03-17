package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

class WorldRenderSystem(
    private val batch: Batch = inject("batch"),
    private val drawer: ShapeDrawer = inject("drawer")
) : IteratingSystem(
    family { any(PositionComponent, TextureRegionComponent, CompositeVisualComponent) }
) {
    private val barrelVec = Vector2()
    private var totalTime = 0f

    override fun onTick() {
        totalTime += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        if (!entity.has(PositionComponent)) return
        val pos = entity[PositionComponent].pos

        // 1. Draw CompositeVisualComponent (LAYERED RENDERING)
        if (entity.has(CompositeVisualComponent)) {
            val composite = entity[CompositeVisualComponent]
            val baseScale = if (entity.has(TextureRegionComponent)) entity[TextureRegionComponent].scale else 1f
            val baseRotation = if (entity.has(TextureRegionComponent)) entity[TextureRegionComponent].rotation else 0f

            // Sort layers by zIndex before drawing
            composite.layers.sortBy { it.zIndex }

            composite.layers.forEach { layer ->
                val region = layer.region
                val pixelWidth = region.regionWidth.toFloat()
                val pixelHeight = region.regionHeight.toFloat()

                val worldWidth = baseScale * layer.offsetScale
                val worldHeight = (baseScale * layer.offsetScale) * (pixelHeight / pixelWidth)

                // Neon Pulsing effect
                if (layer.pulse) {
                    val alpha = 0.6f + 0.4f * MathUtils.sin(totalTime * 5f)
                    batch.setColor(layer.color.r, layer.color.g, layer.color.b, layer.color.a * alpha)
                } else {
                    batch.color = layer.color
                }

                batch.draw(
                    region,
                    pos.x - worldWidth / 2f,
                    pos.y - worldHeight / 2f,
                    worldWidth / 2f,
                    worldHeight / 2f,
                    worldWidth,
                    worldHeight,
                    1f,
                    1f,
                    baseRotation + layer.rotationOffset
                )
            }
            batch.color = Color.WHITE // Reset batch color
        }
        // 2. Draw Legacy TextureRegionComponent (TRON STYLE)
        else if (entity.has(TextureRegionComponent)) {
            val tex = entity[TextureRegionComponent]
            tex.region?.let { region ->
                val pixelWidth = region.regionWidth.toFloat()
                val pixelHeight = region.regionHeight.toFloat()

                val worldWidth = tex.scale
                val worldHeight = tex.scale * (pixelHeight / pixelWidth)

                batch.color = tex.color
                batch.draw(
                    region,
                    pos.x - worldWidth / 2f,
                    pos.y - worldHeight / 2f,
                    worldWidth / 2f,
                    worldHeight / 2f,
                    worldWidth,
                    worldHeight,
                    1f,
                    1f,
                    tex.rotation
                )
                batch.color = Color.WHITE
            }
        }

        // 3. Draw Weapon Barrels
        if (entity.has(BarrelVisualComponent) && entity.has(WeaponModuleComponent)) {
            val barrel = entity[BarrelVisualComponent]
            val weapon = entity[WeaponModuleComponent]

            val hullRotation = when {
                entity.has(AttachmentComponent) -> {
                    val parent = entity.get(AttachmentComponent).parent
                    if (parent != Entity.NONE && parent in world) {
                        if (parent.has(TextureRegionComponent)) parent.get(TextureRegionComponent).rotation else 0f
                    } else 0f
                }
                else -> 0f
            }

            var platformRotation = hullRotation + 90f
            if (entity.has(ModuleChannelsComponent)) {
                val channels = entity.get(ModuleChannelsComponent)
                val aimInput = channels.inputs["AIM"]
                if (aimInput != null && aimInput.vector.len2() > 0.01f) {
                    platformRotation = aimInput.vector.angleDeg()
                    barrel.platformRotation = platformRotation
                } else {
                    platformRotation = barrel.platformRotation
                }
            }

            drawer.setColor(barrel.color)
            weapon.barrelOffsets.forEach { offset ->
                barrelVec.set(offset).rotateDeg(platformRotation - 90f)
                val bx = pos.x + barrelVec.x
                val by = pos.y + barrelVec.y

                drawer.line(bx, by,
                    bx + MathUtils.cosDeg(platformRotation) * barrel.length,
                    by + MathUtils.sinDeg(platformRotation) * barrel.length,
                    barrel.width)
            }
        }
    }
}

class TrailRenderSystem(
    private val drawer: ShapeDrawer = inject("drawer")
) : IteratingSystem(
    family { all(TrailComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        if (trail.activeCount < 2) return
        for (i in 0 until trail.activeCount - 1) {
            val idx1 = (trail.nextIndex - trail.activeCount + i + trail.maxPoints) % trail.maxPoints
            val idx2 = (idx1 + 1) % trail.maxPoints
            val p1 = trail.points[idx1]
            val p2 = trail.points[idx2]
            val age1 = trail.ages[idx1]
            val age2 = trail.ages[idx2]
            val alpha1 = 1f - (age1 / trail.pointLifetime)
            val alpha2 = 1f - (age2 / trail.pointLifetime)
            if (alpha1 > 0 && alpha2 > 0) {
                val color = trail.color.cpy()
                color.a *= (alpha1 + alpha2) / 2f
                drawer.setColor(color)
                drawer.line(p1.x, p1.y, p2.x, p2.y, trail.width)
            }
        }
    }
}
