# SEED OF SOVEREIGNTY: MASTER SYSTEMS ARCHITECTURE (v1.2.0)

## 1. CAMERA & VIEWPORT PARTITIONING (v1.2.0)
### 1.1 The Ship Partition (Physical Layer)
- **Component:** `shipCamera` & `shipViewport`.
- **Units:** Orthographic (1280x720 world units). 
- **Role:** Exclusively renders physical entities (Ships, Debris, Particles). This layer is immune to UI matrix pollution.
- **Following:** Always centered on the player's "Lead Point".

### 1.2 The UI Partition (AR Layer)
- **Component:** `uiViewport`.
- **Units:** Screen-Pixel relative (Bottom-Left Origin).
- **Role:** Renders diegetic AR overlays, tethers, and static HUD widgets.
- **Projection Bridge:** AR elements in this layer track world entities using the `(uiWidth / 2f) + (sPos - pPos)` world-offset logic.

## 2. ENVIRONMENTAL & SENSOR LOGIC
### 2.1 Infinite Starfield
- **Parallax:** 4 layers (Nebula, Distant, Mid, Near). 
- **Texture:** UV Shifting quadruplets with Faction-based tinting.
### 2.2 Minimap & Scanning
- **Sensor Range:** Entity detection is bound by the `SensorComponent.radius`.
- **Minimap Overlay:** Uses `(uiWidth - 160, uiHeight - 160)` anchor in Bottom-Left origin space.

## 3. STATION & MODULE ECONOMY
### 3.1 The "Swap or Scrap" Loop
- **Direct Action:** Modules found in wrecks or stations must be immediately swapped or scrapped for Essence.
### 3.2 Diegetic Market
- **Hangar UI:** 3-state flow (Welcome, Category, List). Physically hidden during flight.

## 4. FLEET ECS ARCHITECTURE
- **System Order:** Movement -> Combat -> Vitals -> SensorVisual -> DiegeticHUD.
- **Z-Index Stack:** [0: Physics Objects] -> [1: AR Tethers] -> [2: AR Labels] -> [3: Static HUD Overlays].
