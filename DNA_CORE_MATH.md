\# DNA: CORE MATH \& PROCEDURAL LOGIC



\## 1. Floating Origin Protocol

\- \*\*The Jitter Problem:\*\* Floating point precision fails at large distances from `(0,0)`.

\- \*\*The Snap:\*\* If `abs(player.position) > 10,000`, the `OriginSystem` snaps all entities back: `entity.pos -= player.pos`. The player is reset to `(0,0)`, and a global `worldOffset` counter is incremented.



\## 2. Camera Leash (Lerp)

\- \*\*Formula:\*\* $Cam\_{next} = Cam\_{current} + (Target - Cam\_{current}) \\times speed \\times delta$

\- \*\*Lead Offset:\*\* The camera target is not the player, but `player.pos + (player.velocity \* 0.5s)`. This allows the player to see "ahead" in the direction of travel.



\## 3. Procedural Distribution

\- Use a \*\*Coordinate-Based Hash\*\*: `Seed(x, y)` generates the same asteroid field every time the player returns to the same coordinates, without needing to save every rock in a database.

