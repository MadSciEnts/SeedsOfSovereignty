# SOVEREIGN ENGINE: PROJECT CONTEXT

## 1. THE VISION
Tactical naval space RPG. 48 tiers (4 Epochs). High inertia, "Ender's Game" HUD, 16-bit retro aesthetic.

## 2. WORLD RULES
- **SECTOR SIZE:** 20,000 x 20,000 World Units (`Double`).
- **SOFT BOUNDARIES:** "Danger Zone" at 18,000 units. Crossing applies escalating damage.
- **DIFFICULTY:** Enemy tier scales with distance from (0,0).

## 3. TECHNICAL CONSTRAINTS
- **PLATFORM:** Native Android (Kotlin) via `SurfaceView`.
- **RESOLUTION:** 640x360 internal target (16:9). Nearest Neighbor scaling.
- **ZOOM:** Camera zooms out as Tier increases, keeping the ship's screen-size relative.
- **RIBBONS:** 64-segment engine trails with birth-assigned Perlin noise.
