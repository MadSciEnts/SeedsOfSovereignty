# DNA: RADAR SYSTEM (TOP-RIGHT)

This document defines the specialized HUD logic for the long-range sensor and tactical radar display.

## 1. Visual Structure (Top-Right Corner)
The radar is a circular "Scanning" display.

*   **Center Point:** Represents the player ship.
*   **Concentric Circles:** Distance markers (e.g., 500u, 1000u, 5000u).
*   **Scanning Sweep:** A visual "sweep" line that rotates, updating blips as it passes.
*   **Blips:** 
    *   **Red:** Hostile entities.
    *   **Green:** Friendly/Neutral entities.
    *   **Yellow:** Objectives/Warp Gates.
    *   **White:** Destructible debris/Asteroids.

## 2. Sensor Range & Tier Progression
The effectiveness of the radar depends on the ship's Tier and Power allocation.

*   **Range Scaling:** Higher Tiers increase the maximum sensor radius.
*   **Power Allocation:** Utility Power (from `PowerCoreComponent`) affects the "Refresh Rate" (sweep speed) and "Detail Level" (showing entity names/health).
*   **Warp Sensitivity:** When a warp gate is nearby, the radar blip pulses to assist in navigation.

## 3. Fleks Implementation

### Components
*   **RadarComponent:** Attached to the player ship. Stores `range`, `sweepAngle`, and `blipData` (list of detected entities).
*   **VisibleOnRadarComponent:** Attached to any entity that should appear on radar. Stores `blipColor` and `iconType`.

### Systems
*   **SensorSystem:** Iterates over entities with `RadarComponent`. 
    *   Calculates relative distances to all entities with `VisibleOnRadarComponent`.
    *   Filters by `range`.
    *   Updates the `blipData` list.
*   **RadarRenderSystem:**
    *   Draws the circular radar background.
    *   Draws the rotating sweep line.
    *   Draws blips scaled to the radar's UI radius.
    *   Injected dependencies: `ShapeDrawer`, `uiViewport`.
