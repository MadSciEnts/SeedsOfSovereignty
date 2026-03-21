package com.srw.seedofsov.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.ModuleCategory
import com.srw.seedofsov.model.Subtier
import ktx.ashley.entity
import ktx.ashley.with

/**
 * Factory for creating game entities.
 * Truth on Disk: Supports Player, Enemies, Swarm Drones, and Training Dummies.
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

            // 1. Basic Modules
            val engineModule = with<ImpulseEngineComponent> { thrustPower = 10f }
            val powerModule = with<PowerCoreComponent>()
            val shieldModule = with<ShieldModuleComponent>()

            // 2. Epoch 1.12 "Queen Ant" capability: Multiple Drone Bays in UTILITY slots
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

            // 3. Weapons
            val primaryWeapon = with<WeaponModuleComponent> {
                name = "Kinetic Cannon"
                category = ModuleCategory.WEAPON_PRIMARY
                projectileName = "Kinetic Bolt"
                projectileSpeed = 45f
                subtier = rank.subtier
            }

            // 4. Multi-Slot Assembly
            with<ShipComponent> {
                addSlot(ModuleCategory.ENGINE).equippedModule = engineModule
                addSlot(ModuleCategory.POWER_CORE).equippedModule = powerModule
                addSlot(ModuleCategory.SHIELD).equippedModule = shieldModule

                // Multiple Utility Slots for Swarms
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

    fun createEnemy(x: Float, y: Float): Entity {
        return engine.entity {
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
                for (i in 0..3) {
                    shieldQuadrants[i] = 50f
                    maxShieldQuadrants[i] = 50f
                    hullQuadrants[i] = 100f
                    maxHullQuadrants[i] = 100f
                }
                powerCores.add(200f)
                maxPowerCores.add(200f)
            }
            val weaponModule = with<WeaponModuleComponent> {
                name = "Enemy Autocannon"
                category = ModuleCategory.WEAPON_PRIMARY
                projectileName = "Kinetic Bolt"
                projectileSpeed = 30f
                fireRate = 2.0f
                subtier = Subtier.A
            }
            with<ShipComponent> {
                addSlot(ModuleCategory.WEAPON_PRIMARY).equippedModule = weaponModule
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
    }

    /**
     * Creates a stationary target for drone testing.
     */
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

    /**
     * Creates a small agile Drone for the "Queen Ant" swarm mechanics.
     */
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
                turnRate = 450f // Very agile
                velocityInheritance = 0.9f
            }
            // Add a simple weapon module to the drone so it can use WeaponSystem logic
            // or we handle it in a specialized DroneCombatSystem.
            // For now, let's use the DroneComponent's data.
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
