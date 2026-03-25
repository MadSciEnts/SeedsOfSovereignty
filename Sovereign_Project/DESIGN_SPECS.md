# DESIGN SPECIFICATIONS: SOVEREIGN V4 (INTERNAL LAWS)

## 1. THE VIEWPORT LAW (SCALING)
- **Vertical Anchor:** Logical Height is fixed at 360.0 units.
- **Horizontal Variable:** `logicalW` expands to fill the screen width.
- **Asset Protection:** All pixel-art assets (32x32) must maintain 1:1 aspect ratio. Scaling must be uniform.
- **Coordinates:** (0,0) is Top-Left. (logicalW, 360) is Bottom-Right.

## 2. THE COMMAND MATRIX LAW (INTERFACE)
- **The Persistence Mandate:** The UI is a view into the ship's hardware. If a player moves a "Drone Target" on the Tactical Touchpad, that target stays there. The drones continue orbiting that spot even if the player switches the Matrix to "Shields."
- **The Area 2 Chameleon:** This area must dynamically reconfigure its input listeners and rendering logic based on the module currently selected in Area 3.

## 3. THE METABOLISM LAW (PHYSICS)
- **Wattage Supply:** The Power Core generates kW. Every active module has a `powerDraw`.
- **Functional Damage:** Damage is not a number; it is a degradation. If the "AFT" quadrant is at 20% health, the "Impulse Engine" located there only operates at 20% max thrust.
- **Core Brownout:** When power draw exceeds supply, the ship enters a "Brownout." Modules flicker, and system responsiveness drops.

## 4. ASSET & ART DIRECTION
- **Resolution:** 32x32 per module. 16-bit NES palette.
- **Rendering:** Use `Paint.isFilterBitmap = false` for sharp edges.
- **Greebling:** Visual detail is procedural. Modules look like "plates" stacked on top of mechanical "internals."
