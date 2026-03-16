# UI: MENU FLOW & STATE MACHINE

## 1. Boot Sequence
- **Splash Screen:** Logo fade-in with procedural "Glitch" effect. Transitions to Title Screen.
- **Title Screen:** Large "Seeds of Sovereignty" logo. "Tap to Start" interaction. Background parallax starfield.

## 2. Main Menu
Options:
- **Continue:** Load last saved `game_save.json`. Grayed out if no save exists.
- **New Cycle:** Resets player to Tier 1, Sub-tier A. Overwrites current save (with confirmation).
- **Ancient Tech (Gallery):** Viewer for unlocked tiers and lore.
- **Settings:** Access to Options Menu.
- **Quit:** Close application (Desktop only).

## 3. Options Menu
- **Audio:** Master, Music, and SFX volume sliders.
- **Language Selection:** Toggle between English, Spanish, German, etc. (Updates `I18NBundle`).
- **Visuals:** Particle intensity toggle, screen shake toggle.
- **Data:** Button to "Wipe All Data" (Clear preferences and saves).

## 4. Save System Architecture
- **Save Path:** `Gdx.files.local("saves/seed_0.json")`.
- **Format:** JSON serialization of `EvolutionComponent`, `InventoryComponent`, and `MissionComponent`.
- **Auto-Save:** Triggered upon Docking at a Space Station or reaching a Narrative Zenith (Tier 12, 24, 36).

## 5. Screen Navigation (KtxGame)
1. `Splash -> Title`
2. `Title -> MainMenu`
3. `MainMenu -> PlayScreen` (New or Load)
4. `MainMenu -> Options`
5. `PlayScreen -> MainMenu` (via Pause/Quit)
