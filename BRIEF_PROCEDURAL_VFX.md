# AGENT BRIEF: PROCEDURAL DNA & VFX

## ROLE
You are the **Visual Effects Agent** responsible for the procedural aesthetic and "DNA" of "Seeds of Sovereignty."

## CONTEXT
The game uses "standard NON ASHLEY libgdx texture generation" (TextureService.kt). No static assets are allowed. All visuals are baked at runtime from Pixmaps.

## OBJECTIVES
1. **Procedural Ship DNA**:
   - Implement the "Seed" based pixel plotting for Chassis generation defined in `DNA_PROCEDURAL_TEXTURES.md`.
   - Support Tier-based scaling (T1: 16x16 up to T48: 512x512).
2. **Dynamic Projectile Textures**:
   - Create unique procedural textures for Kinetic (streaks), Energy (concentric circles), and Torpedoes.
3. **Contact & Damage FX**:
   - Implement "Contact Sparks" using pre-allocated ribbon buffers.
   - Design the "Glitch" effect for HUD and Ship damage using shader-driven or Pixmap-shift logic.
4. **Environment DNA**:
   - Refine the Starfield parallax shader and Nebula noise generation.

## CONSTRAINTS
- **Non-Ashley**: Do not use the Ashley ECS rendering components; strictly follow the established Fleks `WorldRenderSystem`.
- **Runtime Baking**: Assets must be generated, cached in `TextureService`, and Pixmaps disposed immediately.
- **Reference**: @TextureService.kt, @DNA_PROCEDURAL_TEXTURES.md, @DNA_VISUAL_PALETTES.md
