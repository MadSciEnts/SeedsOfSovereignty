\# UI: MENU FLOW \& STATE MACHINE



\## 1. Boot Sequence

\- \*\*Splash:\*\* Logo fade-in with a procedural "Glitch" effect.

\- \*\*Sync:\*\* Checking for saved `meta.json`.

\- \*\*Main Menu:\*\* A Tier 1 ship floating in a parallax starfield. Options: \[Continue], \[New Cycle], \[Ancient Tech], \[Settings].



\## 2. Transition Logic

\- \*\*Loading:\*\* Use an asynchronous `AssetStorage` (LibKTX). Display a "System Booting" progress bar.

\- \*\*Game to Menu:\*\* Pause game state, blur background, overlay "Transmission Interrupted" UI.

