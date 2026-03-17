# ARCHITECTURE MASTER: The 48-Tier Evolution

## 1. The Progression Curve
The game is divided into 4 Epochs. Every 12 Tiers, the perspective and primary gameplay loop fundamentally shift.

| Epoch | Tiers | Title | Perspective | Genre |
| :--- | :--- | :--- | :--- | :--- |
| **I** | 1–12 | The Pilot | Single Ship | Arcade/Action |
| **II** | 13–24 | The Admiral | Small Fleet | Tactical Combat |
| **III** | 25–36 | The Overlord | Space Station | Base Management/Defense |
| **IV** | 37–48 | The Sovereign | Galactic Hive | Grand Strategy/4X |

## 2. Scaling & Evolution Formula (The "Linear Swarm")
To maintain tactical visibility across all epochs (Ender's Game aesthetic), ship scale follows a linear-magnitude model with efficiency tapering.

### 2.1 The Math
- **Epoch Index (e):** `(GlobalTier - 1) / 12`
- **Internal Tier (t):** `((GlobalTier - 1) % 12) + 1`
- **Magnitude (M):** `1.0 + (e * 2.0)` (Sequence: 1.0, 3.0, 5.0, 7.0)
- **Base Scale:** `M * (1.0 + (t-1)*0.1 - max(0, t-10)*0.2)`
- **Sub-Tier Bonus:** `SubTierOrdinal * 0.02 * M`

### 2.2 Visual Narrative
- **Growth (T1-T10):** Ships expand linearly (+0.1/tier) as systems are added.
- **Elite Taper (T11-T12):** Ships reduce in size (-0.1 net/tier) representing technological refinement and miniaturization before an Epoch jump.
- **Epoch Jump:** Magnitude increases by +2.0, providing a significant but readable jump in the "Capital Ship" hierarchy.

## 3. Transition Mechanics (The Genre Shift)
- **Ascension:** Reaching Tier 12, 24, or 36 triggers a "Narrative Zenith" mission.
- **Conversion:** Upon completion, the player's current "Avatar" becomes the foundation for the next Epoch (e.g., your T24 Fleet flagship becomes your T25 Station Core).
- **Scale:** The camera zooms out significantly at each transition, turning previous high-tier threats into common low-tier obstacles.

## 4. The Power Baseline
- **Tier 1:** A fragile interceptor with a single kinetic pea-shooter.
- **Tier 48:** A solar-system-sized intelligence that manipulates star-states.
