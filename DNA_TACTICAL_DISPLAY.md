# DNA: TACTICAL DISPLAY (TOP-LEFT)

This document defines the specialized HUD logic for the tactical ship status display.

## 1. Visual Structure (Top-Left Corner)
The display is a concentric "Nautilus" style schematic.

*   **Center:** The Power Core.
*   **Inner Ring:** Quad Arcs representing Hull Integrity (Fore, Aft, Port, Starboard).
*   **Outer Ring:** Quad Arcs representing Shield Strength (Fore, Aft, Port, Starboard).

## 2. Core Damage & Power Scaling Logic
The Power Core is not just a health bar; it is the source of all performance.

### A. Core Integrity
*   If the **Power Core** takes damage, `totalOutput` in `PowerCoreComponent` decreases.
*   **The Squeeze:** As `totalOutput` drops, the player can no longer maintain 100% performance across all 4 sliders. 
*   **The Choice:** The player must manually slide down non-essential systems (e.g., lower Engine Power to keep Weapons at full capacity) to avoid global system slowdown.

### B. Arced Defense (Hull/Shield)
*   Damage is calculated based on the angle of impact relative to the ship's rotation.
*   Each of the 4 Shield Arcs has its own hit points. If a Shield Arc is at 0, damage passes to the corresponding Hull Arc.
*   If a Hull Arc is at 0, damage passes directly to the **Power Core**.

## 3. Fleks Implementation

### Components
*   **HealthComponent:** Stores `current` and `max` HP.
*   **ArcedDefenseComponent:** Stores 4x Shield HP and 4x Hull HP values.
*   **PowerCoreComponent:** (Updated) Now includes `integrity: Float` [0.0 to 1.0].

### Systems
*   **DamageSystem:** Calculates angle of impact -> determines which Arc takes the hit -> applies spillover to Core if Arcs are depleted.
*   **TacticalRenderSystem:** Uses a `ShapeDrawer` to draw the concentric arcs in the top-left HUD. The color of the arcs (Blue/Green -> Yellow -> Red) reflects health percentage.
*   **PowerThrottlingSystem:** Calculates `AvailablePower = PowerCore.totalOutput * PowerCore.integrity`. If the sum of the 4 Sliders > `AvailablePower`, it applies a proportional penalty to all module cooldowns.
