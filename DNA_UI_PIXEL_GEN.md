\# DNA: UI PIXEL GENERATION



\## 1. The "Aperture" Frame

\- \*\*Corners:\*\* 45-degree chamfered edges.

\- \*\*Borders:\*\* 2-pixel thick lines with 1-pixel "Gap" segments to imply data transmission.

\- \*\*Background:\*\* 40% opacity dark grey with a subtle "Hex" pattern shader.



\## 2. Scaling Logic

\- \*\*Mobile First:\*\* All touch targets must be at least 44x44dp.

\- \*\*Dynamic Padding:\*\* Use $ScreenHeight \\times 0.02$ for consistent spacing regardless of resolution.



\## 3. Feedback Loops

\- \*\*Hover/Tap:\*\* Frame thickness increases by 1px; color shifts to "Accent" (White/Cyan).

\- \*\*Glitches:\*\* On menu transition, randomly offset frame segments for 0.1s.

