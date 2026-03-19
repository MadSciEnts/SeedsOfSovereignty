# Project Virtues - Seeds of Sovereignty

## 1. Core Vision
- **Theme:** Sci-Fi Space RPG/Sim inspired by the "Ender's Game" movie aesthetic.
- **Goal:** Provide a sense of immense scale and achievement, transitioning the player from a single pilot to a galactic sovereign.
- **Tone:** Technical, tactical, and expansive.

## 2. Progression & Scale
The game is divided into four major **Epochs**, each containing multiple **Tiers** (12 per epoch) and **Subtiers** (A, B, C).

### The Epochs
1. **Epoch 1 (Tiers 1-12):** Single player ship (1 unit scale). Manual impulse/tactical control. Use of **Warp Gates** for inter-sector travel.
2. **Epoch 2 (Tiers 13-24):** Carrier class (5 units scale). Introduction of fighter bays and destination-based **NAV engines**. Introduces **Warp Engines** for free-range travel.
3. **Epoch 3 (Tiers 25-36):** Admiral class. Command a fleet of Epoch 2 ships via tactical placement.
4. **Epoch 4 (Tiers 37-48):** Sovereign era. Operating a space station, managing sectors, and conquering the galaxy.

### Subtier Progression (A, B, C)
- Linear DPS increase (100% -> 200% -> 300%) via increased projectile emission points.
- **Rule:** Tier X Subtier C DPS must always be less than Tier X+1 Subtier A DPS.

## 3. Command Matrix & UI (Precise Placement & Procedural)
The player interacts with the ship through two primary command areas at the bottom of the screen.

- **Nautilus HUD:** Displays Health (Power Core), Hull, and Shields.
- **Lower Left (Navigation):** Impulse engine joystick (early) or navigation destination target (later).
- **Lower Right (Tactical):** Module selection, weapon operation, power balancing, and drone/fighter positioning.
- **Module Shelf:** A filterable shelf of categories. Selecting a category reveals specific modules; selecting a module reveals its unique controls.
- **UI Architecture:** **NO Scene2D Tables.** Use precise positioning, parenting, offsets, and anchors to prevent auto-scaling/squishing.
- **UI Interactions:** Custom components for Buttons, Toggles, Joysticks, TouchPads, and Vertical Sliders.
- **Visuals:** Procedurally generated UI in a scifi style, categorized by Epoch/Tier. Shaders for glitch effects and high-tech details.
- **UI Assets:** Procedurally generated NinePatch UI elements and buttons with synced UVs. Style generators to maintain visual consistency across tiers.

## 4. Ship Systems & Modules
Every ship requires a [Power Core, Shield, Hull, Impulse Engine].

### Procedural Ships & Modules
- **Blueprint System:** Defines ship chassis, module locations, and slot amounts.
- **Procedural Generators:** Both ships and modules (including their associated projectiles) are procedurally generated based on Tier.
- **Textures:** Real-time procedurally generated textures for ships and modules.

### Key Modules:
- **Power Core:** Supplies power categories (Weapons, Shields, Engines, Utilities). Players balance output via vertical sliders.
- **Shields:** Quadrant-based priority (Fore, Aft, Port, Starboard, Average).
- **Weaponry:** Primary (Joystick/Directional), Secondary (Single-fire/Missiles), Point Defense (Automated).
- **Bays:** Drone Bays (local flocking/orbiting) and Fighter Bays (long-distance targeting). Drones have "Ship Space" vs. "World Space" orientation toggles.

## 5. Combat & Projectiles
- **Categories:** Kinetic (Balanced), Ion (Shield-focused), Plasma (Slow/High Damage), Laser (Fast/Hull-focused).
- **Weapon logic:** Modules are tied to specific projectile types at generation (Swap/Scrap only, no inventory).
- **Difficulty:** Enemy ships are built using the same module logic but are generally tuned to be less capable than equivalent player ships.

## 6. World & Technical Style
- **Sectors:** Finite space units. Sector origin centered, roughly 10k unit limits to avoid floating-point jitter.
- **Sector Boundaries:** Enforced via radiation, asteroids, or anomalies to isolate players.
- **Tiered Sectors:** Each sector has a [Min Tier, Max Tier]. Difficulty increases with distance from origin.
- **Travel:** 
    - **Epoch 1:** Stationary **Warp Gates** (1-2 per sector).
    - **Epoch 2+:** **Warp Engines** (Fuel-based free travel).
- **Camera:** Dynamic zoom that scales out as the ship grows, maintaining a sense of relative scale while ensuring projectiles remain visible.
- **Background:** 3-layer parallax shader-based starfield. Farthest layers retire and new ones emerge as the camera zooms.
- **Code Style:** Pure data Components, logic-only Systems, heavy use of KTX for idiomatic Kotlin.
