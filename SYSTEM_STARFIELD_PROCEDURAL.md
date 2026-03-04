\# SYSTEMS: STARFIELD \& VOID RENDERING



\## 1. The Infinite Parallax Concept

To create depth in an infinite world, we use 4 layers of "Space Dust" and "Distant Stars."

\- \*\*Layer 0 (Nebula/Deep Space):\*\* Move at 5% of player speed. Uses a Simplex Noise shader.

\- \*\*Layer 1 (Distant Stars):\*\* Move at 10% of player speed. Small, dim pixels.

\- \*\*Layer 2 (Mid Stars):\*\* Move at 25% of player speed. Standard brightness.

\- \*\*Layer 3 (Near Debris):\*\* Move at 50% of player speed. Large, fast "Space Snow."



\## 2. Implementation: Texture Wrapping

\- \*\*TextureWrap:\*\* Set the star textures to `TextureWrap.Repeat`.

\- \*\*Logic:\*\* Instead of moving the stars, we shift the `u` and `v` texture coordinates of a full-screen quad based on the `Camera` position. 

\- \*\*Code Directive:\*\* `batch.draw(region, x, y, width, height)` where the region's internal UVs are offset by `(camera.position \* parallaxFactor)`.



\## 3. Visuals

\- \*\*Color Sync:\*\* Star colors should shift based on the current sector's "Faction Palette" (from `DNA\_VISUAL\_PALETTES.md`).

