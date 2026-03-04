\# DNA: WEAPON LOGIC \& PROJECTILE GEN



\## 1. Kinetic Projectiles (T1+)

\- \*\*Geometry:\*\* Simple pixel-lines or small diamonds.

\- \*\*VFX:\*\* A 3-frame "Muzzle Flash" at the hardpoint and a 2-pixel "Spark" on impact.



\## 2. Beam Weapons (T7+)

\- \*\*Logic:\*\* Instant hitscan with a "Charge Up" visual.

\- \*\*Rendering:\*\* A central core line (White) with a fluctuating outer glow (Faction Color).

\- \*\*Noise:\*\* Use 1D Perlin noise to make the beam "jitter" while firing.



\## 3. Ordnance (Missiles/Torpedoes)

\- \*\*Pathing:\*\* Use "Proportional Navigation" (not just direct homing) to create realistic curves.

\- \*\*Exhaust:\*\* Procedural particle ribbon that fades from opaque to transparent over 0.5 seconds.

