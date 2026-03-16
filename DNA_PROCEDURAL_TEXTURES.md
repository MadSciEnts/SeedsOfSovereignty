# DNA: PROCEDURAL TEXTURE GENERATION

This document defines the rules for runtime asset generation to support all 48 tiers without static assets.

## 1. UI & HUD (Aperture Style)
*   **Ninepatch Generation:** Create frames with 45-degree chamfered corners using `Pixmap.drawLine`.
*   **Faction Styles:** 
    *   *Player:* Cyan/White crisp lines.
    *   *Hostile:* Red/Amber jagged or "Glitchy" lines.
    *   *Neutral:* Green/Grey industrial blocks.

## 2. Ship & Module Generation
*   **Chassis (The Seed):** 
    *   Use a `Seed` value to plot pixels on a grid (e.g., 32x32).
    *   **Symmetry:** Mirror the X-axis to ensure ship-like structure.
    *   **Tier Scaling:** Grid size increases with Tier (T1: 16x16, T12: 64x64, T48: 512x512).
*   **Modules:** Small sub-grids (8x8) plotted onto specific "Hardpoint Pixels" on the chassis.

## 3. Projectiles & FX
*   **Kinetic:** 1x4 pixel streaks with a 1-pixel "Head" glow.
*   **Energy:** Concentric circles with alpha-fading edges.
*   **Motion Trails:** Ribbon-based (ECS handled), but the "Head" of the trail uses a procedural 4x4 glow texture.

## 4. Starfield & Background
*   **Shader Driven:** Use a fragment shader to generate 4 layers of parallax stars using 2D Noise.
*   **Nebula:** Low-frequency Simplex Noise colored by the sector's palette.

## 5. Implementation (TextureService)
*   **Registry:** A `Map<String, TextureRegion>` to cache generated assets.
*   **Generation Order:** 
    1. Generate Pixmap.
    2. Apply Palette Swapping.
    3. Upload to Texture.
    4. Dispose Pixmap.
