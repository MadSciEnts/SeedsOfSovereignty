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
     * Spawns a projectile entity based on a blueprint at the rotated barrel tip.
     * Note: Math operations use a pre-allocated tempVec to maintain zero-allocation standards.
     *
     * @param world The Fleks World.
     * @param blueprint The projectile's visual and physical data.
     * @param faction The owner's faction.
     * @param pivot The world position of the module/weapon platform.
     * @param offset The relative offset of the barrel on the platform.
     * @param platformRotation The current absolute rotation of the weapon platform.
     */
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

            // 1. Calculate emission point: Pivot + (BarrelOffset rotated by platform angle - 90)
            // Normalization: Forward is 90 deg (Up), so we subtract 90 to rotate from a neutral origin.
            tempVec.set(offset).rotateDeg(platformRotation - 90f).add(pivot)
            entity += PositionComponent(Vector2(tempVec))

            // 2. Calculate velocity: unit vector at platformRotation * speed
            tempVec.set(1f, 0f).setAngleDeg(platformRotation).scl(blueprint.speed)
            entity += VelocityComponent(v = Vector2(tempVec), friction = 0f)

            // 3. Visuals (Using restored ShapeComponent for projectiles)
            when (blueprint.type) {
                ProjectileType.KINETIC -> {
                    entity += ShapeComponent(
                        type = ShapeType.SQUARE,
                        color = blueprint.color.cpy(),
                        size = 0.05f, // Scaled for 1.0 world unit ship baseline
                        rotation = platformRotation
                    )
                }
                ProjectileType.ENERGY -> {
                    entity += ShapeComponent(
                        type = ShapeType.CIRCLE,
                        color = blueprint.color.cpy(),
                        size = 0.15f,
                        rotation = platformRotation
                    )
                }
                ProjectileType.TORPEDO -> {
                    entity += ShapeComponent(
                        type = ShapeType.TRIANGLE,
                        color = blueprint.color.cpy(),
                        size = 0.2f,
                        rotation = platformRotation
                    )
                }
            }
        }
    }
}
