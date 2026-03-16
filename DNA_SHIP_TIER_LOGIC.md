# DNA: SHIP TIER & EPOCH LOGIC

This document defines the structural evolution of ships from Tier 1 to 48, ensuring the ECS architecture remains modular enough to support genre shifts between Epochs.

## 1. Epoch I: The Pilot (Tiers 1–12)
**Focus:** Action/Arcade. Single ship focus.
**Base Loadout:** [Power Core, Shields, Impulse Engines, Hull].

| Tiers | Primary Weapon | Secondary Weapon | Drone Bay | Fighter Bay | Generic Slots |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1–3** | 1 | 1 | 0 | 0 | 0 |
| **4–6** | 1 | 1 | 1 | 0 | 0 |
| **7–9** | 1 | 1 | 2 | 1 | 0 |
| **10–12**| 1 | 1 | 0 | 0 | 6 (Generic) |

### Sub-Tier Evolution (A, B, C)
*   **Weapon Modules:** Lower collective cooldown via sequential barrel firing.
*   **Drone Bays:** Increase `maxDrones` (Sub-Tier A: 2, B: 4, C: 6).
*   **Fighter Bays:** Increase `maxFighters` (Sub-Tier A: 1, B: 2, C: 3).

## 2. Epoch II: The Admiral (Tiers 13–24)
**Focus:** Tactical Combat. Small fleet management.
**Base Loadout:** [Power Core, Shields, Impulse Engines, Warp Engines, Hull].

**Generic Module Slots:**
*   **Small Arms:** Can fit Primary or Secondary Weapons.
*   **Large Arms:** Can fit Tertiary or Quadrary Weapons.
*   **Utility Bay:** Can fit Drone or Fighter Bays.

## 3. Epoch III: The Overlord (Tiers 25–36)
**Focus:** Base Management & Defense. Space Station central hub.
**The Shift:** Modules are no longer just "attachments"—they are entire **Ships** controlled via the Command Matrix.
*   The Space Station has "Communication Slots." 
*   Assigning a "Ship" to a slot allows the player to command that ship as a module.

## 4. ECS Implementation Strategy

### Slotting System
*   **`SlotComponent`**: Attached to the Ship Core. Defines `slotType`, `offset`, and `occupiedBy` (Entity).
*   **`ModuleComponent`**: Attached to the module entity. Defines its `category` (Small Arm, Utility, etc.).

### Generic Modules
*   In Epoch II, systems will check if a module's `category` is compatible with the `SlotComponent.type` before allowing attachment.

### Sequential Capability
*   **`WeaponSystem`**: Iterates `barrelIndex` for sequential fire.
*   **`BaySystem`**: Manages spawning and recall of sub-entities (Drones/Fighters) up to `maxUnits` defined by Sub-Tier.
