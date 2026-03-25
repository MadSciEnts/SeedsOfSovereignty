# TEXTURE GUIDELINES: 16-BIT HARDWARE AESTHETIC

## 1. SPRITE ARCHITECTURE
- **Standard Size:** 32x32 pixels.
- **Internal Grid:** Use an 8x8 "Sub-grid" for mechanical detail placement.
- **Symmetry:** All ship components must be bilaterally symmetrical.

## 2. THE 3-LAYER STACK
1. **BASE LAYER (Bottom):** Dark steel/charcoal grey. Represents the structural frame.
2. **PLATING LAYER (Middle):** Light grey/Paneling. Use 1-pixel "Gap" lines to show depth.
3. **GLOW LAYER (Top):** Neon Cyan (Tech) or Neon Red (Weapons). Represents active power/circuitry.

## 3. RENDERING RULES
- NEVER use gradients. Use "Dithering" (checkered pixels) for shading.
- Use `android.graphics.Bitmap.Config.ARGB_8888` for transparency.
- Mandatory: Point Filtering (`Paint.isFilterBitmap = false`).
