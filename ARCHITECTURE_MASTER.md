# ARCHITECTURE MASTER: The 48-Tier Evolution (v0.8.0)

## 1. The Progression Curve
The game is divided into 4 Epochs. Every 12 Tiers, the perspective and primary gameplay loop fundamentally shift.

| Epoch | Tiers | Title | Perspective | Genre |
| :--- | :--- | :--- | :--- | :--- |
| **I** | 1–12 | The Pilot | Single Ship | Arcade/Action |
| **II** | 13–24 | The Admiral | Small Fleet | Tactical Combat |
| **III** | 25–36 | The Overlord | Space Station | Base Management/Defense |
| **IV** | 37–48 | The Sovereign | Galactic Hive | Grand Strategy/4X |

## 2. The Concentric Quadrant Diagnostic (CQD)
Introduced in v0.8.0, the CQD replaces linear status bars with a diegetic, multi-layered "Blueprint" readout.

### 2.1 Ring Hierarchy
- **Outer Layers (Cyan):** Shields. Managed via individual Shield Modules. 1 Module = 1 Ring.
- **Inner Layers (Red):** Hull/Armor. Core Hull + optional Armor Modules.
- **Core (Yellow):** Internal Systems. Damage here directly impacts module performance.

### 2.2 Hybrid Slot Architecture
Ships utilize a mix of Fixed and Universal slots to balance necessity with specialization.
- **Fixed Slots:** Engine, Reactor, Primary Hull.
- **Universal Slots:** Weapons, Shields, Drones, Utilities. 
- **Chassis Limits:** DNA defines the `maxLayers` (Shield/Hull) a ship can support.

## 3. The Power Baseline (Epoch I)
- **Tier 9: Vanguard** (Current Apex - High Energy)
- **Tier 10-12:** Final Epoch I Evolutions.

## 4. Technical Milestones
- **v0.7.3:** Elliptical Phalanx implemented (Organic Drone Motion).
- **v0.8.0:** CQD Blueprint & Hybrid Slot Architecture defined.
