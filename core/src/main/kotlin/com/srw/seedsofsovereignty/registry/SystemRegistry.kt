package com.srw.seedsofsovereignty.registry

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.WorldConfiguration
import com.srw.seedsofsovereignty.manager.EvolutionManager
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.system.*
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * ARCHITECTURAL WARNING: LEAD ARCHITECT MANDATE (v3.0.2)
 * ROLE: The Immutable ECS Execution Registry.
 *
 * FIXES:
 * 1. [RIBBONS] - Integrated RibbonRenderSystem for movement trails.
 */
object SystemRegistry {

    fun WorldConfiguration.configureSystems(
        batch: SpriteBatch,
        drawer: ShapeDrawer,
        shipCamera: OrthographicCamera,
        shipViewport: Viewport,
        uiViewport: Viewport,
        skin: Skin,
        evolutionManager: EvolutionManager,
        sectorManager: SectorManager,
        starTextures: List<Texture>
    ) {
        systems {
            // --- FOUNDATION ---
            add(ClearScreenSystem())
            add(SectorPopulationSystem(shipCamera, sectorManager))
            add(StarfieldSystem(batch, shipCamera, starTextures))

            // --- INPUT & AI ---
            add(PlayerInputSystem(shipViewport))
            add(EnemyAISystem(shipCamera))
            add(SteeringSystem())

            // --- FLEET LOGIC ---
            add(DroneRegenSystem())
            add(DroneFormationSystem(shipCamera))

            // --- PHYSICS & CAMERA ---
            add(MoveSystem())
            add(CameraSystem(shipCamera))

            // --- COMBAT PIPELINE ---
            add(WeaponSystem())
            add(CombatSystem())
            add(CollisionDamageSystem())
            add(VitalsSystem())
            add(StatusEffectSystem())
            add(LifeSpanSystem())

            // --- STATE & SENSORS ---
            add(EvolutionSystem(evolutionManager))
            add(EvolutionCelebrationSystem(batch, drawer, uiViewport))
            add(SensorSystem())
            add(SensorVisualSystem())
            add(StationSystem())
            add(SalvageSystem())

            // --- LAYER 0: PHYSICAL FLEET (World Space) ---
            add(EntityRenderSystem(batch, drawer, shipViewport, skin))

            // --- LAYER 1: BULLETS/VFX/TRAILS (World Space) ---
            add(RibbonRenderSystem(batch, drawer, shipViewport))
            add(RenderSystem(batch, drawer, shipViewport, skin))

            // --- INTERFACE PASS ---
            add(RadarRenderSystem(batch, drawer, uiViewport))
            add(DiagnosticSignalSystem())

            // --- LAYER 2: DIEGETIC AR HUD (AR Bridge) ---
            add(DiegeticHUDSystem(batch, drawer, shipViewport, uiViewport, skin, sectorManager))

            // --- OVERLAYS & CLEANUP ---
            add(ApertureSystem(batch, drawer, uiViewport))
            add(OriginSystem())
        }
    }
}
