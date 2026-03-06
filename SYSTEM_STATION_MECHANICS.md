# SYSTEM: Station Mechanics & Module Economy

## 1. The "Swap or Scrap" Loop
- **No Inventory:** Adheres to the core design pillar. Players cannot hoard modules.
- **Direct Slotting:** When interacting with a Station vendor or a procedural Wreck, the player is presented with a module. They must choose to either:
    - **Swap:** The new module immediately replaces an existing one in a compatible slot.
    - **Scrap:** The module is destroyed, and the player receives a fraction of its value in **Essence**.
- **Undocking:** The station UI closes, and the player is pushed back to resume gameplay.

## 2. Professional Station Hangar UI (v0.1.1 Update)
- **Multi-State Flow:** 
    - **Welcome:** Ship status, repair options, and entry to market.
    - **Categories:** Weaponry, Drones, Utility (Planned).
    - **Weapon Market:** List-based selection with comparison HUD.
- **Technical Visuals:**
    - **Data-Link:** A procedural connection line that tracks from the selected market item to the tactical analysis widget.
    - **Iconography:** Projectile previews scaled to 4.5x for high visibility.
    - **Symmetry:** Real-time comparison between "Equipped" (Left) and "Station Offer" (Right).

## 3. The Module Architecture (Fleks)
- **Slot-Based Hulls:** High-tier ships (T8+) utilize `WeaponModuleComponent` for swappable logic.
- **Market Generation:** `MarketComponent` on Stations holds a list of procedural weapon offers.
- **Manager Layer:** `MarketManager` and `DockingManager` decouple UI from ECS logic.

## 4. Drone Module DNA (Tier 9+)
- **`DroneLauncherComponent` Fields:**
    - `droneType: String` (e.g., "Kinetic", "Pulse", "Repair")
    - `behaviorMode: Enum` (OFFENSIVE, DEFENSIVE, STRATEGIC)
    - `maxDrones: Int`
    - `regenRate: Float` (Time in seconds to build a new drone)
