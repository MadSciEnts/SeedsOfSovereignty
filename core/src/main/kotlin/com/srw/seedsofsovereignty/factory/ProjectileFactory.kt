package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator

/**
 * PROJECTILE FACTORY: Centralizes modular projectile creation.
 * Implements "Visually Pleasing" primitives with steady energy glows for the swarm aesthetic.
 */
class ProjectileFactory {

    private val tempVec = Vector2()

    fun spawn(
        world: World,
        blueprint: ProjectileBlueprint,
        faction: FactionType,
        pivot: Vector2,
        offset: Vector2,
        platformRotation: Float
    ): Entity {
        return world.entity { entity ->
            entity += ProjectileComponent(lifeTime = blueprint.lifeTime)
            entity += FactionComponent(faction)

            // 1. Position: Pivot + (Offset rotated by platform - 90)
            tempVec.set(offset).rotateDeg(platformRotation - 90f).add(pivot)
            entity += PositionComponent(Vector2(tempVec))

            // 2. Velocity: Unit vector at platformRotation * speed
            tempVec.set(1f, 0f).setAngleDeg(platformRotation).scl(blueprint.speed)
            entity += VelocityComponent(v = Vector2(tempVec), friction = 0f)

            // 3. Core Visual (The physical core)
            val shapeType = when (blueprint.type) {
                ProjectileType.KINETIC -> ShapeType.SQUARE
                ProjectileType.ENERGY -> ShapeType.CIRCLE
                ProjectileType.TORPEDO -> ShapeType.TRIANGLE
                else -> ShapeType.SQUARE
            }

            entity += ShapeComponent(
                type = shapeType,
                color = Color.WHITE, // Bright Inner Core
                size = 0.05f * (1f + blueprint.tier * 0.1f),
                rotation = platformRotation
            )

            // 4. Aura Visual (STEADY Energy Glow)
            // Note: pulse is set to false to avoid distracting synchronized pulsing
            entity += CompositeVisualComponent(
                layers = mutableListOf(
                    VisualLayer(
                        region = ShipTextureGenerator.getProjectileGlow(shapeType, blueprint.color),
                        color = blueprint.color.cpy().apply { a = 0.5f },
                        pulse = false,
                        zIndex = -1,
                        offsetScale = 3.0f // Significant energy aura relative to core
                    )
                )
            )

            // 5. Status Effects
            if (blueprint.damageType == DamageType.ACID) {
                entity += StatusEffectComponent(
                    type = DamageType.ACID,
                    duration = 3f,
                    intensity = blueprint.baseDamage * 0.2f
                )
            }
        }
    }
}
