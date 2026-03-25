# SOVEREIGN PROMPT STACK: EXECUTION KEYS

## PROMPT 01: INITIALIZATION & TOPOLOGY
"Read all files in the `Sovereign_Project` directory. You are acting as the Phase 1 Lead Architect.
Implement **Phase 01 and 02**:
1. Create `ViewportManager.kt` using the 360-unit Vertical Anchor math.
2. Create `SovereignView.kt` (SurfaceView) with a 60FPS threaded Game Loop.
3. Render four 32x32 pixel squares at the logical corners: (0,0), (logicalW-32, 0), (0, 328), (logicalW-32, 328).
   Confirm the `logicalW` value and verify 1:1 pixel integrity with a screenshot or console log of coordinates."

## PROMPT 02: INPUT ROUTING
"With the Viewport verified, implement **Phase 03**: `InputRouter.kt`. Assign Pointer IDs to the NAV, WORLD, and TAC zones. Print console logs when each zone is touched to prove independent multi-touch delegation."
