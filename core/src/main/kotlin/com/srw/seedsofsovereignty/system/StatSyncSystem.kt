package com.srw.seedsofsovereignty.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.srw.seedsofsovereignty.component.*

/**
 * STAT SYNC SYSTEM: Aggregates data from attached modules into the parent ship's summary components.
 * This ensures the HUD (Nautilus) and other top-level systems have a single point of truth.
 * Adheres to Fleks 2.x zero-allocation patterns.
 */
class StatSyncSystem : IteratingSystem(
    family { all(PlayerComponent, ArcedDefenseComponent, PowerCoreComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val summaryDefense = entity[ArcedDefenseComponent]
        val summaryPower = entity[PowerCoreComponent]

        var totalIntegrity = 0f
        var coreCount = 0

        var sFore = 0f; var sAft = 0f; var sPort = 0f; var sStbd = 0f
        var hFore = 0f; var hAft = 0f; var hPort = 0f; var hStbd = 0f

        world.family { all(AttachmentComponent) }.forEach { module ->
            if (module[AttachmentComponent].parent == entity) {

                // Aggregate Power Core Data
                if (module.has(PowerCoreComponent)) {
                    val mPower = module[PowerCoreComponent]
                    totalIntegrity += mPower.integrity
                    coreCount++

                    // Sync power levels
                    summaryPower.weaponPower = mPower.weaponPower
                    summaryPower.shieldPower = mPower.shieldPower
                    summaryPower.enginePower = mPower.enginePower
                }

                // Aggregate Defense Data (Shields/Hull)
                if (module.has(ArcedDefenseComponent)) {
                    val mDef = module[ArcedDefenseComponent]
                    sFore += mDef.shieldFore; sAft += mDef.shieldAft
                    sPort += mDef.shieldPort; sStbd += mDef.shieldStbd
                    hFore += mDef.hullFore; hAft += mDef.hullAft
                    hPort += mDef.hullPort; hStbd += mDef.hullStbd
                }
            }
        }

        // Apply aggregated totals to summary
        if (coreCount > 0) summaryPower.integrity = totalIntegrity / coreCount

        entity.configure {
            it += summaryDefense.copy(
                shieldFore = sFore, shieldAft = sAft, shieldPort = sPort, shieldStbd = sStbd,
                hullFore = hFore, hullAft = hAft, hullPort = hPort, hullStbd = hStbd
            )
        }
    }
}
