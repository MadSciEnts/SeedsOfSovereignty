package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ModuleGenerator

/**
 * Manages ship progression and hardware synchronization.
 * Updated (v0.5.3): Phase 9.1 Drone Control Crosstalk Fix - Sync slotIndex to ModuleGenerator.
 */
class EvolutionManager {

    /**
     * Dev Helper: Jumps the player to a specific tier for testing.
     * Default behavior: Fills essence to trigger natural sub-tier evolution.
     */
    fun cheatEvolve(world: World, targetTier: Int = -1) {
        val family = world.family { all(PlayerComponent, EvolutionComponent) }
        if (family.numEntities > 0) {
            val player = family.first()
            with(world) {
                if (targetTier > 0) {
                    applyTierBaseline(world, player, targetTier)
                } else {
                    val evo = player[EvolutionComponent]
                    evo.essenceCollected = evo.essenceRequired
                }
            }
        }
    }

    /**
     * Applies the mandatory hardware baseline for a new Tier.
     * v0.3.14: Phase 5.12 - Absolute Operational Restoration.
     */
    fun applyTierBaseline(world: World, player: Entity, tier: Int) {
        with(world) {
            val evo = player[EvolutionComponent]
            val weapon = player[WeaponComponent]
            val module = player[WeaponModuleComponent]

            evo.currentTier = tier
            evo.subTier = 1

            weapon.barrels = 1
            weapon.currentBarrel = 0
            weapon.barrelSequence.clear()
            weapon.barrelSequence.add(0)

            val stockWeapon = ModuleGenerator.generateWeapon(tier, Rarity.COMMON).apply {
                name = "STOCK T$tier"
                description = "Standard issue hardware for your new chassis."
            }

            // Task Fix: Sync Control DNA so the HUD knows when to switch to Joystick
            module.name = stockWeapon.name
            module.description = stockWeapon.description
            module.projectileTier = stockWeapon.projectileTier
            module.rarity = stockWeapon.rarity
            module.controlType = stockWeapon.controlType
            // v0.3.14: Absolute Operational Restoration - Forensic Schema Sync
            module.tacticalSchema = stockWeapon.tacticalSchema
            module.fireRate = stockWeapon.fireRate
            module.projectileSpeed = stockWeapon.projectileSpeed
            module.projectileColor = stockWeapon.projectileColor
            module.effects.clear()
            module.effects.addAll(stockWeapon.effects)

            syncDroneSlots(world, player, tier)

            if (player.has(ModuleSlotComponent)) {
                val slotComp = player[ModuleSlotComponent]
                slotComp.slots.find { it.type == SlotType.WEAPON }?.let { ws ->
                    ws.equippedName = module.name
                    ws.equippedTier = module.projectileTier
                }
            }
        }
    }

    private fun syncDroneSlots(world: World, player: Entity, tier: Int) {
        with(world) {
            val slotComp = if (player.has(ModuleSlotComponent)) player[ModuleSlotComponent] else {
                player.configure { it += ModuleSlotComponent() }
                player[ModuleSlotComponent]
            }

            val targetDroneSlots = when {
                tier >= 13 -> 4
                tier >= 11 -> 3
                tier >= 9 -> 2
                tier >= 5 -> 1
                else -> 0
            }

            val currentDroneSlots = slotComp.slots.count { it.type == SlotType.DRONE }

            for (i in currentDroneSlots until targetDroneSlots) {
                val newSlotIndex = i
                val sEntity = world.entity { e ->
                    e += DroneLauncherComponent(slotIndex = newSlotIndex)
                    // TVM Phase 9.1: Pass slot index to generate unique tactical schema
                    e += ModuleGenerator.generateDroneModule(tier, Rarity.COMMON, slotIndex = newSlotIndex)
                }

                val dModule = sEntity[DroneModuleComponent]
                slotComp.slots.add(ModuleSlot(
                    type = SlotType.DRONE,
                    tier = tier,
                    equippedName = dModule.name,
                    equippedTier = tier,
                    slotEntity = sEntity
                ))
            }
        }
    }
}
