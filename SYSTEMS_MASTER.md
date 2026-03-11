# SEED OF SOVEREIGNTY: MASTER SYSTEMS ARCHITECTURE (v1.3.0)

## 1. THE PARTITION PROTOCOL (v1.3.0)
### 1.1 Physical Layer (The Ship Partition)
- **Component:** `shipViewport` & `shipCamera`.
- **Units:** 1280x720 world units.
- **Mandate:** All physical hulls (Ships, Stations, Bullets) MUST render here.
- **Scaling:** MUST be explicitly updated in `resize()` alongside the UI viewport to prevent "Stretched World" (ovular ship) bugs.

### 1.2 Diegetic Layer (The AR Partition)
- **Component:** `uiViewport`.
- **Units:** Adaptive screen pixels (Bottom-Left Origin).
- **Role:** Interactive AR HUD, tethers, and numerical telemetry.
- **Projection Bridge:** World-to-UI mapping follows the `(uiWidth/2) + (worldDiff / zoom)` logic.

## 2. SHAPEDRAWER STATE MANAGEMENT
- **Mandate:** `drawer.update()` MUST be called immediately after every `batch.use(matrix)` to synchronize the drawer with the current viewport partition.
- **Safety Reset:** `drawer.setColor(Color.WHITE)` MUST be called at the end of every system pass to prevent alpha-leaks between partitions.

## 3. ECS EXECUTION ORDER (PHYSICAL DRAW)
1. **ClearScreenSystem**: Wipes the OpenGL buffer.
2. **StarfieldSystem**: Renders the parallax void.
3. **EntityRenderSystem**: Draws ship hulls (Ship Viewport).
4. **RenderSystem**: Draws bullets/VFX (Ship Viewport).
5. **DiegeticHUDSystem**: Draws AR HUD (UI Viewport).
6. **ApertureSystem**: Full-screen cinematic overlays.

## 4. STATION ECONOMY
- **Swap or Scrap:** Standardized logic for direct module replacement.
- **Hangar UI:** 3-state flow (Welcome -> Category -> Market). MUST be hidden during flight.
