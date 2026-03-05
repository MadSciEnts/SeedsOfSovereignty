# SYSTEM: Station Mechanics & Module Economy

## 1. The "Swap or Scrap" Loop
- **No Inventory:** Adheres to the core design pillar. Players cannot hoard modules.
- **Direct Slotting:** When interacting with a Station vendor or a procedural Wreck, the player is presented with a module. They must choose to either:
    - **Swap:** The new module immediately replaces an existing one in a compatible slot.
    - **Scrap:** The module is destroyed, and the player receives a fraction of its value in **Essence**.
- **Undocking:** The station UI closes, and the player is pushed back to resume gameplay.

## 2. The Module Architecture (Fleks)
- **Slot-Based Hulls:** High-tier ships (T8+) will have `ModuleSlotComponent`s. Each slot has a `type` (Weapon, Drone, Utility) and a `size`.
- **Launcher Components:** `DroneLauncherComponent` and `WeaponPlatformComponent` will define the stats and behaviors of swappable modules.
- **Data-Driven Evolution:** The `EvolutionSystem` will transition to a `ModuleSystem` that unlocks *slots* rather than hard-coding weapon stats.

## 3. Drone Module DNA (Tier 9+)
- **`DroneLauncherComponent` Fields:**
    - `droneType: String` (e.g., "Kinetic", "Pulse", "Repair")
    - `behaviorMode: Enum` (OFFENSIVE, DEFENSIVE, STRATEGIC)
    - `maxDrones: Int`
    - `regenRate: Float` (Time in seconds to build a new drone)
- **Behavior Definitions:**
    - **DEFENSIVE:** (Default) Orbits the owner.
    - **OFFENSIVE:** Orbits the owner's current target.
    - **STRATEGIC:** Follows global commands set in a new `FleetCommandComponent` on the player entity.

## 4. UI/UX Flow
- **Station Proximity:** A "DOCK" button appears on the HUD when near a station.
- **Aperture Menu:** Docking triggers the main Aperture menu, with a new button for "MODULE HANGAR."
- **Hangar UI:** A grid-based view showing the ship's slots and the currently offered module, with clear "SWAP" and "SCRAP" buttons.
