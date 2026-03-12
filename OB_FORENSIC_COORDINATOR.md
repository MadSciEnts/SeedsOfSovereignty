# MISSION COORDINATOR PROTOCOL (v0.9.1)

## 1. THE PRESERVATION MANDATE (CRITICAL)
- **DO NOT ALTER ARCHITECTURAL ANNOTATIONS.** 

---

# CURRENT MISSION: Phase 23.2 - Projectile Genesis & Material Physics (v3.0.0)
**Status:** [ 0 / 3 : INITIALIZING ] - Consensus Reached at High Table.

## STEP 1: IMPLEMENT MASS-BASED FIRING
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/CombatSystems.kt`
- Update `WeaponSystem` to assign `mass` and `velocity` based on `ProjectileType`:
    - KINETIC: [1.0, 1.0], ION: [2.0, 0.5], PHOTON: [5.0, 0.01], PLASMA: [0.4, 4.0].
- Ensure `ProjectileComponent` is correctly initialized with the `type` from the module.

## STEP 2: IMPLEMENT KNOCKBACK & CCD
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/CombatSystems.kt`
- In `resolveHit`, apply velocity impulse to targets: `target.vel += (proj.vel.nor() * (proj.mass * 2.0f))`.
- Validate the 2-step Sub-Frame collision scan to prevent "Photon Tunneling."

## STEP 3: INITIALIZE RIBBON PIPELINE
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/system/CombatSystems.kt`
- Initialize `history` buffer in projectiles for future Ribbon Trail rendering.
- Apply `LifeSpan` variants: PHOTON (0.2s), KINETIC (1.5s), PLASMA (4.0s).
