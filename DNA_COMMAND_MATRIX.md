# DNA: COMMAND MATRIX & MODULE CONTROL

This document defines the specialized HUD logic for ship module management and the "Command Matrix" interface.

## 1. Physical Layout (Bottom-Right Corner)
The "Command Matrix" is a persistent UI area in the bottom-right corner, defined by absolute offsets from the screen edge.

### A. The Control Area (Center)
*   **Dimensions:** Roughly square, matching the height of the movement joystick on the left.
*   **Population:** Content changes dynamically based on the *Selected Module*.
*   **Control Types:**
    *   **Joystick (2D):** Used for Primary Weapons (Directional Aim) or Drones/Fighters (POI targeting).
    *   **Touchpad (2D):** Used for Tertiary Weapons (Targeting reticule) or Drones/Fighters (Spatial orientation toggle: World vs. Local).
    *   **Buttons (Discrete):** Used for Secondary Weapons (Single trigger), Shields (Fore/Aft/Port/Stbd/Center), or Toggle modes.
    *   **Vertical Sliders (1D):** Used for PowerCore (Weapon/Shield/Engine/Utility power allocation).

### B. The Module Shelves (Edges)
*   **Category Shelf (Lowest Right):** Persistent filter buttons [Primary, Secondary, Tertiary, Quadrary, Shields, Power, Drones, Fighters].
*   **Selection Shelf (Top of Matrix):** Lists icons for all equipped modules within the *selected category*. Tapping one populates the Control Area.

## 2. Module Logic & Interactions

| Module Category | Primary Input | Interaction Logic | Performance Impact |
| :--- | :--- | :--- | :--- |
| **Primary** | Joystick | Determines direction of fire. | Rotation Speed / Accuracy. |
| **Secondary** | Button | Fire-and-forget (e.g., Torpedoes). | Cooldown Management. |
| **Tertiary** | Touchpad | Establish specific target coordinates. | Splash damage / Precision. |
| **Shields** | 5-Way Buttons | Redistribute shield strength (Fore, Aft, Port, Stbd, Center). | Absorption efficiency per side. |
| **PowerCore** | 4x Sliders | Allocate limited pool to [Wpn, Shld, Eng, Util]. | Affects all cooldowns & performance. |
| **Drone/Fighter** | Touchpad | POI for orbiting units. Toggle: World vs. Local. | Tactical positioning. |

## 3. Implementation Patterns (Fleks)

*   **SelectedModuleComponent:** Tags the module entity currently "Active" in the Command Matrix.
*   **ControlSystem:** Listens to UI inputs and applies them to the entity with `SelectedModuleComponent`.
*   **PowerDistributionSystem:** Scales the `cooldown` or `speed` values of all modules based on the `PowerCore` slider ratios.
*   **SpatialToggle:** A boolean on the Drone/Fighter module that flips input mapping:
    *   `World Space`: Y+ = Screen Up.
    *   `Local Space`: Y+ = Ship Forward.
