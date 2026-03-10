package com.srw.seedsofsovereignty.manager

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.util.ModuleGenerator
import com.srw.seedsofsovereignty.util.ShieldGenerator
import com.srw.seedsofsovereignty.util.ArmorGenerator

/**
 * Manages ship progression and hardware synchronization.
 * v0.8.9.1: Hardened Physical Slot Entity creation and linkage.
 */
class EvolutionManager {

    fun cheatEvolve(world: World, targetTier: Int = -1) {
        val family = world.family { all(PlayerComponent, EvolutionComponent) }
        if (family.numEntities > 0) {
            val player = family.first()
            val evo = with(world) { player[EvolutionComponent] }
            if (targetTier > 0) applyTierBaseline(world, player, targetTier)
            else applyTierBaseline(world, player, evo.currentTier + 1)
        }
    }

    fun applyTierBaseline(world: World, player: Entity, tier: Int) {
        with(world) {
            val evo = player[EvolutionComponent]
            val weapon = player[WeaponComponent]
            val module = player[WeaponModuleComponent]

            evo.currentTier = tier
            evo.subTier = 1

            // 1. Reset Base Weapon
            val stockWeapon = ModuleGenerator.generateWeapon(tier, Rarity.COMMON)
            module.name = stockWeapon.name
            module.tacticalSchema = stockWeapon.tacticalSchema
            module.projectileTier = tier

            // 2. Scale Chassis
            val maxShields = when { tier >= 25 -> 4; tier >= 13 -> 3; tier >= 5 -> 2; else -> 1 }
            val maxHull = if (tier >= 13) 2 else 1

            player.configure {
                it += ShipChassisComponent(maxShieldLayers = maxShields, maxHullLayers = maxHull, wireframeAsset = "blueprint_t$tier")
                if (!it.has(QuadrantHealthComponent)) it += QuadrantHealthComponent()
            }

            // 3. Sync Hardware
            syncHardware(world, player, tier)
        }
    }

    private fun syncHardware(world: World, player: Entity, tier: Int) {
        with(world) {
            val slotComp = if (player.has(ModuleSlotComponent)) player[ModuleSlotComponent] else {
                player.configure { it += ModuleSlotComponent() }
                player[ModuleSlotComponent]
            }

            // A. Fixed Core
            ensureFixedHardware(world, slotComp, SlotType.FIXED_HULL, tier)
            ensureFixedHardware(world, slotComp, SlotType.FIXED_ENGINE, tier)
            ensureFixedHardware(world, slotComp, SlotType.FIXED_REACTOR, tier)

            // B. Playtest Shield (Tier 2+)
            if (tier >= 2 && slotComp.slots.none { it.type == SlotType.UNIVERSAL }) {
                val sEntity = world.entity { e ->
                    e += ShieldGenerator.generateShield(tier, Rarity.COMMON)
                }
                slotComp.slots.add(ModuleSlot(SlotType.UNIVERSAL, tier, "DEFLECTOR", tier, sEntity))
            }
        }
    }

    private fun ensureFixedHardware(world: World, slotComp: ModuleSlotComponent, type: SlotType, tier: Int) {
        if (slotComp.slots.none { it.type == type }) {
            val hEntity = world.entity { e ->
                if (type == SlotType.FIXED_HULL) e += ArmorGenerator.generateHull(tier, Rarity.COMMON)
            }
            slotComp.slots.add(ModuleSlot(type, tier, "INTERNAL SYSTEMS", tier, hEntity))
        }
    }
}
