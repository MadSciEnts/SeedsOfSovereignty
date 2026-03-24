package com.srw.seedofsov.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.*
import ktx.ashley.entity
import ktx.ashley.with
import ktx.ashley.get

/**
 * Factory for creating game entities and modules.
 * Truth on Disk: Supports Player (Queen Ant), Enemies, and technical debug components.
 */
class EntityFactory(private val engine: Engine) {

    fun createPlayer(x: Float, y: Float): Entity {
        return engine.entity {
            val rank = with<PlayerRankComponent> {
                subtier = Subtier.A
            }
            with<PlayerComponent>()
            with<InputComponent>()
            with<ShipStatsComponent> {
                tier = 1
                subTier = Subtier.A
                for (i in 0..3) {
                    shieldQuadrants[i] = 100f
                    maxShieldQuadrants[i] = 100f
                    hullQuadrants[i] = 200f
                    maxHullQuadrants[i] = 200f
                }
                powerCores.add(500f)
                maxPowerCores.add(500f)
            }
            with<TransformComponent> {
                position.set(x, y, 0f)
                size.set(1f, 1f)
            }
            with<PhysicsComponent> {
                velocity.set(0f, 0f)
                friction = 1.0f
            }
            with<TrailComponent> {
                color = Color(0.2f, 0.6f, 1.0f, 0.6f)
            }
            // Truth on Disk: Technical log for damage validation
            with<CombatLogComponent>()

            // 1. Initial Core Modules
            val engineModule = with<ImpulseEngineComponent> { thrustPower = 15f }
            val powerModule = with<PowerCoreComponent>()
            val shieldModule = with<ShieldModuleComponent>()

            // 2. Epoch 1.12 "Queen Ant" capability: Dual Drone Bays
            val droneBay1 = with<BayModuleComponent> {
                name = "Drone Bay A"
                capacity = 6
                launchRate = 1.5f
            }
            val droneBay2 = with<BayModuleComponent> {
                name = "Drone Bay B"
                capacity = 6
                launchRate = 1.5f
            }

            val primaryWeapon = with<WeaponModuleComponent> {
                name = "Kinetic Cannon"
                category = ModuleCategory.WEAPON_PRIMARY
                projectileName = "Kinetic Bolt"
                projectileSpeed = 45f
                subtier = rank.subtier
            }

            // 3. Multi-Slot Assembly
            with<ShipComponent> {
                addSlot(ModuleCategory.ENGINE).equippedModule = engineModule
                addSlot(ModuleCategory.POWER_CORE).equippedModule = powerModule
                addSlot(ModuleCategory.SHIELD).equippedModule = shieldModule
                addSlot(ModuleCategory.UTILITY).equippedModule = droneBay1
                addSlot(ModuleCategory.UTILITY).equippedModule = droneBay2
                addSlot(ModuleCategory.WEAPON_PRIMARY).equippedModule = primaryWeapon
            }

            with<WeaponPlatformComponent>()
            with<SpriteComponent> {
                val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
                    setColor(Color.CYAN)
                    fillTriangle(32, 0, 0, 64, 64, 64)
                }
                val texture = Texture(pixmap)
                sprite.set(Sprite(texture))
                pixmap.dispose()
            }
        }
    }

    fun injectModule(entity: Entity, slot: ModuleSlot, newModule: ShipModule) {
        slot.equippedModule?.let { old ->
            if (old is Component) {
                entity.remove(old.javaClass)
            }
        }
        if (newModule is Component) {
            entity.add(newModule)
        }
        slot.equippedModule = newModule
    }

    fun generateModule(category: ModuleCategory, tier: Int, subTier: Subtier): ShipModule {
        return when (category) {
            ModuleCategory.ENGINE -> ImpulseEngineComponent().apply {
                thrustPower = 10f + (tier * 5f)
                maxRotationSpeed = 120f + (tier * 10f)
            }
            ModuleCategory.SHIELD -> ShieldModuleComponent().apply {
                maxShieldStrength = 200f + (tier * 100f)
                shieldStrength = maxShieldStrength
                rechargeRate = 5f + (tier * 2f)
            }
            ModuleCategory.WEAPON_PRIMARY -> WeaponModuleComponent().apply {
                this.subtier = subTier
                projectileSpeed = 40f + (tier * 2f)
                fireRate = 1.0f + (tier * 0.1f)
            }
            ModuleCategory.UTILITY -> BayModuleComponent().apply {
                capacity = 4 + tier
                launchRate = (2f - (tier * 0.05f)).coerceAtLeast(0.5f)
            }
            else -> PowerCoreComponent()
        }
    }

    fun createEnemy(x: Float, y: Float, tier: Int = 1): Entity {
        val shipEntity = engine.entity {
            with<EnemyComponent>()
            with<TransformComponent> {
                position.set(x, y, 0f)
                size.set(1.2f, 1.2f)
            }
            with<PhysicsComponent> {
                velocity.set(0f, 0f)
                friction = 1.0f
            }
            with<ShipStatsComponent> {
                this.tier = tier
                for (i in 0..3) {
                    shieldQuadrants[i] = 50f + (tier * 10f)
                    maxShieldQuadrants[i] = shieldQuadrants[i]
                    hullQuadrants[i] = 100f + (tier * 20f)
                    maxHullQuadrants[i] = hullQuadrants[i]
                }
                powerCores.add(200f)
                maxPowerCores.add(200f)
            }
            with<TrailComponent> {
                color = Color(1.0f, 0.4f, 0.2f, 0.6f)
            }
            with<WeaponPlatformComponent>()
            with<SpriteComponent> {
                val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
                    setColor(Color.ORANGE)
                    fillTriangle(0, 0, 64, 0, 32, 64)
                }
                val texture = Texture(pixmap)
                sprite.set(Sprite(texture))
                pixmap.dispose()
            }
        }

        val weapon = generateModule(ModuleCategory.WEAPON_PRIMARY, tier, Subtier.A) as WeaponModuleComponent
        val engineMod = generateModule(ModuleCategory.ENGINE, tier, Subtier.A) as ImpulseEngineComponent

        val ship = shipEntity[ShipComponent.mapper] ?: ShipComponent().also { shipEntity.add(it) }
        ship.addSlot(ModuleCategory.WEAPON_PRIMARY).equippedModule = weapon
        ship.addSlot(ModuleCategory.ENGINE).equippedModule = engineMod

        shipEntity.add(weapon)
        shipEntity.add(engineMod)

        return shipEntity
    }

    fun createTrainingDummy(x: Float, y: Float): Entity {
        return engine.entity {
            with<EnemyComponent>()
            with<TransformComponent> {
                position.set(x, y, 0f)
                size.set(1.5f, 1.5f)
            }
            with<ShipStatsComponent> {
                for (i in 0..3) {
                    shieldQuadrants[i] = 200f
                    maxShieldQuadrants[i] = 200f
                    hullQuadrants[i] = 500f
                    maxHullQuadrants[i] = 500f
                }
            }
            with<SpriteComponent> {
                val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888).apply {
                    setColor(Color.RED)
                    drawRectangle(0, 0, 64, 64)
                    drawRectangle(8, 8, 48, 48)
                    fillCircle(32, 32, 16)
                }
                val texture = Texture(pixmap)
                sprite.set(Sprite(texture))
                pixmap.dispose()
            }
        }
    }

    fun createDrone(x: Float, y: Float, owner: Entity): Entity {
        return engine.entity {
            with<DroneComponent> {
                parent = owner
                type = "OFFENSIVE"
            }
            with<TransformComponent> {
                position.set(x, y, 0.05f)
                size.set(0.4f, 0.4f)
            }
            with<PhysicsComponent> {
                velocity.set(0f, 0f)
                friction = 2.0f
            }
            with<SeekingComponent> {
                target = owner
                turnRate = 450f
                velocityInheritance = 0.9f
            }
            with<SpriteComponent> {
                val pixmap = Pixmap(32, 32, Pixmap.Format.RGBA8888).apply {
                    setColor(Color.CYAN)
                    fillTriangle(16, 0, 0, 32, 32, 32)
                }
                val texture = Texture(pixmap)
                sprite.set(Sprite(texture))
                pixmap.dispose()
            }
        }
    }
}
