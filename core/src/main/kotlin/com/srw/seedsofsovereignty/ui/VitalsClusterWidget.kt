package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Technical Vitals Cluster (v0.2.0).
 * Orchestrates multiple HudBar instances into a clean vertical stack.
 * Replaces the rigid manual drawing loop in UISystems.
 */
class VitalsClusterWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : Table(skin), SignalObserver {

    private var healthBar: HudBar? = null
    private var shieldBar: HudBar? = null
    private var essenceBar: HudBar? = null

    init {
        left().top()
        pad(10f)
    }

    /**
     * Updates internal bars based on live ECS data from the player ship.
     * Note: Prefer onSignalReceived for event-driven updates where available.
     */
    fun update(player: Entity) {
        var needsLayout = false
        with(world) {
            // 1. Health (Structural Integrity)
            if (player.has(HealthComponent)) {
                val hp = player[HealthComponent]
                if (healthBar == null) {
                    healthBar = HudBar(drawer, skin, "STRUCTURAL", Color.FIREBRICK, hp.current, hp.max)
                    add(healthBar).padBottom(8f).row()
                    needsLayout = true
                }
                healthBar?.current = hp.current
                healthBar?.max = hp.max
            }

            // 2. Shield (Deflector Array)
            if (player.has(ShieldComponent)) {
                val sp = player[ShieldComponent]
                if (shieldBar == null) {
                    shieldBar = HudBar(drawer, skin, "DEFLECTOR", Color.SKY, sp.current, sp.max)
                    add(shieldBar).padBottom(8f).row()
                    needsLayout = true
                }
                shieldBar?.current = sp.current
                shieldBar?.max = sp.max
            }

            // 3. Evolution (Link Synchronization)
            if (player.has(EvolutionComponent)) {
                val evo = player[EvolutionComponent]
                if (essenceBar == null) {
                    essenceBar = HudBar(drawer, skin, "EVO-LINK", Color.GOLD, evo.essenceCollected.toFloat(), evo.essenceRequired.toFloat())
                    add(essenceBar).padBottom(8f).row()
                    needsLayout = true
                }
                essenceBar?.current = evo.essenceCollected.toFloat()
                essenceBar?.max = evo.essenceRequired.toFloat()
            }
        }

        if (needsLayout) {
            invalidateHierarchy()
            pack()
        }
    }

    override fun onSignalReceived(signal: StatusSignal) {
        // Handle signals like "SYSTEMS_INTEGRITY" or label-based updates
        when (signal.label) {
            "STRUCTURAL", "SYSTEMS_INTEGRITY" -> healthBar?.let {
                it.current = signal.value
                signal.maxValue?.let { max -> it.max = max }
            }
            "DEFLECTOR" -> shieldBar?.let {
                it.current = signal.value
                signal.maxValue?.let { max -> it.max = max }
            }
            "EVO-LINK" -> essenceBar?.let {
                it.current = signal.value
                signal.maxValue?.let { max -> it.max = max }
            }
        }
    }
}
