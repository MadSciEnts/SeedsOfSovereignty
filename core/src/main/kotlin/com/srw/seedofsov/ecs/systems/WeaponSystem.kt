package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.generators.ProjectileAssetGenerator
import com.srw.seedofsov.assets.StyleRegistry
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.EffectType
import com.srw.seedofsov.model.ModuleCategory
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.entity
import ktx.ashley.with

/**
 * Handles weapon logic and visual feedback (muzzle flashes, traces).
 * Truth on Disk: Synchronized with List-based ShipComponent slots.
 */
class WeaponSystem(val game: SovereigntyGame) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class, PhysicsComponent::class).get(),
    15
) {
    private val assetGen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)

    // Cached textures
    private val boltTexture = Texture(assetGen.generateKineticBolt())
    private val missileTexture = Texture(assetGen.generateMissile())
    private val barrelTexture = Texture(assetGen.generateBarrel())

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return
        val platform = entity[WeaponPlatformComponent.mapper]

        platform?.let { it.barrelTexture = barrelTexture }

        // Logic Change: Iterate over List<ModuleSlot>
        ship.slots.forEach { slot ->
            val module = slot.equippedModule
            if (module is WeaponModuleComponent) {
                updateWeapon(entity, transform, physics, module, slot, deltaTime)
            }
        }
    }

    private fun updateWeapon(owner: Entity, shipTransform: TransformComponent, shipPhysics: PhysicsComponent, weapon: WeaponModuleComponent, slot: com.srw.seedofsov.model.ModuleSlot, deltaTime: Float) {
        if (weapon.cooldownTimer > 0) {
            weapon.cooldownTimer -= deltaTime
        }

        val tempVec = Vector2()
        val localDirection = Vector2()

        if (weapon.isSecondary) {
            val fireBtn = slot.getChannel("fire_button").valueBoolean
            if (fireBtn && weapon.cooldownTimer <= 0) {
                spawnSeekingProjectile(owner, shipTransform, shipPhysics, weapon, deltaTime)
                weapon.cooldownTimer = weapon.fireRate
            }
        } else {
            val axes = slot.getChannel("weapon_aim").valueVector2
            if (axes.len() > 0.1f) {
                localDirection.set(axes).nor()
                weapon.platformRotation = localDirection.angleDeg() - 90f

                if (weapon.cooldownTimer <= 0) {
                    val barrels = weapon.getBarrelOffsets()
                    barrels.forEach { offset ->
                        tempVec.set(offset).rotateDeg(weapon.platformRotation)
                        val spawnX = shipTransform.position.x + tempVec.x
                        val spawnY = shipTransform.position.y + tempVec.y

                        spawnStandardProjectile(owner, spawnX, spawnY, shipPhysics, weapon, localDirection, deltaTime)
                        spawnMuzzleFlash(spawnX, spawnY, weapon)
                    }
                    weapon.cooldownTimer = weapon.fireRate
                }
            }
        }
    }

    private fun spawnMuzzleFlash(x: Float, y: Float, weapon: WeaponModuleComponent) {
        engine.entity {
            with<TransformComponent> {
                position.set(x, y, 0.1f)
                size.set(weapon.barrelWidth * 2f, weapon.barrelWidth * 2f)
                rotation = weapon.platformRotation
            }
            with<EffectComponent> {
                type = EffectType.MUZZLE_FLASH
                maxLifetime = 0.1f
                timer = 0.1f
                metadata = weapon.visuals
            }
            with<SpriteComponent>()
        }
    }

    private fun spawnStandardProjectile(owner: Entity, x: Float, y: Float, shipPhysics: PhysicsComponent, weapon: WeaponModuleComponent, direction: Vector2, deltaTime: Float) {
        engine.entity {
            with<TransformComponent> {
                position.set(x, y, 0f)
                rotation = direction.angleDeg() - 90f
                size.set(0.3f, 0.8f)

                position.x += shipPhysics.velocity.x * deltaTime
                position.y += shipPhysics.velocity.y * deltaTime
            }
            with<PhysicsComponent> {
                velocity.set(shipPhysics.velocity)
                velocity.add(direction.x * weapon.projectileSpeed, direction.y * weapon.projectileSpeed)
                friction = 0f
            }
            with<ProjectileComponent> {
                damage = weapon.damage
                damageType = weapon.damageType
                sourceEntityId = owner.hashCode()
                lifetime = 2.5f
            }
            with<SpriteComponent> {
                sprite.set(Sprite(boltTexture))
                sprite.color.set(weapon.visuals.mainColor)
            }
        }
    }

    private fun spawnSeekingProjectile(owner: Entity, shipTransform: TransformComponent, shipPhysics: PhysicsComponent, weapon: WeaponModuleComponent, deltaTime: Float) {
        val direction = Vector2(0f, 1f).rotateDeg(shipTransform.rotation)
        engine.entity {
            with<TransformComponent> {
                position.set(shipTransform.position.x, shipTransform.position.y, 0f)
                rotation = shipTransform.rotation
                size.set(0.3f, 0.6f)

                position.x += shipPhysics.velocity.x * deltaTime
                position.y += shipPhysics.velocity.y * deltaTime
            }
            with<PhysicsComponent> {
                velocity.set(shipPhysics.velocity)
                velocity.add(direction.x * weapon.projectileSpeed, direction.y * weapon.projectileSpeed)
                friction = 0f
            }
            with<ProjectileComponent> {
                damage = weapon.damage * 2f
                damageType = weapon.damageType
                sourceEntityId = owner.hashCode()
                lifetime = 4f
            }
            with<SeekingComponent> {
                turnRate = 120f
            }
            with<SpriteComponent> {
                sprite.set(Sprite(missileTexture))
                sprite.color.set(weapon.visuals.secondaryColor)
            }
        }
    }
}
