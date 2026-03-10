package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * Technical Specification: DiagnosticSignalSystem (v0.8.9)
 * ROLE: The "Brain" of the CQD. Aggregates module health into quadrant signals.
 *
 * DESIGN PRINCIPLES:
 * 1. [HEALTH UNIFICATION] - v0.8.9: Now correctly aggregates both Hull and Armor modules.
 * 2. [ZERO ALLOCATION] - Uses cached quadrant list to reduce GC pressure.
 */
class DiagnosticSignalSystem : IteratingSystem(
    family { all(QuadrantHealthComponent, ModuleSlotComponent, TacticalHubComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val health = entity[QuadrantHealthComponent]
        val slots = entity[ModuleSlotComponent]
        val hub = entity[TacticalHubComponent]

        val quadrants = listOf(health.front, health.rear, health.left, health.right)

        // 1. AGGREGATION: Clear components
        quadrants.forEach {
            it.shieldLayers.clear()
            it.hullLayers.clear()
        }

        // 2. SCAN: Populate layers from equipped modules
        slots.slots.forEach { slot ->
            val moduleEntity = slot.slotEntity
            if (moduleEntity != null && world.contains(moduleEntity)) {
                if (moduleEntity.has(ShieldModuleComponent)) {
                    val shield = moduleEntity[ShieldModuleComponent]
                    val pct = shield.currentHP / shield.maxHP
                    quadrants.forEach { it.shieldLayers.add(pct) }
                }
                // Unified Hull/Armor Recognition (v0.8.9)
                if (moduleEntity.has(ArmorModuleComponent)) {
                    val armor = moduleEntity[ArmorModuleComponent]
                    quadrants.forEach { it.hullLayers.add(armor.currentHP / armor.maxHP) }
                }
                if (moduleEntity.has(HullModuleComponent)) {
                    val hull = moduleEntity[HullModuleComponent]
                    quadrants.forEach { it.hullLayers.add(hull.currentHP / hull.maxHP) }
                }
            }
        }

        // 3. BROADCAST: Push to TacticalHub
        broadcastQuadrant(hub, 1, health.front)
        broadcastQuadrant(hub, 3, health.rear)
        broadcastQuadrant(hub, 2, health.left)
        broadcastQuadrant(hub, 0, health.right)

        hub.signals["SYSTEMS_INTEGRITY"] = StatusSignal(value = health.systemsIntegrity, label = "SYSTEMS_INTEGRITY", maxValue = 100f)
    }

    private fun broadcastQuadrant(hub: TacticalHubComponent, qIdx: Int, data: QuadrantHealth) {
        data.shieldLayers.forEachIndexed { i, v ->
            val key = "SHIELD_Q${qIdx}_L$i"
            hub.signals[key] = StatusSignal(value = v, label = key, maxValue = 1f)
        }
        data.hullLayers.forEachIndexed { i, v ->
            val key = "HULL_Q${qIdx}_L$i"
            hub.signals[key] = StatusSignal(value = v, label = key, maxValue = 1f)
        }
    }
}
