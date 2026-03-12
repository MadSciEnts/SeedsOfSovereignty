package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Handles high-fidelity rendering of primary game entities.
 */
class EntityRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val shipViewport: Viewport,
    private val skin: Skin
) : IteratingSystem(family { all(PositionComponent).any(PlayerComponent, EnemyComponent, DroneComponent, StationComponent, SalvageComponent) }) {

    private var renderTime = 0f

    override fun onTick() {
        renderTime += deltaTime
        shipViewport.camera.update()
        batch.use(shipViewport.camera.combined) {
            drawer.update(); super.onTick(); drawer.setColor(Color.WHITE)
        }
    }

    override fun onTickEntity(entity: Entity) {
        if (!entity.has(PlayerComponent) && !entity.has(VisibleComponent) && !entity.has(SalvageComponent)) return
        val pos = entity[PositionComponent].pos
        val orient = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else 0f
        when {
            entity.has(PlayerComponent) -> {
                val evo = entity[EvolutionComponent]
                ProceduralShipDrawer.drawPlayerShip(drawer, pos.x, pos.y, orient, (if (entity.has(WeaponComponent)) entity[WeaponComponent].aimAngleRad else orient), evo.currentTier, evo.subTier)
            }
            entity.has(EnemyComponent) -> ProceduralShipDrawer.drawEnemyShip(drawer, pos.x, pos.y, orient, entity[EnemyComponent].type, Color.RED)
            entity.has(DroneComponent) -> {
                val h = if (entity.has(HealthComponent)) entity[HealthComponent] else null; val s = if (entity.has(ShieldComponent)) entity[ShieldComponent] else null
                ProceduralShipDrawer.drawDrone(drawer, pos.x, pos.y, orient, h?.current ?: 10f, h?.max ?: 10f, s?.current ?: 0f)
            }
            entity.has(StationComponent) -> ProceduralShipDrawer.drawStation(drawer, pos.x, pos.y, entity[StationComponent].radius, skin, batch)
            entity.has(SalvageComponent) -> drawSalvageItem(pos.x, pos.y, entity[SalvageComponent])
        }
    }

    private fun drawSalvageItem(x: Float, y: Float, salvage: SalvageComponent) {
        val camera = shipViewport.camera as OrthographicCamera
        val jitterX = x + MathUtils.sin(renderTime * 4f) * 0.2f
        val jitterY = y + MathUtils.cos(renderTime * 5f) * 0.2f
        val visualSize = (2.4f * salvage.visualScale * camera.zoom * (0.9f + MathUtils.sin(renderTime * 8f) * 0.1f)).coerceIn(1.6f, 16.0f)
        val oldSrc = batch.blendSrcFunc; val oldDst = batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        val sawPeriod = 1.5f
        for (i in 0 until 3) {
            val progress = ((renderTime + i * 0.5f) % sawPeriod) / sawPeriod
            drawer.setColor(0f, 0.9f, 1f, (1f - progress) * 0.5f)
            drawer.circle(jitterX, jitterY, (visualSize / 2f) * (0.4f + progress * 1.8f), 0.2f)
        }
        if (skin.has("essence-core", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val region = skin.getRegion("essence-core")
            batch.setColor(1f, 1f, 1f, 0.9f)
            batch.draw(region, jitterX - visualSize/2.5f, jitterY - visualSize/2.5f, visualSize/1.25f, visualSize/1.25f)
        }
        batch.setBlendFunction(oldSrc, oldDst); batch.setColor(Color.WHITE)
    }
}

/**
 * Technical Renderer for Projectile and Engine Trails (v3.2.3).
 * ROLE: Draws polygon ribbons with ORGANIC SPATIAL NOISE.
 *
 * FIXES:
 * 1. [NOISE] - Implemented World-Pinned Multidimensional Noise (Mixed p1.x/p1.y seeds) to prevent straight lines on cardinal axes.
 */
class RibbonRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val shipViewport: Viewport
) : IteratingSystem(family { all(TrailComponent, PositionComponent) }) {

    private var totalTime = 0f

    override fun onTick() {
        totalTime += deltaTime
        batch.use(shipViewport.camera.combined) {
            drawer.update(); super.onTick(); drawer.setColor(Color.WHITE)
        }
    }

    /**
     * Deterministic organic noise seeded by world coordinates (v3.2.3).
     * Mixing seeds prevents pattern repetition on axes.
     */
    private fun getOrganicNoise(x: Float, y: Float, scale: Float): Vector2 {
        val seed1 = (x * 12.9898f + y * 78.233f)
        val seed2 = (y * 12.9898f + x * 78.233f) // Mixed axis seed
        val jitterX = MathUtils.sin(totalTime * 2.5f + seed1) * scale
        val jitterY = MathUtils.cos(totalTime * 2.1f + seed2 * 1.1f) * scale
        return Vector2(jitterX, jitterY)
    }

    override fun onTickEntity(entity: Entity) {
        val trail = entity[TrailComponent]
        if (trail.history.size < 2) return
        val baseAlpha = trail.color.a
        val stepAlpha = baseAlpha / trail.history.size

        for (i in 0 until trail.history.size - 1) {
            val p1 = trail.history[i]; val p2 = trail.history[i + 1]
            val ageProgress = i.toFloat() / trail.history.size
            val tailAge = 1.0f - ageProgress // 1.0 = oldest part

            // DYNAMIC NOISE: Jitter increases as segments age/approach tail
            val noiseScale = tailAge * trail.turbulenceIntensity * 10.0f
            val j1 = getOrganicNoise(p1.x, p1.y, noiseScale)
            val j2 = getOrganicNoise(p2.x, p2.y, noiseScale)

            // DISSIPATION: Width blooms at tail before fading
            val w = trail.width * (0.4f + tailAge * 0.1f)

            drawer.setColor(trail.color.r, trail.color.g, trail.color.b, stepAlpha * i)
            drawer.line(p1.x + j1.x, p1.y + j1.y, p2.x + j2.x, p2.y + j2.y, w)
        }
    }
}

class RenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val shipViewport: Viewport,
    private val skin: Skin
) : IteratingSystem(family { all(PositionComponent).any(ProjectileComponent, ParticleComponent) }) {
    override fun onTick() {
        batch.use(shipViewport.camera.combined) {
            drawer.update(); super.onTick(); drawer.setColor(Color.WHITE)
        }
    }
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos
        when {
            entity.has(ProjectileComponent) -> {
                val proj = entity[ProjectileComponent]
                com.srw.seedsofsovereignty.util.ProjectileDrawer.draw(drawer, pos.x, pos.y, (if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else 0f), proj.tier, proj.color)
            }
            entity.has(ParticleComponent) -> {
                val part = entity[ParticleComponent]
                when (part.type) {
                    ParticleComponent.Type.SPARK -> drawTexturedPart(pos.x, pos.y, "vfx-spark", part)
                    ParticleComponent.Type.GLOW -> drawTexturedPart(pos.x, pos.y, "vfx-glow", part)
                    ParticleComponent.Type.SHIELD_RIPPLE -> {
                        drawer.setColor(part.color.r, part.color.g, part.color.b, part.alpha)
                        drawer.circle(pos.x, pos.y, part.size, 0.2f)
                    }
                    else -> {
                        drawer.setColor(part.color.r, part.color.g, part.color.b, part.alpha)
                        drawer.filledCircle(pos.x, pos.y, part.size)
                    }
                }
            }
        }
    }
    private fun drawTexturedPart(x: Float, y: Float, name: String, part: ParticleComponent) {
        if (skin.has(name, com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val region = skin.getRegion(name)
            batch.setColor(part.color.r, part.color.g, part.color.b, part.alpha)
            batch.draw(region, x - part.size/2f, y - part.size/2f, part.size, part.size)
            batch.setColor(Color.WHITE)
        }
    }
}

class EvolutionCelebrationSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val uiViewport: Viewport
) : IteratingSystem(family { all(EvolutionCelebrationComponent) }) {
    private var flashAlpha = 0f
    override fun onTick() {
        if (flashAlpha > 0f) {
            batch.use(uiViewport.camera.combined) {
                drawer.update(); drawer.setColor(1f, 1f, 1f, flashAlpha)
                drawer.filledRectangle(0f, 0f, uiViewport.worldWidth, uiViewport.worldHeight)
                flashAlpha -= 2f * deltaTime; drawer.setColor(Color.WHITE)
            }
        }
        super.onTick()
    }
    override fun onTickEntity(entity: Entity) {
        val celeb = entity[EvolutionCelebrationComponent]
        celeb.timer += deltaTime
        if (celeb.phase == EvolutionCelebrationComponent.Phase.TRIGGERED) {
            flashAlpha = 0.8f; celeb.phase = EvolutionCelebrationComponent.Phase.FLASH
        } else if (celeb.phase == EvolutionCelebrationComponent.Phase.COMPLETED) {
            entity.configure { it -= EvolutionCelebrationComponent }
        }
    }
}
