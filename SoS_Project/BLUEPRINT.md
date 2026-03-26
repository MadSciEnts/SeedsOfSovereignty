# Seeds of Sovereignty - Technical Blueprint

## 1. The Command Matrix (The Central Nervous System)
The Command Matrix is the unified interface between the ship's modules and its "Pilot" (Player UI or Enemy AI).

### Core Components:
*   **`ShipModule` (Abstract Class):**
    *   `val id: String`
    *   `val category: ModuleCategory` (Primary, Secondary, Engine, etc.)
    *   `val controlType: ControlType` (JOYSTICK, TOUCHPAD, TOGGLE)
    *   `fun onUpdate(input: Vector2D, dt: Float)`: The module's internal logic.
*   **`CommandMatrix` (Class):**
    *   `val modules: List<ShipModule>`
    *   `var selectedModule: ShipModule?`
    *   `fun relayInput(input: Vector2D)`: Routes input to the selected module or the dedicated Navigation Matrix.

---

## 2. World Structure: Sectors & Precision
To maintain high performance and avoid floating-point errors, the universe is divided into **Sectors**.

*   **Sector Dimensions:** 20,000 x 20,000 units.
*   **Boundary Enforcement:** Traveling beyond 10,000 units from the sector origin triggers "C'thulu Space Monsters" (Lethal high-tier guardians).
*   **Difficulty Scaling:** 
    *   Enemies scale in Tier based on the distance from the Sector Origin.
    *   Each Sector has a `MinTier` and `MaxTier` rating.
*   **Bosses:** Mission-specific entities 1-2 Tiers above the sector maximum.

---

## 3. The 4 Epochs of Evolution
The game progresses through four distinct gameplay eras:

### Epoch 1: The Lone Pilot (Tiers 1-12)
*   **Ship Size:** 1.0 to 3.0 units.
*   **Assets:** Single ship + Drone Bays.
*   **Travel:** Warp Gates (Static portals between sectors).

### Epoch 2: The Carrier (Tiers 13-24)
*   **Ship Size:** 4.0 to 6.0 units.
*   **Assets:** Carrier vessel with Fighter and Bomber bays.
*   **Travel:** Warp Engines (Manual jump capability using Fuel).

### Epoch 3: The Admiral (Tiers 25-36)
*   **Fleet Size:** Up to 12 Ships.
*   **Command:** Player controls a Fleet Ship. Other Epoch 2 ships are equipped as "Command Modules" and act as tactical assets.
*   **Travel:** Fleet Warp.

### Epoch 4: The Sovereign (Tiers 37-48)
*   **Form:** Stationary Space Station.
*   **Gameplay:** Tower Defense + Fleet Management.
*   **Missions:** Sending fleets on simulated Epoch 1-3 missions to expand influence.

---

## 4. Points of Interest (POI)
*   **Space Stations:** Neutral safe harbors.
*   **Features:** Repair, Module Shop (Randomized based on distance from origin), Mission Terminal.

---

## 5. The Physics & Combat Engine
Inspired by **Naval Battles** and **Ender's Game**.

### Newtonian Movement:
*   **Inertia:** Drifting is a core mechanic. Ships require counter-thrust.
*   **Rotational Drag:** Larger ships (Tiers 24+) feel significantly "heavier."

### Shield Quadrants:
*   Ships have 4 health pools: `Shields(Front, Rear, Left, Right)`.
*   Strategic power management is required to survive naval broadsides.

---

## 6. The Rendering Pipeline
*   **Visual Style:** "Ender's Game" HUD / Procedural Vector Graphics.
*   **Texture System:** Trinity maps (Diffuse, Normal, Emissive) generated once and cached.
*   **Performance:** 1280x720 base height with dynamic width. Target 60+ FPS.
