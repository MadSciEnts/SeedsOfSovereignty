package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

class WorldRenderSystem(
    private val batch: Batch = inject("batch"),
    private val drawer: ShapeDrawer = inject("drawer"),
    private val gameViewport: Viewport = inject("gameViewport")
) : IteratingSystem(
    family { any(PositionComponent, TextureRegionComponent, CompositeVisualComponent, ShapeComponent, ImpactFXComponent) }
) {
    private val barrelVec = Vector2()
    private val rotationVec = Vector2()
    private var totalTime = 0f

    override fun onTick() {
        totalTime += deltaTime
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        if (!entity.has(PositionComponent)) return
        val pos = entity[PositionComponent].pos

        // 1. Draw Impact FX
        if (entity.has(ImpactFXComponent)) {
            val fx = entity[ImpactFXComponent]
            drawer.setColor(fx.color.r, fx.color.g, fx.color.b, fx.lifeTime * 5f)
            when (fx.type) {
                ImpactFXType.SPARK -> drawer.line(pos.x, pos.y, pos.x + MathUtils.random(-0.5f, 0.5f), pos.y + MathUtils.random(-0.5f, 0.5f), 0.05f)
                ImpactFXType.FLARE -> drawer.filledCircle(pos.x, pos.y, 0.2f * (1f + MathUtils.sin(totalTime * 20f)))
                ImpactFXType.DETONATION -> drawer.circle(pos.x, pos.y, 0.5f - fx.lifeTime, 0.1f)
                else -> {}
            }
            return
        }

        // 2. Resolve Base Scale & Rotation
        var baseScale = when {
            entity.has(TextureRegionComponent) -> entity[TextureRegionComponent].scale
            entity.has(ShapeComponent) -> entity[ShapeComponent].size
            else -> 1f
        }

        if (entity.has(ProjectileComponent)) {
            val proj = entity[ProjectileComponent]
            val ppu = gameViewport.screenWidth / gameViewport.worldWidth
            val screenWidth = baseScale * ppu
            if (screenWidth < proj.minWidthPixels) {
                baseScale = proj.minWidthPixels / ppu
            }
        }

        val baseRotation = when {
            entity.has(TextureRegionComponent) -> entity[TextureRegionComponent].rotation
            entity.has(ShapeComponent) -> entity[ShapeComponent].rotation
            else -> 0f
        }

        // 3. Draw CompositeVisualComponent (AURAS / LAYERS)
        if (entity.has(CompositeVisualComponent)) {
            val composite = entity[CompositeVisualComponent]
            composite.layers.sortBy { it.zIndex }

            composite.layers.forEach { layer ->
                val region = layer.region
                val worldWidth = baseScale * layer.offsetScale
                val worldHeight = worldWidth * (region.regionHeight.toFloat() / region.regionWidth.toFloat())

                when (layer.effect) {
                    RenderEffect.STATIC -> batch.color = layer.color
                    RenderEffect.PULSE -> {
                        val alpha = 0.6f + 0.4f * MathUtils.sin(totalTime * 5f)
                        batch.setColor(layer.color.r, layer.color.g, layer.color.b, layer.color.a * alpha)
                    }
                    RenderEffect.FLICKER -> {
                        val alpha = 0.5f + 0.5f * MathUtils.sin(totalTime * 20f)
                        val colorSine = (MathUtils.sin(totalTime * 15f) + 1f) / 2f
                        batch.setColor(MathUtils.lerp(layer.color.r, 1f, colorSine), MathUtils.lerp(layer.color.g, 0f, colorSine), layer.color.b, layer.color.a * alpha)
                    }
                    RenderEffect.GLOW -> batch.setColor(layer.color.r, layer.color.g, layer.color.b, 0.8f)
                }

                batch.draw(region, pos.x - worldWidth / 2f, pos.y - worldHeight / 2f, worldWidth / 2f, worldHeight / 2f, worldWidth, worldHeight, 1f, 1f, baseRotation + layer.rotationOffset)
            }
            batch.color = Color.WHITE
        } else if (entity.has(TextureRegionComponent)) {
            // Fallback for simple sprites (modules, etc)
            val tr = entity[TextureRegionComponent]
            tr.region?.let { region ->
                val w = baseScale; val h = w * (region.regionHeight.toFloat() / region.regionWidth.toFloat())
                batch.color = tr.color
                batch.draw(region, pos.x - w / 2f, pos.y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, baseRotation)
                batch.color = Color.WHITE
            }
        }

        // 4. Draw ShapeComponent (ROTATED CORE)
        if (entity.has(ShapeComponent)) {
            val shape = entity[ShapeComponent]
            drawer.setColor(shape.color)

            when (shape.type) {
                ShapeType.SQUARE -> {
                    val length = if (entity.has(ProjectileComponent) && entity[ProjectileComponent].style == ProjectileStyle.KINETIC) baseScale * 4f else baseScale
                    val thickness = baseScale
                    rotationVec.set(0f, length).rotateDeg(baseRotation)
                    drawer.line(pos.x - rotationVec.x/2f, pos.y - rotationVec.y/2f, pos.x + rotationVec.x/2f, pos.y + rotationVec.y/2f, thickness)
                }
                ShapeType.CIRCLE -> drawer.filledCircle(pos.x, pos.y, baseScale)
                ShapeType.TRIANGLE -> {
                    val r = baseScale
                    drawer.filledTriangle(
                        pos.x + MathUtils.cosDeg(baseRotation + 90f) * r, pos.y + MathUtils.sinDeg(baseRotation + 90f) * r,
                        pos.x + MathUtils.cosDeg(baseRotation + 210f) * r, pos.y + MathUtils.sinDeg(baseRotation + 210f) * r,
                        pos.x + MathUtils.cosDeg(baseRotation + 330f) * r, pos.y + MathUtils.sinDeg(baseRotation + 330f) * r
                    )
                }
                ShapeType.HEXAGON -> drawer.polygon(pos.x, pos.y, 6, baseScale)
            }
        }

        // 5. Draw Weapon Barrels
        if (entity.has(BarrelVisualComponent) && entity.has(WeaponLauncherComponent)) {
            val barrel = entity[BarrelVisualComponent]
            val launcher = entity[WeaponLauncherComponent]
            val hullRotation = when {
                entity.has(AttachmentComponent) -> {
                    val parent = entity.get(AttachmentComponent).parent
                    if (parent != Entity.NONE && parent in world) {
                        world.run { if (parent.has(TextureRegionComponent)) parent[TextureRegionComponent].rotation else 0f }
                    } else 0f
                }
                else -> 0f
            }
            var platformRotation = hullRotation + 90f
            if (entity.has(ModuleChannelsComponent)) {
                val channels = entity.get(ModuleChannelsComponent)
                // Standardized TARGET channel for aiming
                val targetInput = channels.inputs[StandardChannels.TARGET]
                if (targetInput != null && targetInput.vector.len2() > 0.01f) {
                    platformRotation = targetInput.vector.angleDeg()
                    barrel.platformRotation = platformRotation
                } else { platformRotation = barrel.platformRotation }
            }
            drawer.setColor(barrel.color)
            launcher.barrelOffsets.forEach { offset ->
                barrelVec.set(offset).rotateDeg(platformRotation - 90f)
                val bx = pos.x + barrelVec.x; val by = pos.y + barrelVec.y
                drawer.line(bx, by, bx + MathUtils.cosDeg(platformRotation) * barrel.length, by + MathUtils.sinDeg(platformRotation) * barrel.length, barrel.width)
            }
        }
    }
}

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
        val count = trail.activeCount
        for (i in 0 until count - 1) {
            val idx1 = (trail.nextIndex - count + i + trail.maxPoints) % trail.maxPoints
            val idx2 = (idx1 + 1) % trail.maxPoints
            val p1 = trail.points[idx1]; val p2 = trail.points[idx2]
            val headProgress = i.toFloat() / (count - 1)
            val tailProgress = 1.0f - headProgress
            j1.set(trail.jitters[idx1]).scl(tailProgress * 0.5f)
            j2.set(trail.jitters[idx2]).scl(tailProgress * 0.5f)
            val w = trail.width * (0.05f + headProgress * 0.95f)
            drawer.setColor(trail.color.r, trail.color.g, trail.color.b, trail.color.a * headProgress)
            drawer.line(p1.x + j1.x, p1.y + j1.y, p2.x + j2.x, p2.y + j2.y, w)
        }
    }
}
