package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.service.ShipTextureGenerator

/**
 * PROJECTILE FACTORY: Centralizes modular projectile creation.
 * Implements KINETIC, ION, and PLASMA archetypes with dynamic scaling.
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
            entity += ProjectileComponent(
                lifeTime = blueprint.lifeTime,
                style = blueprint.style,
                minWidthPixels = blueprint.minWidthPixels
            )
            entity += FactionComponent(faction)

            // 1. Position: Pivot + (Offset rotated by platform - 90)
            tempVec.set(offset).rotateDeg(platformRotation - 90f).add(pivot)
            entity += PositionComponent(Vector2(tempVec))

            // 2. Velocity: Unit vector at platformRotation * speed
            tempVec.set(1f, 0f).setAngleDeg(platformRotation).scl(blueprint.speed)
            entity += VelocityComponent(v = Vector2(tempVec), friction = 0f)

            // 3. Assemble Visual DNA based on Type
            val shapeType = when (blueprint.style) {
                ProjectileStyle.KINETIC -> ShapeType.SQUARE // Elongated in renderer
                ProjectileStyle.ION -> ShapeType.CIRCLE
                ProjectileStyle.PLASMA -> ShapeType.CIRCLE
            }

            entity += ShapeComponent(
                type = shapeType,
                color = Color.WHITE, // Bright Inner Core
                size = 0.05f * (1f + blueprint.tier * 0.1f),
                rotation = platformRotation
            )

            // 4. Aura Visuals based on Archetype
            val auraEffect = when (blueprint.style) {
                ProjectileStyle.ION -> RenderEffect.PULSE
                ProjectileStyle.PLASMA -> RenderEffect.FLICKER
                else -> RenderEffect.STATIC
            }

            entity += CompositeVisualComponent(
                layers = mutableListOf(
                    VisualLayer(
                        region = ShipTextureGenerator.getProjectileGlow(shapeType, blueprint.color),
                        color = blueprint.color.cpy().apply { a = 0.6f },
                        effect = auraEffect,
                        zIndex = -1,
                        offsetScale = if (blueprint.style == ProjectileStyle.PLASMA) 4.0f else 2.5f
                    )
                )
            )

            // 5. Combat Data & Impact Pools
            entity += DamageComponent(
                amount = blueprint.baseDamage,
                type = blueprint.damageType,
                shieldMultiplier = blueprint.shieldMultiplier,
                impactFX = blueprint.impactFX
            )

            // 6. Status Effects (Special Attributes)
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
