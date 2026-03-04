\# UI LAYOUT: ADAPTIVE MOBILE LOGIC



\## 1. Resolution Independence

\- \*\*The Virtual Grid:\*\* Map the screen to a 1000x1000 unit coordinate system regardless of physical pixels.

\- \*\*Scaling:\*\* Use `ExtendViewport` in LibGDX to maintain the aspect ratio without "black bars" by showing more of the starfield on wider screens.



\## 2. Portrait vs. Landscape

\- \*\*Portrait (One-Handed):\*\* HUD elements move to the bottom 30% of the screen. Navigation is thumb-driven.

\- \*\*Landscape (Two-Handed):\*\* HUD splits to the far left and far right "Safe Zones." The center remains clear for flight.



\## 3. Safe Zones

\- \*\*Notch Handling:\*\* Use `Gdx.graphics.getSafeInset...` to ensure UI buttons never sit behind a camera notch or rounded corner.

