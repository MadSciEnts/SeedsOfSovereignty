# HUD ARCHITECTURE: THE DIEGETIC ORCHESTRATOR (v1.0.0)

## 1. Goal
To provide a high-performance, zero-allocation HUD system specialized for Android. It replaces standard LibGDX Table layouts with a component-based AR (Augmented Reality) engine that bridges World and Screen space.

## 2. Core Principles
- **Zero-Allocation:** No new objects created during the render loop.
- **Space-Agnostic:** Elements exist in SCREEN or WORLD space with seamless inter-space tethers.
- **Pivot-Centric:** All transformations (Scale, Rotation, Growth) are relative to a local normalized pivot.
- **Compositional Rendering:** Drawing logic is decoupled from positioning via HUDRendererComponent.

## 3. The Anchor Matrix (Screen Space)
Standard 9-point screen alignment with cached world-space coordinates updated on resize.

## 4. Components
### HUDElementComponent
- `space`: (SCREEN, WORLD)
- `anchor`: (Enum)
- `parentId`: (Entity?) - For hierarchical stacking and inherited transforms.
- `offsetX`, `offsetY`: (Float) - Static alignment.
- `localX`, `localY`: (Float) - Dynamic animation/scroll offsets.
- `width`, `height`, `alpha`, `rotation`, `pivotX`, `pivotY`, `scaleX`, `scaleY`, `zIndex`

### HUDRendererComponent
- `renderType`: (BLUEPRINT, ARC, ICON, TEXT, TETHER, PANEL_BG)
- `localizationKey`: (String?) - For localized text strings.
- `styleId`: (String?) - Maps to "INDUSTRIAL", "IMPERIAL", etc.
- `blendingMode`: (Enum: NORMAL, ADDITIVE)
- `shaderId`: (String?) - For glitch/hologram effects.
- `isVisible`: (Boolean)

### HUDInteractionComponent
- `isClickable`, `isScrollable`: (Boolean)
- `gestures`: (Bitmask: TAP, LONG_PRESS, DOUBLE_TAP)
- `actionKey`: (String)

### HUDLinkComponent
- `sourceEntity`, `targetEntity`: (Entity) - Bridges spaces (e.g., World Ship to Screen Diagnostic).
- `lineStyle`: (SOLID, DASHED, PULSING)

### HUDAnimationComponent
- `animationId`: (String) - Maps to a stateless functional logic object.
- `elapsedTime`, `intensity`, `isLooping`

## 5. View Management (HUD Layers)
Elements define a `HUDLayer` (Enum: FLIGHT, MARKET, MAP, SYSTEM, VFX).
- Systems can enable/disable entire layers (e.g., `HUDSystem.hideLayer(FLIGHT)`).

## 6. The HUDSystem Pipeline (Fleks)
1. **Cleanup Pass:** Removes entities with dead `parentId` or `source/target` references.
2. **Logic Pass:** Processes `HUDAnimationComponent` and calculates absolute coordinates.
3. **Log/Scroll Pass:** Applies stacking gravity and Scissor clipping.
4. **Input Pass:** Processes touch events from **Highest Z-Index to Lowest**.
5. **Render Pass:** 
    - Pass 1: WORLD Space (Sorted by Z).
    - Pass 2: SCREEN Space Panels.
    - Pass 3: SCISSOR Content.
    - Pass 4: TEXT & OVERLAYS (Batched by Font).
    - Pass 5: TETHERS (Projected Inter-space lines).
