# SOVEREIGN PROJECT MANIFEST: VERSION 4.0 (TACTICAL OS)

## STAGE 1: VIRTUAL TOPOLOGY & HARDWARE (PHASES 01-04)
- [ ] **01: Viewport Manager:** Implement `ViewportManager.kt`. Logical Height = 360f. `logicalW` must be calculated as `(PhysicalWidth / PhysicalHeight) * 360`. NO PILLARBOXING.
- [ ] **02: SovereignView & GameLoop:** Create a custom `SurfaceView`. Implement a high-precision `Thread` using `System.nanoTime()`. Target 60 FPS with a fixed delta-time step.
- [ ] **03: Input Router & Region Delegation:** Implement `InputRouter.kt`. Divide `logicalW` into:
    - NAV ZONE (Left 30%): Reserved for Impulse/Warp controls.
    - WORLD ZONE (Center 40%): Reserved for ship interaction/targeting.
    - TAC ZONE (Right 30%): Reserved for the Command Matrix.
- [ ] **04: Pointer ID Synchronization:** Multi-touch support. Ensure a thumb on the NAV joystick does not interfere with a finger on the TAC touchpad.

## STAGE 2: THE COMMAND MATRIX (PHASES 05-08)
- [ ] **05: Registry Parser:** Implement GSON/Kotlin-Serialization for `REGISTRY.json`. Support Tiers 1-20.
- [ ] **06: 3-Zone Matrix UI:** Implement the vertical Right-Hand stack:
    - AREA 1 (Bottom): Category Filters (WEA, SHI, TAC, ENG).
    - AREA 2 (Center): Chameleon Control Canvas.
    - AREA 3 (Top): Scrollable Module Shelf.
- [ ] **07: Chameleon Input Logic:** Swap Area 2 between Analog Joystick (Weapons), Persistent Touchpad (Drones/Shields), and Sliders (Engines/Power).
- [ ] **08: Tactical State Persistence:** Every module instance must store its `tacticalPosition: PointF`. When re-selecting a module, the Area 2 handle must "snap" to its last saved position.

## STAGE 3: VISUAL GRAMMAR & CACHING (PHASES 09-12)
- [ ] **09: Texture Cache Engine:** Implement `TextureCache.kt`. Modules are rendered once to an off-screen `Bitmap` and reused. Do not draw raw paths to the screen canvas every frame.
- [ ] **10: Procedural Greeble Engine:** Logic to generate 32x32 16-bit hardware sprites. Must use symmetrical plating, mechanical "base" layers, and neon "glow" layers.
- [ ] **11: Nautilus HUD:** Render 4 arcs centered on the ship. Foreground Arc = Hull. Background Arc = Shields. Must rotate in local space via `Canvas.rotate(shipAngle)`.
- [ ] **12: Neon Shader Effects:** Implement `ColorFilter` and `Glow` effects for active modules and critical health warnings.

## STAGE 4: COMBAT METABOLISM (PHASES 13-16)
- [ ] **13: Power Core Metabolism:** Total Watts ($kW$) = `BasekW * (CoreHealth / 100)`.
- [ ] **14: Quadrant Damage Mapping:** Implement `hullHealth[4]` array (FORE, AFT, PORT, STAR). Damage to a quadrant scales the efficiency of modules located there.
- [ ] **15: Brownout Feedback:** If `PowerDraw > CoreOutput`, implement UI flickering, input "stuttering," and reduced engine thrust.
- [ ] **16: Dynamic Module Scaling:** Module performance (Cooldown, Speed, Fire Rate) is a function of `min(PowerAllocated, CoreHealth, QuadrantHealth)`.

## STAGE 5: SYSTEM LOOPS & PERSISTENCE (PHASES 17-20)
- [ ] **17: Projected Orbits:** Calculate elliptical paths for Drones/Fighters using the `tacticalPosition` persistent XY as the "Focus" of the orbit.
- [ ] **18: Mirror AI Capabilities:** NPC ships must use the same `IModule` and `PowerCore` logic as the player. No "Cheating" AI.
- [ ] **19: Ascension & Evolution:** Logic for "Tier-Up." Replaces the ship chassis and wipes current modules for a "Standard" set of the higher Tier.
- [ ] **20: Ledger Serialization:** Save/Load the entire `ShipState` to/from JSON, including individual module persistent coordinates.
