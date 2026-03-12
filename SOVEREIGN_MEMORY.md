# SOVEREIGN MEMORY: THE SAVE STATE (v1.2.0)

## 1. CURRENT VECTOR
- **Epoch:** Pilot Epoch.
- **Mission:** Phase 23.2 - Torpedoes & Command Matrix.
- **Immediate Goal:** Finalize visual/physics baseline and implement homing logic.

## 2. STRATEGIC DECISION LOG (THE "WHY")
- **GSF 0.1 Standard:** Adopted to solve floating-point jitter at vast distances. 1.0u = 100px.
- **The Husk Purge:** **Decision: Permanent.** Persistent post-death smoke caused race conditions in the CCD loop.
- **Active-Only Wakes:** Trails now exist only while the entity is alive. This provides high-fidelity feedback without registry clutter.
- **World-Pinned Noise:** Switched from index-based noise to World-Coordinate-Pinned noise to stop the "Coiled Spring" visual bug.
- **Segment-Circle CCD:** Adopted in v3.2.1 to replace multi-point sampling. It is the definitive standard for preventing "Photon Tunneling."

## 3. PHYSICAL STANDARDS (v3.2.1)
- **Player Trail:** 24-32 segments. Width = `hullSize * 0.8f`.
- **Enemy Trail:** 24-32 segments. Width = `hullSize * 0.4f`.
- **Projectile Trail:** 16 segments. Width = `mass * 0.3f`.
- **Mass-Based Knockback:** Impulse applied using `(ProjectileMass * 1.5f)`.

## 4. ADVISORY BRIEFING FOR T1 AGENTS
"Welcome, Architect. You are leading a 0.1 scale precision engine. Logic must be Sole-Source (TacticalStatsComponent). Physics must be CCD-hardened (Segment-Circle). Visuals must be pinned to World-Space (No relative-index noise). Your current surgical focus is Torpedo Steering logic and Command Matrix integration."
