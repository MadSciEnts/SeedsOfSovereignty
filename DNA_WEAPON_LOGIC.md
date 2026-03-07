# DNA: WEAPON LOGIC & PROJECTILE GENESIS

## Rule 1: Tier Gating
- A ship may only find, purchase, and equip modules that match its current Evolution Tier. A Tier 1 ship cannot use a Tier 2 module.

## Rule 2: Power Progression (The "Apex-to-Proto" Rule)
- Progression follows a 3-subtier model (a, b, c).
- A new Tier at Sub-tier 1 (1 barrel) must be slightly more powerful than the previous Tier at Sub-tier 3 (3 barrels).
- Current Growth Factor: **3.2x DPS per Tier**.

## Rule 3: The Effect System (Multi-Faceted Impacts)
- Projectiles carry a `List<ProjectileEffect>`.
- Effects target specific attributes: `HULL_DAMAGE`, `SHIELD_DAMAGE`, `SPEED_MULTIPLIER`, `COOLDOWN_MULTIPLIER`.

## Rule 4: Visual Language (Color & Shape)
- **Kinetic (Yellow):** General Purpose.
- **Ion (Violet):** High Shield Disrupt.
- **Acid (Green):** Hull Erosion/Snare.
- **Piercer (Orange):** Shield Bypass.
- **Disruptor (Cyan):** System Jamming.

## Rule 5: Impact VFX Granularity
- Impact visuals are triggered per-effect (e.g., Shield Ripple + Hull Spark).

## Rule 6: Strategic Specialization
- Modules can sacrifice direct damage for powerful status effects or specialized utility.

## Rule 7: Rarity & Deviation
- `COMMON` modules are the baseline. Higher rarities (`UNCOMMON` to `LEGENDARY`) provide 10-60% DPS boosts and additional secondary effects.

## Rule 8: Drone Slot & Count Escalation
- **Tier 5:** Initial drone capability unlocked (1 Slot).
- **Slot Scaling:**
    - Tier 5-8: 1 Drone Slot.
    - Tier 9-10: 2 Drone Slots.
    - Tier 11-12: 3 Drone Slots.
    - Tier 13+: 4 Drone Slots (Max).
- **Drones per Module Scaling:**
    - T5: 1 Drone
    - T6: 2 Drones
    - T7: 4 Drones
    - T8: 6 Drones
    - T9: 8 Drones
    - T10: 10 Drones
    - T11: 12 Drones
    - T12: 14 Drones
    - T13+: 16 Drones (Max)
- **Automatic Provisioning:** Reaching Tier 5 automatically adds the first drone slot and a basic module. Subsequent slot unlocks (T9, T11, T13) automatically add an "EMPTY" slot to the ship's topology.
