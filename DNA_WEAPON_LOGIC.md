# DNA: WEAPON LOGIC & PROJECTILE GEN

## 1. Firing Mechanics
- **Staggered Start:** initial weapon cooldowns are randomized: `MathUtils.random(0f, fireRate)` to prevent synchronized group fire.
- **Alternating Fire:** Tier 1c+ configurations alternate between left and right barrels.

## 2. Projectile Tiers
- **Tier 1 (Kinetic):** Yellow streaks. Damage: 10. Speed: 1000u.
- **Tier 2 (Plasma):** Purple bolts. Damage: 25. Speed: 1000u.
- **Tier 3 (Pulse):** Gold spheres. Damage: 15. Speed: 1000u.
- **Tier 4 (Heavy Turret):** Orange heavy pulses. Damage: 40. Speed: 1000u.

## 3. Combat Logic
- **Player Damage:** Shields absorb damage first; hull health takes the remainder.
- **Collision:** Impact with drones deals 10 damage to player and destroys drone.
- **Enemy Firing:** Enemies use slightly slower projectiles (600u).
