package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.assets.generators.ProjectileAssetGenerator
import com.srw.seedofsov.assets.StyleRegistry
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.entity
import ktx.ashley.with

/**
 * Handles weapon logic and visual feedback.
 * Truth on Disk: Priority 35 ensures tactical hub moves perfectly with ship physics.
 * Stability: Delegates tactical state management to TacticalSystem and reports direct hardware orientation.
 */
class WeaponSystem(val game: SovereigntyGame) : IteratingSystem(
    allOf(ShipComponent::class, TransformComponent::class, PhysicsComponent::class).get(),
    35
) {
    private val assetGen = ProjectileAssetGenerator(StyleRegistry.EPOCH_1_STYLE)

    private val boltTexture = Texture(assetGen.generateKineticBolt())
    private val missileTexture = Texture(assetGen.generateMissile())
    private val barrelTexture = Texture(assetGen.generateBarrel())

    private val tempVec = Vector2()
    private val localDirection = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ship = entity[ShipComponent.mapper] ?: return
        val transform = entity[TransformComponent.mapper] ?: return
        val physics = entity[PhysicsComponent.mapper] ?: return
        val platform = entity[WeaponPlatformComponent.mapper]

        platform?.let { it.barrelTexture = barrelTexture }

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

        val state = weapon.tacticalState

        if (weapon.isSecondary) {
            val fireBtn = slot.getChannel("fire_button").valueBoolean
            if (fireBtn && weapon.cooldownTimer <= 0) {
                spawnSeekingProjectile(owner, shipTransform, shipPhysics, weapon, deltaTime)
                weapon.cooldownTimer = weapon.fireRate
                state.firePulse = 1.0f
            }
            weapon.commandPosition.set(0f, 0f)
            weapon.commandSpace = CoordinateSpace.OBJECT

            // Secondary weapons aim forward by default (Ship Relative)
            weapon.worldAimAngle = shipTransform.rotation + 90f
            weapon.platformRotation = shipTransform.rotation // Barrels follow ship
        } else {
            val axes = slot.getChannel("weapon_aim").valueVector2
            weapon.commandPosition.set(axes).limit(0.8f)

            // TRUTH ON DISK: Primary weapons use WORLD space for the joystick reticle.
            // This ensures the HUD ring stays fixed in the world direction of aim.
            weapon.commandSpace = CoordinateSpace.WORLD

            if (axes.len() > 0.1f) {
                localDirection.set(axes).nor()
                // platformRotation is world-relative (0 = UP)
                weapon.platformRotation = localDirection.angleDeg() - 90f

                if (weapon.cooldownTimer <= 0) {
                    val barrels = weapon.getBarrelOffsets()
                    barrels.forEach { offset ->
                        // offset is ship-local, needs to be rotated by ship rotation
                        tempVec.set(offset).rotateDeg(shipTransform.rotation)
                        val spawnX = shipTransform.position.x + tempVec.x
                        val spawnY = shipTransform.position.y + tempVec.y

                        spawnStandardProjectile(owner, spawnX, spawnY, shipPhysics, weapon, localDirection, deltaTime)
                        spawnMuzzleFlash(spawnX, spawnY, weapon)
                    }
                    weapon.cooldownTimer = weapon.fireRate
                    state.firePulse = 1.0f
                }
            }

            // TRUTH ON DISK FIX: Report the absolute world orientation.
            // Since platformRotation is already world-relative (0 = UP),
            // and Tactical HUD expects 90 = UP, we just add 90.
            weapon.worldAimAngle = weapon.platformRotation + 90f
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
