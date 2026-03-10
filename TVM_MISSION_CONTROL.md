# TVM MISSION CONTROL: Phase 10 Strategic Deployment (v0.7.0)

## 1. ARCHITECTURAL BASELINE
- **Stable Version:** v0.6.5 (Behavioral Lockdown).
- **Core Status:** Unified Addressing (slot-indexed signals) and Persistence Lockdown achieved.
- **Refactor:** Load-Shedding refactor complete (specialized generators and input services).

## 2. JUNIOR AGENT: RULES OF ENGAGEMENT
1. **Rule #14 (Persistence):** DO NOT modify the `setPosition(..., Align.bottomRight)` call in `PlayScreen.kt`.
2. **Standardization:** All primary controls must be 250x250; all satellites/toggles must be 100x100.
3. **The Handover:** STOP and wait for review after every step.
4. **Modularity:** Keep files below 150 lines. Use delegation.

## 3. ROADMAP: FLEET SUPPORT PAYLOADS (Phase 10)
We are now implementing specialized tactical instruments for the Admiral Epoch.

### Phase 10.1: The Shield Management System
- **Objective:** Implement a 4-quadrant shield management instrument using the DIAMOND_GRID classification.
- **Feedback:** UI must glow based on incoming damage signals from the ECS (Unified Addressing).

### Phase 10.2: Repair Drone Directive
- **Objective:** Implement Repair Drone Directive Instrument.
- **Objective:** Target specific fleet members or auto-repair highest priority damage.

### Phase 10.3: Fleet Formation Selection
- **Objective:** Implement Fleet Formation Selection UI (Wedge, Sphere, Echelon).
- **Objective:** Integrate with `DroneFormationSystem` using the new signal architecture.

## 4. CURRENT STATUS: READY FOR 10.1
Awaiting T1 Architect directive to begin Phase 10.1 implementation.
