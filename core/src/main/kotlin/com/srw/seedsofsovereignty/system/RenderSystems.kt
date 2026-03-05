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

        if (entity.has(VisibleComponent) || entity.has(PlayerComponent) || entity.has(ProjectileComponent) || entity.has(EssenceComponent)) {
            if (entity.has(PlayerComponent)) {
                val angleRad = if (entity.has(OrientationComponent)) entity[OrientationComponent].angleRad else MathUtils.PI / 2f
                val evo = if (entity.has(EvolutionComponent)) entity[EvolutionComponent] else null
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
            else -> Color.CYAN
        }
        drawer.setColor(color)
        drawer.line(x1, y1, x2, y2, 2f)
        drawer.line(x2, y2, x3, y3, 2f)
        drawer.line(x3, y3, x1, y1, 2f)

        drawer.setColor(Color.WHITE)
        if (tier == 1) {
            when (subTier) {
                2 -> drawWeaponPoint(x + 10f * cos, y + 10f * sin, rotation, 4f)
                3 -> {
                    val s1 = rotation + MathUtils.PI / 2f
                    val s2 = rotation - MathUtils.PI / 2f
                    drawWeaponPoint(x + 8f * MathUtils.cos(s1), y + 8f * MathUtils.sin(s1), rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s2), y + 8f * MathUtils.sin(s2), rotation, 4f)
                }
                4 -> {
                    val s1 = rotation + MathUtils.PI / 2f
                    val s2 = rotation - MathUtils.PI / 2f
                    drawWeaponPoint(x + 10f * cos, y + 10f * sin, rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s1), y + 8f * MathUtils.sin(s1), rotation, 4f)
                    drawWeaponPoint(x + 8f * MathUtils.cos(s2), y + 8f * MathUtils.sin(s2), rotation, 4f)
                }
            }
        } else if (tier == 4) {
            drawer.setColor(Color.GOLD)
            drawer.filledCircle(x, y, 6f)
            drawer.setColor(Color.WHITE)
            drawer.line(x, y, x + 12f * cos, y + 12f * sin, 3f)
        }

        if (tier >= 3) {
            drawer.setColor(color)
            drawer.circle(x, y, size * 0.8f, 1.5f)
        } else if (tier >= 2) {
            drawer.line(x, y, x - size * 0.5f * cos, y - size * 0.5f * sin, 2f)
        }

        drawer.setColor(Color.WHITE)
        drawer.filledCircle(x, y, 3f)
    }

    private fun drawWeaponPoint(x: Float, y: Float, rotation: Float, size: Float) {
        val cos = MathUtils.cos(rotation)
        val sin = MathUtils.sin(rotation)
        val x1 = x + size * cos
        val y1 = y + size * sin
        val x2 = x + size * MathUtils.cos(rotation + 2.4f)
        val y2 = y + size * MathUtils.sin(rotation + 2.4f)
        val x3 = x + size * MathUtils.cos(rotation - 2.4f)
        val y3 = y + size * MathUtils.sin(rotation - 2.4f)
        drawer.line(x1, y1, x2, y2, 1f)
        drawer.line(x2, y2, x3, y3, 1f)
        drawer.line(x3, y3, x1, y1, 1f)
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
