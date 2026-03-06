package com.srw.seedsofsovereignty.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ProceduralShipDrawer
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class RenderSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera,
    private val drawer: ShapeDrawer
) : IteratingSystem(family { all(PositionComponent) }) {

    override fun onTick() {
        batch.use(camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent].pos

        if (entity.has(VisibleComponent) || entity.has(PlayerComponent) || entity.has(ProjectileComponent) || entity.has(EssenceComponent) || entity.has(ParticleComponent) || entity.has(DroneComponent)) {
            if (entity.has(PlayerComponent)) {
                val angleRad = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else MathUtils.PI / 2f
                val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
                val weapon = if (entity.has(WeaponComponent)) entity[WeaponComponent] else null
                val aimAngle = weapon?.aimAngleRad ?: angleRad

                if (evo != null && evo.currentTier >= 5 && entity.has(ShieldComponent)) {
                    val shield = entity[ShieldComponent]
                    if (shield.active && shield.current > 0) {
                        val shieldColor = when(evo.currentTier) { 8 -> Color.SLATE; 9 -> Color.FIREBRICK; else -> Color.CYAN }
                        drawer.setColor(shieldColor.r, shieldColor.g, shieldColor.b, 0.2f)
                        val size = ProceduralShipDrawer.getShipSize(evo.currentTier, evo.subTier)
                        drawer.filledCircle(pos.x, pos.y, size * 1.3f)
                        drawer.setColor(shieldColor); drawer.circle(pos.x, pos.y, size * 1.3f, 2f)
                    }
                }
                ProceduralShipDrawer.drawPlayerShip(drawer, pos.x, pos.y, angleRad, aimAngle, evo?.currentTier ?: 1, evo?.subTier ?: 1)
            } else if (entity.has(ProjectileComponent)) {
                val proj = entity[ProjectileComponent]
                val vel = entity[VelocityComponent].v
                drawProjectile(pos.x, pos.y, vel.angleRad(), proj.tier)
            } else if (entity.has(EnemyComponent)) {
                drawEnemy(pos.x, pos.y, entity[EnemyComponent].type)
            } else if (entity.has(EssenceComponent)) {
                drawEssence(pos.x, pos.y)
            } else if (entity.has(DroneComponent)) {
                val angleRad = entity[OrientationComponent].angleRad
                val health = if (entity.has(HealthComponent)) entity[HealthComponent] else null
                val shield = if (entity.has(ShieldComponent)) entity[ShieldComponent] else null
                ProceduralShipDrawer.drawDrone(drawer, pos.x, pos.y, angleRad, health?.current ?: 1f, health?.max ?: 1f, shield?.current ?: 0f)
            } else if (entity.has(StationComponent)) {
                drawStation(pos.x, pos.y)
            } else if (entity.has(ParticleComponent)) {
                drawParticle(pos.x, pos.y, entity[ParticleComponent], entity[LifeSpanComponent].remaining)
            } else {
                drawDetectedObject(pos.x, pos.y)
            }
        }
    }

    private fun drawProjectile(x: Float, y: Float, rotation: Float, tier: Int) {
        val cos = MathUtils.cos(rotation); val sin = MathUtils.sin(rotation)
        when (tier) {
            1 -> { drawer.setColor(Color.YELLOW); drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1.5f) }
            2 -> { drawer.setColor(Color.VIOLET); drawer.line(x, y, x + 12f * cos, y + 12f * sin, 3f) }
            3 -> { drawer.setColor(Color.GOLD); drawer.filledCircle(x, y, 3f); drawer.line(x, y, x + 10f * cos, y + 10f * sin, 2f) }
            4 -> { drawer.setColor(Color.ORANGE); drawer.filledCircle(x, y, 4f); drawer.setColor(Color.WHITE); drawer.circle(x, y, 6f, 1f) }
            5 -> { // Diamond kinetic
                drawer.setColor(Color.CHARTREUSE); val s = 5f
                drawer.line(x - s*cos + s*sin, y - s*sin - s*cos, x + s*cos + s*sin, y + s*sin - s*cos, 2.5f)
                drawer.line(x + s*cos + s*sin, y + s*sin - s*cos, x + s*cos - s*sin, y + s*sin + s*cos, 2.5f)
                drawer.line(x + s*cos - s*sin, y + s*sin + s*cos, x - s*cos - s*sin, y - s*sin + s*cos, 2.5f)
                drawer.line(x - s*cos - s*sin, y - s*sin + s*cos, x - s*cos + s*sin, y - s*sin - s*cos, 2.5f)
            }
            6 -> { // Square plasma
                drawer.setColor(Color.SKY); val s = 6f
                drawer.line(x - s*cos + s*sin, y - s*sin - s*cos, x + s*cos + s*sin, y + s*sin - s*cos, 3.5f)
                drawer.line(x + s*cos + s*sin, y + s*sin - s*cos, x + s*cos - s*sin, y + s*sin + s*cos, 3.5f)
                drawer.line(x + s*cos - s*sin, y + s*sin + s*cos, x - s*cos - s*sin, y - s*sin + s*cos, 3.5f)
                drawer.line(x - s*cos - s*sin, y - s*sin + s*cos, x - s*cos + s*sin, y - s*sin - s*cos, 3.5f)
            }
            7 -> { drawer.setColor(Color.PURPLE); drawer.line(x, y, x + 20f * cos, y + 20f * sin, 3f); drawer.setColor(Color.WHITE); drawer.filledCircle(x, y, 3.5f) }
            8 -> { drawer.setColor(Color.LIGHT_GRAY); drawer.line(x, y, x + 25f * cos, y + 25f * sin, 6f); drawer.setColor(Color.WHITE); drawer.line(x + 15f * cos, y + 15f * sin, x + 25f * cos, y + 25f * sin, 3f) }
            9 -> { // Vanguard Heavy Bolt
                drawer.setColor(Color.FIREBRICK); drawer.line(x, y, x + 35f * cos, y + 35f * sin, 10f)
                drawer.setColor(Color.WHITE); drawer.line(x + 15f * cos, y + 15f * sin, x + 30f * cos, y + 30f * sin, 4f)
            }
            else -> { drawer.setColor(Color.WHITE); drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1.5f) }
        }
    }

    private fun drawEnemy(x: Float, y: Float, type: String) {
        when (type) {
            "Reaper" -> { drawer.setColor(Color.PURPLE); drawer.circle(x, y, 18f, 3f); drawer.setColor(Color.RED); drawer.circle(x, y, 10f, 2f) }
            "Phantom" -> { drawer.setColor(Color.TEAL); drawer.rectangle(x-10f, y-10f, 20f, 20f, 2f); drawer.setColor(Color.RED); drawer.filledCircle(x, y, 4f) }
            else -> { drawer.setColor(Color.RED); drawer.circle(x, y, 15f, 2f); drawer.setColor(Color.ORANGE); drawer.line(x - 10f, y, x + 10f, y, 1f); drawer.line(x, y - 10f, x, y + 10f, 1f) }
        }
    }

    private fun drawEssence(x: Float, y: Float) {
        drawer.setColor(Color.CYAN); val pulse = 4f + MathUtils.sin(Gdx.graphics.frameId.toFloat() * 0.1f) * 2f
        drawer.filledCircle(x, y, pulse); drawer.setColor(Color.WHITE); drawer.circle(x, y, pulse, 1f)
    }

    private fun drawParticle(x: Float, y: Float, part: ParticleComponent, life: Float) {
        val alpha = if (part.fade) life / part.maxLife else 1f
        drawer.setColor(part.color.r, part.color.g, part.color.b, part.color.a * alpha)
        when (part.type) {
            ParticleComponent.Type.SHIELD_RIPPLE -> { drawer.circle(x, y, part.size * (1f - life/part.maxLife), 2f) }
            ParticleComponent.Type.SPARK -> { drawer.filledCircle(x, y, part.size * (life/part.maxLife)) }
        }
    }

    private fun drawStation(x: Float, y: Float) {
        drawer.setColor(Color.CYAN); drawer.circle(x, y, 150f, 4f); drawer.setColor(Color.DARK_GRAY); drawer.filledRectangle(x - 60f, y - 60f, 120f, 120f); drawer.setColor(Color.WHITE); drawer.rectangle(x - 60f, y - 60f, 120f, 120f, 2f); drawer.setColor(Color.CYAN); drawer.line(x - 60f, y, x + 60f, y, 1f); drawer.line(x, y - 60f, x, y + 60f, 1f)
    }

    private fun drawDetectedObject(x: Float, y: Float) {
        drawer.setColor(Color.GRAY); drawer.rectangle(x - 8f, y - 8f, 16f, 16f, 1f); drawer.setColor(Color.DARK_GRAY); drawer.filledRectangle(x - 6f, y - 6f, 12f, 12f)
    }
}
