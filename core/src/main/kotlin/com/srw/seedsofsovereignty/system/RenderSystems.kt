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

        if (entity.has(VisibleComponent) || entity.has(PlayerComponent) || entity.has(ProjectileComponent) || entity.has(EssenceComponent) || entity.has(ParticleComponent)) {
            if (entity.has(PlayerComponent)) {
                val angleRad = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else MathUtils.PI / 2f
                val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null

                if (evo?.currentTier == 5 && entity.has(ShieldComponent)) {
                    val shield = entity[ShieldComponent]
                    if (shield.active && shield.current > 0) {
                        drawer.setColor(0f, 0.8f, 1f, 0.3f)
                        drawer.filledCircle(pos.x, pos.y, 35f)
                        drawer.setColor(Color.CYAN)
                        drawer.circle(pos.x, pos.y, 35f, 2f)
                    }
                }

                drawPlayerShip(pos.x, pos.y, angleRad, evo?.currentTier ?: 1, evo?.subTier ?: 1)
            } else if (entity.has(ProjectileComponent)) {
                val proj = entity[ProjectileComponent]
                val vel = entity[VelocityComponent].v
                drawProjectile(pos.x, pos.y, vel.angleRad(), proj.tier)
            } else if (entity.has(EnemyComponent)) {
                val enemy = entity[EnemyComponent]
                drawEnemy(pos.x, pos.y, enemy.type)
            } else if (entity.has(EssenceComponent)) {
                drawEssence(pos.x, pos.y)
            } else if (entity.has(DroneComponent)) {
                val angleRad = entity[OrientationComponent].angleRad
                drawDrone(pos.x, pos.y, angleRad)
            } else if (entity.has(StationComponent)) {
                drawStation(pos.x, pos.y)
            } else if (entity.has(ParticleComponent)) {
                val part = entity[ParticleComponent]
                val life = entity[LifeSpanComponent].remaining
                drawParticle(pos.x, pos.y, part, life)
            } else {
                drawDetectedObject(pos.x, pos.y)
            }
        }
    }

    private fun drawPlayerShip(x: Float, y: Float, rotation: Float, tier: Int, subTier: Int) {
        val size = when(tier) {
            1 -> 16f
            2 -> 20f
            3 -> 24f
            4 -> 30f
            5 -> 35f
            6 -> 45f
            else -> 16f
        }
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)

        val x1 = x + size * cos
        val y1 = y + size * sin
        val angleOffset = if (tier >= 2) 2.2f else 2.4f
        val x2 = x + size * MathUtils.cos(rotation + angleOffset)
        val y2 = y + size * MathUtils.sin(rotation + angleOffset)
        val x3 = x + size * MathUtils.cos(rotation - angleOffset)
        val y3 = y + size * MathUtils.sin(rotation - angleOffset)

        val color = when(tier) {
            1 -> Color.CYAN
            2 -> Color.VIOLET
            3 -> Color.GOLD
            4 -> Color.ORANGE
            5 -> Color.CHARTREUSE
            6 -> Color.SKY
            else -> Color.CYAN
        }
        drawer.setColor(color)

        if (tier == 6) {
            drawer.filledCircle(x, y, size * 0.4f)
            drawer.circle(x, y, size * 0.7f, 3f)

            val wingSize = size * 1.2f
            val wingAngle = 2.1f
            val wx1 = x + wingSize * MathUtils.cos(rotation + wingAngle)
            val wy1 = y + wingSize * MathUtils.sin(rotation + wingAngle)
            val wx2 = x + wingSize * MathUtils.cos(rotation - wingAngle)
            val wy2 = y + wingSize * MathUtils.sin(rotation - wingAngle)

            drawer.line(x1, y1, wx1, wy1, 4f)
            drawer.line(x1, y1, wx2, wy2, 4f)
            drawer.line(wx1, wy1, x - size * 0.5f * cos, y - size * 0.5f * sin, 2f)
            drawer.line(wx2, wy2, x - size * 0.5f * cos, y - size * 0.5f * sin, 2f)
        } else {
            drawer.line(x1, y1, x2, y2, 2f)
            drawer.line(x2, y2, x3, y3, 2f)
            drawer.line(x3, y3, x1, y1, 2f)
        }

        if (tier == 5) {
            drawer.line(x2, y2, x2 - 10f * cos, y2 - 10f * sin, 3f)
            drawer.line(x3, y3, x3 - 10f * cos, y3 - 10f * sin, 3f)
        }

        if (tier >= 3 && tier != 6) {
            drawer.circle(x, y, size * 0.8f, 1.5f)
        } else if (tier >= 2 && tier != 6) {
            drawer.line(x, y, x - size * 0.5f * cos, y - size * 0.5f * sin, 2f)
        }

        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, 3f)
    }

    private fun drawDrone(x: Float, y: Float, rotation: Float) {
        drawer.setColor(Color.GOLD)
        drawer.circle(x, y, 6f, 1.5f)
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)
        drawer.line(x, y, x + 8f * cos, y + 8f * sin, 1f)
    }

    private fun drawProjectile(x: Float, y: Float, rotation: Float, tier: Int) {
        when (tier) {
            1 -> {
                drawer.setColor(Color.YELLOW)
                val length = 8f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 1.5f)
            }
            2 -> {
                drawer.setColor(Color.VIOLET)
                val length = 12f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 3f)
            }
            3 -> {
                drawer.setColor(Color.GOLD)
                val length = 10f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.filledCircle(x, y, 3f)
                drawer.line(x, y, x + length * cos, y + length * sin, 2f)
            }
            4 -> {
                drawer.setColor(Color.ORANGE)
                drawer.filledCircle(x, y, 4f)
                drawer.setColor(Color.WHITE)
                drawer.circle(x, y, 6f, 1f)
            }
            5 -> {
                drawer.setColor(Color.CHARTREUSE)
                drawer.rectangle(x-4f, y-4f, 8f, 8f, 2f)
            }
            6 -> {
                drawer.setColor(Color.SKY)
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.filledRectangle(x - 4f, y - 4f, 8f, 8f, rotation * MathUtils.radDeg)
                drawer.setColor(Color.WHITE)
                drawer.rectangle(x - 6f, y - 6f, 12f, 12f, 1f, rotation * MathUtils.radDeg)
            }
            else -> {
                drawer.setColor(Color.WHITE)
                val length = 8f
                val cos = MathUtils.cos(rotation)
                val sin = MathUtils.sin(rotation)
                drawer.line(x, y, x + length * cos, y + length * sin, 1.5f)
            }
        }
    }

    private fun drawEnemy(x: Float, y: Float, type: String) {
        when (type) {
            "Reaper" -> {
                drawer.setColor(Color.PURPLE)
                drawer.circle(x, y, 18f, 3f)
                drawer.setColor(Color.RED)
                drawer.circle(x, y, 10f, 2f)
            }
            "Phantom" -> {
                drawer.setColor(Color.TEAL)
                drawer.rectangle(x-10f, y-10f, 20f, 20f, 2f)
                drawer.setColor(Color.RED)
                drawer.filledCircle(x, y, 4f)
            }
            else -> {
                drawer.setColor(Color.RED)
                drawer.circle(x, y, 15f, 2f)
                drawer.setColor(Color.ORANGE)
                drawer.line(x - 10f, y, x + 10f, y, 1f)
                drawer.line(x, y - 10f, x, y + 10f, 1f)
            }
        }
    }

    private fun drawEssence(x: Float, y: Float) {
        drawer.setColor(Color.CYAN)
        val time = Gdx.graphics.frameId.toFloat() * 0.1f
        val pulse = 4f + MathUtils.sin(time) * 2f
        drawer.filledCircle(x, y, pulse)
        drawer.setColor(Color.WHITE)
        drawer.circle(x, y, pulse, 1f)
    }

    private fun drawParticle(x: Float, y: Float, part: ParticleComponent, life: Float) {
        val alpha = if (part.fade) life / part.maxLife else 1f
        drawer.setColor(part.color.r, part.color.g, part.color.b, part.color.a * alpha)

        when (part.type) {
            ParticleComponent.Type.SHIELD_RIPPLE -> {
                val size = part.size * (1f - life/part.maxLife)
                drawer.circle(x, y, size, 2f)
            }
            ParticleComponent.Type.SPARK -> {
                drawer.filledCircle(x, y, part.size * (life/part.maxLife))
            }
        }
    }

    private fun drawStation(x: Float, y: Float) {
        drawer.setColor(Color.CYAN)
        drawer.circle(x, y, 150f, 4f)
        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x - 60f, y - 60f, 120f, 120f)
        drawer.setColor(Color.WHITE)
        drawer.rectangle(x - 60f, y - 60f, 120f, 120f, 2f)
        drawer.setColor(Color.CYAN)
        drawer.line(x - 60f, y, x + 60f, y, 1f)
        drawer.line(x, y - 60f, x, y + 60f, 1f)
    }

    private fun drawDetectedObject(x: Float, y: Float) {
        drawer.setColor(Color.GRAY)
        drawer.rectangle(x - 8f, y - 8f, 16f, 16f, 1f)
        drawer.setColor(Color.DARK_GRAY)
        drawer.filledRectangle(x - 6f, y - 6f, 12f, 12f)
    }
}
