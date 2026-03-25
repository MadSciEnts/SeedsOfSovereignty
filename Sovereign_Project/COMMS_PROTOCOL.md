# SOVEREIGN COMMS PROTOCOL: UI-TO-ENGINE SYNC

## 1. INPUT MESSAGING
- **NAV Matrix:** Sends `Vector2(thrust, rotation)` to the Engine.
- **TAC Matrix (Touchpad):** Updates the `tacticalPosition` on the specific `IModule` instance. The Engine reads this XY to project the Drone Orbit in the World View.
- **METABOLISM Matrix (Sliders):** Updates the `ShipIntegrity.categoryPower` map.

## 2. STATE FEEDBACK
- **Frame Sync:** The Engine pushes the `ShipIntegrity` object to the HUD and Matrix components at the start of every draw cycle.
- **Brownout Trigger:** If total `powerDraw` > `coreOutput`, the UI enters "Degraded Mode" (flicker shaders active, input lag simulated).

## 3. PERSISTENCE TRIGGER
- When a module is swapped in the Matrix, the current `tacticalPosition` must be immediately written to the module's persistence handle in the `ShipState`.
