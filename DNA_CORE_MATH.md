# DNA: CORE MATH & PROCEDURAL LOGIC

## 1. Floating Origin Protocol
- **The Jitter Problem:** Floating point precision fails at large distances from `(0,0)`.
- **The Snap:** If `abs(player.position) > 10,000`, the `OriginSystem` snaps all entities back: `entity.pos -= player.pos`. The player is reset to `(0,0)`. 
- **Verification:** Verified implementation in `MovementSystems.kt`.

## 2. Camera Leash (Lerp)
- **Formula:** $Cam_{next} = Cam_{current} + (Target - Cam_{current}) \times speed \times delta$
- **Lead Offset:** The camera target is not the player, but `player.pos + (player.velocity * 0.5s)`. This allows the player to see "ahead" in the direction of travel.
- **Dynamic Zoom:** Camera zoom increases with velocity (1.0 to 1.4) to provide a wider FOV during high-speed combat.

## 3. Station Logic
- **Safety Aura:** Stations provide a 600-unit "Disengage Zone." Enemies entering this radius switch to `RETREATING` state.
- **Docking Lock:** ship physically anchors to the station core when within 100 units.
