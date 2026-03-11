# SEED OF SOVEREIGNTY: MASTER SYSTEMS ARCHITECTURE (v1.1.0)

## 1. CAMERA & VIEWPORT PIPELINE
### 1.1 Following & Culling
- **Lead Offset:** Camera follows `player.pos + (player.velocity * 0.5s)`.
- **Frustum Culling:** `RenderSystem` ignores entities > 200 units off-screen.
### 1.2 Dynamic Effects
- **Speed Zoom:** Lerps from 1.0 to 1.4 based on velocity.
- **Shake/Flash:** Decaying random offsets on impact; color buffer tints on breach.

## 2. ENVIRONMENTAL & SENSOR LOGIC
### 2.1 Infinite Starfield
- **Parallax:** 4 layers (Nebula, Distant, Mid, Near). 
- **Texture:** UV Shifting quadruplets with Faction-based tinting.
### 2.2 Minimap & Scanning
- **Sensor Range:** Entity detection is bound by the `SensorComponent.radius`.
- **Minimap Overlay:** Top-right circular HUD element. Uses `(uiWidth - 160, uiHeight - 160)` anchor in Bottom-Left origin space.

## 3. STATION & MODULE ECONOMY
### 3.1 The "Swap or Scrap" Loop
- **Direct action:** modules found in wrecks or stations must be immediately swapped or scrapped for Essence.
### 3.2 Diegetic Market
- **Hangar UI:** 3-state flow (Welcome, Category, List). Physically hidden during flight to prevent occlusion.

## 4. FLEET ECS ARCHITECTURE
- **System Order:** Movement -> Combat -> Vitals -> SensorVisual -> DiegeticHUD.
- **Z-Index Stack:** [0: Physics Objects] -> [1: AR Tethers] -> [2: AR Labels] -> [3: Static HUD Overlays].
