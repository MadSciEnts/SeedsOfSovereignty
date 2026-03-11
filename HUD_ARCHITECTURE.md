# HUD ARCHITECTURE: THE DIEGETIC ORCHESTRATOR (v1.1.0)

## 1. THE AR ENGINE (GOLD STANDARD)
- **Zero-Allocation:** No new objects created during render loop.
- **Space-Agnostic:** Elements exist in SCREEN or WORLD space with inter-space tethers.
- **Pivot-Centric:** All transformations relative to a local normalized pivot.

## 2. INSTRUMENT CLASSIFICATION
- **TACTICAL_STICK (Z: 5):** Primary interactive controls (Movement/Aim). Tethered to source world position.
- **DATA_READOUT (Z: 2):** Numerical telemetry (Distance, Velocity). Top-Right center mapping.
- **STATUS_LIGHT (Z: 10):** Diagnostic core and quadrant rings.
- **PANEL_BG (Z: 1):** Semi-transparent blue-grey terminal windows with neon cyan borders.

## 3. TEXTURE & VISUAL DNA
- **Palettes:** Faction-appropriate energy colors (Vanguard: Cyan, Swarm: Green, Ancients: Gold).
- **Scanlines:** Subtle 10% opacity HUD overlay.
- **Scan Period:** Diegetic scanline period set to 4.0f units per cycle.

## 4. INTERACTION & FX
- **Tap-to-Target:** Interaction interceptor in PlayScreen routes touches to HUD hitboxes.
- **Pulsing Tethers:** Locked targets trigger a 0.5Hz intensity pulse on their tether lines.
- **GLITCH:** Structural integrity < 20% triggers random localX/Y jitter on HUD entities.
