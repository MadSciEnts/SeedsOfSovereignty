package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Handles the high-fidelity rendering of primary game entities.
 * Constructor injection used for Skin to prevent Fleks injection errors.
 */
class EntityRenderSystem(
    private val batch: SpriteBatch,
    private val drawer: ShapeDrawer,
    private val viewport: Viewport,
    private val skin: Skin
) : IteratingSystem(family { all(PositionComponent).any(PlayerComponent, EnemyComponent, DroneComponent, StationComponent) }) {

    override fun onTick() {
        viewport.camera.update()
        batch.use(viewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        if (!entity.has(PlayerComponent) && !entity.has(VisibleComponent)) return

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
                val tier = when(enemy.type) {
                    "Swarm" -> 1
                    "Reaper" -> 2
                    "Phantom" -> 7
                    "Apex" -> 8
                    else -> 1
                }
                ProceduralShipDrawer.drawPlayerShip(drawer, pos.x, pos.y, orient, orient, tier, 1, Color.RED)
            }
            entity.has(DroneComponent) -> {
                val health = if (entity.has(HealthComponent)) entity[HealthComponent] else null
                val shield = if (entity.has(ShieldComponent)) entity[ShieldComponent] else null
                ProceduralShipDrawer.drawDrone(drawer, pos.x, pos.y, orient, health?.current ?: 10f, health?.max ?: 10f, shield?.current ?: 0f)
            }
            entity.has(StationComponent) -> {
                val station = entity[StationComponent]
                // Uses the textured technical blueprint with improved radius scaling
                ProceduralShipDrawer.drawStation(drawer, pos.x, pos.y, station.radius, skin, batch)
            }
        }
    }
}

/**
 * Handles rendering for non-hull items (Bullets, Particles, Essence).
 */
class RenderSystem(
    private val batch: SpriteBatch,
    private val camera: com.badlogic.gdx.graphics.OrthographicCamera,
    private val drawer: ShapeDrawer
) : IteratingSystem(family { all(PositionComponent).any(ProjectileComponent, ParticleComponent, EssenceComponent) }) {

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
                // APPLY ALPHA FOR TRANSPARENCY
                drawer.setColor(part.color.r, part.color.g, part.color.b, part.alpha)

                if (part.type == ParticleComponent.Type.SHIELD_RIPPLE) {
                    drawer.circle(pos.x, pos.y, part.size, 2f)
                } else {
                    drawer.filledCircle(pos.x, pos.y, part.size)
                }
            }
            entity.has(EssenceComponent) -> {
                val rot = (System.currentTimeMillis() % 1000) / 1000f * com.badlogic.gdx.math.MathUtils.PI2
                drawer.setColor(Color.GOLD)
                drawer.rectangle(pos.x - 4f, pos.y - 4f, 8f, 8f, rot, 2f)
            }
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
