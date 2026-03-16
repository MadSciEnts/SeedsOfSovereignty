package com.srw.seedsofsovereignty.factory

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * PROJECTILE FACTORY: Centralizes the creation of all physical projectiles.
 * Handles different ProjectileTypes and their initial physical states.
 */
class ProjectileFactory {

    private val tempVec = Vector2()

    /**
     * Spawns a projectile entity based on a blueprint.
     * Note: Component instances are created here as they are unique to the entity.
     * Math operations use a pre-allocated tempVec.
     */
    fun spawn(
        world: World,
        blueprint: ProjectileBlueprint,
        faction: FactionType,
        origin: Vector2,
        offset: Vector2,
        angle: Float
    ): Entity {
        return world.entity { entity ->
            entity += ProjectileComponent(lifeTime = blueprint.lifeTime)
            entity += FactionComponent(faction)

            // Calculate position: origin + (offset rotated by angle)
            tempVec.set(offset).rotateDeg(angle).add(origin)
            entity += PositionComponent(Vector2(tempVec))

            // Calculate velocity: unit vector at angle * speed
            tempVec.set(1f, 0f).setAngleDeg(angle).scl(blueprint.speed)
            entity += VelocityComponent(v = Vector2(tempVec), friction = 0f)

            // Visuals based on ProjectileType
            when (blueprint.type) {
                ProjectileType.KINETIC -> {
                    entity += ShapeComponent(
                        type = ShapeType.SQUARE,
                        color = blueprint.color,
                        size = 0.1f,
                        rotation = angle
                    )
                }
                ProjectileType.ENERGY -> {
                    entity += ShapeComponent(
                        type = ShapeType.CIRCLE,
                        color = blueprint.color,
                        size = 0.25f,
                        rotation = angle
                    )
                }
                ProjectileType.TORPEDO -> {
                    entity += ShapeComponent(
                        type = ShapeType.TRIANGLE,
                        color = blueprint.color,
                        size = 0.3f,
                        rotation = angle
                    )
                }
            }
        }
    }
}
