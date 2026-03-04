\# SYSTEMS: MINIMAP \& SENSORS (Exploration)



\## 1. The Sensor Component

\- \*\*SensorRadius:\*\* Every ship entity has a `SensorRadiusComponent`. 

\- \*\*Active Discovery:\*\* Entities (enemies, wrecks, stations) remain "Inert" (hidden and logic-paused) until they enter the Player's `SensorRadius`.

\- \*\*Memory:\*\* Once a static object (like a Station or Wreck) is discovered, its icon remains on the Minimap even if the player leaves the radius.



\## 2. Minimap Rendering

\- \*\*Style:\*\* A circular "Radar" overlay in the top-right or bottom-right of the HUD.

\- \*\*Orientation:\*\* "North-Up" or "Forward-Up" (User toggleable). 

\- \*\*Iconography:\*\*

&nbsp;   - \*\*Hostile:\*\* Red flickering dot.

&nbsp;   - \*\*Neutral/Scrap:\*\* Grey diamond.

&nbsp;   - \*\*POI (Station/Gate):\*\* Cyan square.

&nbsp;   - \*\*Player:\*\* White chevron indicating heading.



\## 3. Off-Screen Indicators (Edge-Tracking)

\- If a high-priority target (e.g., an Elite Enemy or the Epoch Zenith Gate) is outside the viewport but inside the Sensor Radius, draw a small arrow at the edge of the screen pointing toward it.

\- \*\*Distance Scaling:\*\* The arrow's size or transparency should indicate how far away the target is.



\## 4. Technical Implementation (Fleks)

\- \*\*SensorSystem:\*\* An `IteratingSystem` that checks the distance between the Player and all `Discoverable` components.

\- \*\*Optimization:\*\* Use a Spatial Hash or Grid-based partitioning so the system doesn't check 1,000 entities every frame. Only check entities in the current and neighboring "World Chunks."

