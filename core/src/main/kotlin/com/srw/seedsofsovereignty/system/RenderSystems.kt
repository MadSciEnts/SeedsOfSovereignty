package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
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
    private val viewport: Viewport,
    private val skin: Skin
) : IteratingSystem(family { all(PositionComponent).any(PlayerComponent, EnemyComponent, DroneComponent, StationComponent, SalvageComponent) }) {

    private var renderTime = 0f

    override fun onTick() {
        renderTime += deltaTime
        viewport.camera.update()
        batch.use(viewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        if (!entity.has(PlayerComponent) && !entity.has(VisibleComponent) && !entity.has(SalvageComponent)) return

        val pos = entity[PositionComponent].pos
        val orient = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else 0f

        when {
            entity.has(PlayerComponent) -> {
                val evo = entity[EvolutionComponent]
                val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null
                ProceduralShipDrawer.drawPlayerShip(drawer, pos.x, pos.y, orient, weapon?.aimAngleRad ?: orient, evo.currentTier, evo.subTier)
            }
            entity.has(EnemyComponent) -> {
                val enemy = entity[EnemyComponent]
                ProceduralShipDrawer.drawEnemyShip(drawer, pos.x, pos.y, orient, enemy.type, Color.RED)
            }
            entity.has(DroneComponent) -> {
                val health = if (entity.has(HealthComponent)) entity[HealthComponent] else null
                val shield = if (entity.has(ShieldComponent)) entity[ShieldComponent] else null
                ProceduralShipDrawer.drawDrone(drawer, pos.x, pos.y, orient, health?.current ?: 10f, health?.max ?: 10f, shield?.current ?: 0f)
            }
            entity.has(StationComponent) -> {
                val station = entity[StationComponent]
                ProceduralShipDrawer.drawStation(drawer, pos.x, pos.y, station.radius, skin, batch)
            }
            entity.has(SalvageComponent) -> {
                drawSalvageItem(pos.x, pos.y, entity[SalvageComponent])
            }
        }
    }

    private fun drawSalvageItem(x: Float, y: Float, salvage: SalvageComponent) {
        val camera = viewport.camera as OrthographicCamera

        val jitterX = x + MathUtils.sin(renderTime * 4f) * 2.0f
        val jitterY = y + MathUtils.cos(renderTime * 5f) * 2.0f

        val baseSize = 24f * salvage.visualScale
        val pulse = 0.9f + MathUtils.sin(renderTime * 8f) * 0.1f
        val visualSize = (baseSize * camera.zoom * pulse).coerceIn(16f, 160f)

        val oldSrc = batch.blendSrcFunc
        val oldDst = batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)

        // 1. Technical Beacon (Task: Cyan sawtooth expanding rings)
        val sawPeriod = 1.5f
        for (i in 0 until 3) {
            val offset = i * 0.5f
            val progress = ((renderTime + offset) % sawPeriod) / sawPeriod
            val sawScale = 0.4f + progress * 1.8f
            val sawAlpha = (1f - progress) * 0.5f

            drawer.setColor(0f, 0.9f, 1f, sawAlpha)
            drawer.circle(jitterX, jitterY, (visualSize / 2f) * sawScale, 2f)
        }

        // 2. Core Specimen (Amber/White Core)
        if (skin.has("essence-core", com.badlogic.gdx.graphics.g2d.TextureRegion::class.java)) {
            val region = skin.getRegion("essence-core")
            batch.setColor(1f, 1f, 1f, 0.9f)
            batch.draw(region, jitterX - visualSize/2.5f, jitterY - visualSize/2.5f, visualSize/1.25f, visualSize/1.25f)
        }

        batch.setBlendFunction(oldSrc, oldDst)
        batch.setColor(Color.WHITE)
    }
}

/**
 * Handles rendering for technical assets (Bullets, Particles).
 */
class RenderSystem(
    private val batch: SpriteBatch,
    private val camera: com.badlogic.gdx.graphics.OrthographicCamera,
    private val drawer: ShapeDrawer,
    private val skin: Skin
) : IteratingSystem(family { all(PositionComponent).any(ProjectileComponent, ParticleComponent) }) {

    override fun onTick() {
        batch.use(camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos

        when {
            entity.has(ProjectileComponent) -> {
                val proj = entity[ProjectileComponent]
                val orient = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else 0f
                com.srw.seedsofsovereignty.util.ProjectileDrawer.draw(drawer, pos.x, pos.y, orient, proj.tier, proj.color)
            }
            entity.has(ParticleComponent) -> {
                val part = entity[ParticleComponent]
                when (part.type) {
                    ParticleComponent.Type.SPARK -> drawTexturedPart(pos.x, pos.y, "vfx-spark", part)
                    ParticleComponent.Type.GLOW -> drawTexturedPart(pos.x, pos.y, "vfx-glow", part)
                    ParticleComponent.Type.SHIELD_RIPPLE -> {
                        drawer.setColor(part.color.r, part.color.g, part.color.b, part.alpha)
                        drawer.circle(pos.x, pos.y, part.size, 2f)
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
                drawer.setColor(1f, 1f, 1f, flashAlpha)
                drawer.filledRectangle(0f, 0f, uiViewport.worldWidth, uiViewport.worldHeight)
                flashAlpha -= 2f * deltaTime
            }
        }
        super.onTick()
    }

    override fun onTickEntity(entity: Entity) {
        val celeb = entity[EvolutionCelebrationComponent]
        if (celeb.phase == EvolutionCelebrationComponent.Phase.TRIGGERED) {
            flashAlpha = 0.8f
            celeb.phase = EvolutionCelebrationComponent.Phase.FLASH
        } else if (celeb.phase == EvolutionCelebrationComponent.Phase.COMPLETED) {
            entity.configure { it -= EvolutionCelebrationComponent }
        }
    }
}
