# ARCHITECTURE INDEX: SEEDS OF SOVEREIGNTY

This index defines the logic layers and file responsibilities for efficient AI synchronization.

## 1. DATA LAYER (/component/)
*   **PhysicsComponents.kt:** Position, Velocity, Player-tag.
*   **DefenseComponents.kt:** ArcedDefense (Hull/Shield arcs), PowerCore.
*   **RadarComponents.kt:** Radar data and blip storage.
*   **CombatComponents.kt:** WeaponModules, Sequential Fire barrels, Projectiles.
*   **EvolutionComponents.kt:** Tier/SubTier definitions.
*   **HierarchyComponents.kt:** Attachment (Parent-Child) and Control Groups.
*   **StationComponents.kt:** Docking zones, Shop inventory, Player credits.
*   **MissionComponents.kt:** Active mission data and objective targets.
*   **HudComponents.kt:** Global shader effects (Glitch, Flicker, Alpha).
*   **VisualComponents.kt:** Chassis Shape, Visual Barrel decorators.
*   **TrailComponents.kt:** Pre-allocated ribbon trail buffers.

## 2. LOGIC LAYER (/system/)
*   **PhysicsSystems.kt:** Input reading (Joystick/KB) -> Velocity -> Position.
*   **DefenseSystems.kt:** Module Attachment sync, Power Core throttling.
*   **RadarSystems.kt:** Distance-scaled blip calculation.
*   **ProgressionSystems.kt:** Reactive Evolution (advancing Tiers/SubTiers).
*   **VisualSystems.kt:** Engine Trail aging and noise-based turbulence.
*   **CameraSystems.kt:** Smooth interpolation + **Adaptive Zoom (Ship ~1% Screen).**
*   **RenderSystems.kt:** World-space rendering (Chassis, Barrels, Trails).
*   **HudRenderSystems.kt:** UI-space rendering (Tactical Arcs, Radar Blips).
*   **BackgroundSystems.kt:** 4-layer Starfield Parallax (Noise-based).

## 3. ENGINE LAYER
*   **SovereigntyGame.kt:** KtxGame entry point. Owns the Global Camera.
*   **PlayScreen.kt:** Master Controller. UI Stages, Fleks World creation, Init logic.
*   **TextureService.kt:** **Asset Factory.** Bakes procedural NinePatches, Skin, and Fonts.

## 4. UI SPECIFICATIONS
*   **Joystick:** Lower-Left. Scaled to 1/3 screen height.
*   **Command Matrix:** Lower-Right. Coupled height to joystick. 180x80 category buttons.
*   **Dev Button:** Top-Center.
*   **Tactical HUD:** Top-Left. Concentric arcs.
*   **Radar HUD:** Top-Right. Scanning sweep.
