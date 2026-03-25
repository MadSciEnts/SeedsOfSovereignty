# AGENT BOOTSTRAP PROTOCOL: READ BEFORE CODING

## 1. THE ARCHITECTURAL MISSION
You are developing a Pure Kotlin/Android Canvas game. No external libraries (LibGDX, Unity, etc.) are permitted. You are responsible for the raw SurfaceView life cycle and the threaded Game Loop.

## 2. IMMUTABLE FILES (READ-ONLY)
The following files are the project's "Constitution." You are strictly forbidden from modifying them:
- `PROJECT_MANIFEST.md`
- `DESIGN_SPECS.md`
- `BLUEPRINT.kt`
- `CONTEXT.md`
- `BOOTSTRAP.md`
- `TEXTURE_GUIDELINES.md`
- `COMMS_PROTOCOL.md`

## 3. PROCEDURAL DIRECTIVES
- **Coordinate Discipline:** Always use logical coordinates (0-360 vertical). Convert all touch inputs immediately.
- **Rendering Efficiency:** You MUST implement the Texture Cache (Phase 09). Redrawing complex paths every frame is a failure.
- **Input Routing:** Do not allow NAV touches to bleed into TAC zones. Check the Pointer ID and X-coordinate against the `InputRouter` zones.

## 4. PIXEL ART INTEGRITY
- Point Filtering (Nearest Neighbor) is mandatory.
- Ensure that the `logicalW` fill logic does not result in "wide" or "stretched" pixels. All pixels must be perfectly square.
