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
    family { any(PositionComponent, TextureRegionComponent, CompositeVisualComponent, ShapeComponent) }
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

        // 1. Draw CompositeVisualComponent (LAYERED RENDERING & AURAS)
        if (entity.has(CompositeVisualComponent)) {
            val composite = entity[CompositeVisualComponent]

            val baseScale = when {
                entity.has(TextureRegionComponent) -> entity[TextureRegionComponent].scale
                entity.has(ShapeComponent) -> entity[ShapeComponent].size
                else -> 1f
            }

            val baseRotation = when {
                entity.has(TextureRegionComponent) -> entity[TextureRegionComponent].rotation
                entity.has(ShapeComponent) -> entity[ShapeComponent].rotation
                else -> 0f
            }

            composite.layers.sortBy { it.zIndex }

            composite.layers.forEach { layer ->
                val region = layer.region
                val pixelWidth = region.regionWidth.toFloat()
                val pixelHeight = region.regionHeight.toFloat()

                val worldWidth = baseScale * layer.offsetScale
                val worldHeight = (baseScale * layer.offsetScale) * (pixelHeight / pixelWidth)

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
            batch.color = Color.WHITE
        }

        // 2. Draw ShapeComponent (CORE PROJECTILES & PRIMITIVES)
        if (entity.has(ShapeComponent)) {
            val shape = entity[ShapeComponent]
            drawer.setColor(shape.color)

            when (shape.type) {
                ShapeType.SQUARE -> drawer.rectangle(pos.x - shape.size / 2f, pos.y - shape.size / 2f, shape.size, shape.size, 0.02f)
                ShapeType.CIRCLE -> drawer.filledCircle(pos.x, pos.y, shape.size)
                ShapeType.TRIANGLE -> {
                    val r = shape.size
                    drawer.filledTriangle(
                        pos.x + MathUtils.cosDeg(shape.rotation) * r, pos.y + MathUtils.sinDeg(shape.rotation) * r,
                        pos.x + MathUtils.cosDeg(shape.rotation + 140f) * r, pos.y + MathUtils.sinDeg(shape.rotation + 140f) * r,
                        pos.x + MathUtils.cosDeg(shape.rotation + 220f) * r, pos.y + MathUtils.sinDeg(shape.rotation + 220f) * r
                    )
                }
                ShapeType.HEXAGON -> drawer.polygon(pos.x, pos.y, 6, shape.size)
            }
        }

        // 3. Draw Legacy TextureRegionComponent
        if (entity.has(TextureRegionComponent)) {
            val tex = entity[TextureRegionComponent]
            tex.region?.let { region ->
                val pixelWidth = region.regionWidth.toFloat()
                val pixelHeight = region.regionHeight.toFloat()
                val worldWidth = tex.scale
                val worldHeight = tex.scale * (pixelHeight / pixelWidth)

                batch.color = tex.color
                batch.draw(
                    region, pos.x - worldWidth / 2f, pos.y - worldHeight / 2f,
                    worldWidth / 2f, worldHeight / 2f, worldWidth, worldHeight,
                    1f, 1f, tex.rotation
                )
                batch.color = Color.WHITE
            }
        }

        // 4. Draw Weapon Barrels (Modular Sync)
        if (entity.has(BarrelVisualComponent) && entity.has(WeaponLauncherComponent)) {
            val barrel = entity[BarrelVisualComponent]
            val launcher = entity[WeaponLauncherComponent]

            val hullRotation = when {
                entity.has(AttachmentComponent) -> {
                    val parent = entity.get(AttachmentComponent).parent
                    if (parent != Entity.NONE && parent in world) {
                        world.run {
                            if (parent.has(TextureRegionComponent)) parent.get(TextureRegionComponent).rotation else 0f
                        }
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
            launcher.barrelOffsets.forEach { offset ->
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

/**
 * TRAIL RENDER SYSTEM: Finalized with Baked Emission Jitter and Inverted Taper.
 */
class TrailRenderSystem(
    private val drawer: ShapeDrawer = inject("drawer")
) : IteratingSystem(
    family { all(TrailComponent) }
) {
    private val j1 = Vector2()
    private val j2 = Vector2()

    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        if (trail.activeCount < 2) return

        val baseAlpha = trail.color.a
        val count = trail.activeCount

        for (i in 0 until count - 1) {
            // idx1 starts at oldest (tail), idx2 is next point toward newest (head)
            val idx1 = (trail.nextIndex - count + i + trail.maxPoints) % trail.maxPoints
            val idx2 = (idx1 + 1) % trail.maxPoints

            val p1 = trail.points[idx1]
            val p2 = trail.points[idx2]

            // Baked jitter sampled at emission
            val jitter1 = trail.jitters[idx1]
            val jitter2 = trail.jitters[idx2]

            // Progress: 0.0 at TAIL (oldest), 1.0 at HEAD (newest/ship)
            val headProgress = i.toFloat() / (count - 1)
            val tailProgress = 1.0f - headProgress

            // 1. APPLY BAKED JITTER: Scale by tailProgress (older points drift more)
            // Multiplier 3.0f for visible but controlled "exhaust drift"
            j1.set(jitter1).scl(tailProgress * 1f)
            j2.set(jitter2).scl(tailProgress * 1f)

            // 2. TAPERING: thickest at ship (headProgress=1.0), thin at tail
            val w = trail.width * (0.15f + headProgress * 0.95f)

            // 3. ALPHA FADE: Full at head, zero at tail
            drawer.setColor(trail.color.r, trail.color.g, trail.color.b, baseAlpha * headProgress)
            drawer.line(p1.x + j1.x, p1.y + j1.y, p2.x + j2.x, p2.y + j2.y, w)
        }
    }
}
