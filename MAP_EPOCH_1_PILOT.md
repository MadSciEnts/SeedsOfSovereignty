\# MAP: EPOCH I (Tiers 1–12) - The Pilot



\## 1. Core Loop

\- \*\*Objective:\*\* Exploration and survival. Navigate the "Void" to discover procedural wrecks, Essence clusters, and outposts.

\- \*\*Combat:\*\* Twitch-based movement across vast distances. No screen-edge wrapping.



\## 2. World Geometry \& Exploration

\- \*\*Coordinate Plane:\*\* Space is a continuous Cartesian plane. Entities move freely using `float` coordinates.

\- \*\*Sensor Radius:\*\* Objects are hidden by default. The player ship has a "Sensor Radius" (Component-driven). Entities are only "discovered" (made visible/active) when within this radius.

\- \*\*Procedural Horizon:\*\* Obstacles and enemies spawn in a ring just beyond the camera's view, based on the player's movement vector.



\## 3. Technical Specs for Epoch I

\- \*\*Camera:\*\* Dynamic "Leash" follow-lock. The camera follows the player with Linear Interpolation (Lerp) to provide a sense of weight and speed.

\- \*\*World Bounds:\*\* Theoretically infinite. Use a "Floating Origin" (resetting world to 0,0) when the player exceeds 10,000 units to maintain precision.

\- \*\*Viewport:\*\* `ExtendViewport` is used to maintain aspect ratio while revealing more of the universe on wider screens.

