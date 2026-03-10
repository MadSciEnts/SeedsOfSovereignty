# HUD: CONCENTRIC QUADRANT DIAGNOSTIC (CQD) - v0.8.0

## 1. Vision
The CQD is a diegetic "Blueprint" display in the top-left HUD corner. It provides real-time, high-fidelity status of the ship's defensive layers across 4 quadrants.

## 2. Visual Layers (Concentric Rings)
From Outermost to Innermost:
1. **Shield Layers (Cyan):** Multi-ring support. 1 Module = 1 Ring.
2. **Armor Layers (Red):** Represents physical plating.
3. **Core Hull (White/Grey):** The primary chassis integrity.
4. **Internal Systems (Yellow):** Critical components (Engines/Reactor).

## 3. Quadrant Logic (The 4-Way Split)
The ship is divided into 4 sectors (FRONT, REAR, LEFT, RIGHT).
- **Damage Routing:** Hits are calculated against the specific quadrant they impact.
- **HP Distribution:** Each segment (Ring x Quadrant) has its own HP value.
- **Bleed-Through:** If a shield segment is down, 100% damage hits the next layer. If it's active but weak, a % of damage "bleeds" to the hull.

## 4. Technical Specification: DiagnosticWidget
- **Anchor:** Top-Left HUD.
- **Rendering:** Procedural using `ShapeDrawer`. 
- **Method:** `arc(centerX, centerY, radius, startAngle, sweepAngle, thickness)`
- **Dynamic Assets:** Draws a vector-wireframe of the `currentChassis` in the center.

## 5. Signal Integration
The widget listens to the following signal patterns:
- `SHIELD_Q[0-3]_L[0-N]` (StatusSignal)
- `HULL_Q[0-3]_L[0-N]` (StatusSignal)
- `SYSTEMS_INTEGRITY` (StatusSignal)

## 6. Modularization Plan
- **DiagnosticWidget.kt:** Handles drawing logic and signal binding.
- **DiagnosticSignalSystem.kt:** A new Fleks system that aggregates module HP into quadrant signals.
- **ChassisGenerator.kt:** Defines the "Blueprint" wireframe geometry for each Tier.
