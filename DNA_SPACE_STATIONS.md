# DNA: SPACE STATIONS & COMMERCE

This document defines the specialized HUD logic and ECS structure for Space Stations, Shops, and Repairs.

## 1. Visual & Physical Structure
Space Stations are large, stationary entities.
*   **Docking Zone:** A circular proximity area around the station.
*   **Docking Interaction:** A "Dock" button appears in the HUD when inside the zone.
*   **Station HUD:** Upon docking, the main game view is overlaid with the Station Menu (Shop, Repair, Missions, Undock).

## 2. Storefront Logic (The Bazaar)
Shops are dynamic and tier-aware.
*   **Inventory Generation:** 
    *   **Player Tier:** Stations only stock items relevant to the player's current Epoch/Tier.
    *   **Distance Scaling:** Quality/Rarity increases as `distance from (0,0)` increases.
*   **UI Representation:**
    *   Modules are represented as interactive buttons with unique procedural icons.
    *   **Detail Panel:** Selecting a module displays a comparison matrix (Current vs. Potential stats).
*   **Categories:** Primary, Secondary, Utility, Engines, etc.

## 3. Repair & Maintenance
*   **Hull/Shield Restoration:** Fixed cost or resource-based repair.
*   **Power Core Calibration:** Repairing core integrity to restore `totalOutput`.

## 4. Fleks Implementation

### Components
*   **StationComponent:** Stores `dockingRange`, `repairCost`, and `stationName`.
*   **ShopComponent:** Stores `inventory` (List of module templates) and `categoryFilters`.
*   **InventoryComponent:** Attached to player ship to track currency and stored modules.

### Systems
*   **DockingSystem:** Monitors distance between player and stations. Triggers the HUD "Dock" prompt.
*   **EconomySystem:** Handles transactions and calculates price modifiers based on station distance.
