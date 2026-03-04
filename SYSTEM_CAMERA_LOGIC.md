\# SYSTEMS: CAMERA \& VIEWPORT LOGIC



\## 1. Orthographic Controls

\- \*\*Following:\*\* The camera must always stay centered on the "Lead Point" of the player.

\- \*\*Culling:\*\* The `RenderSystem` must use the Camera's `Frustum` to ignore entities more than 200 units off-screen to save Android CPU/Battery.



\## 2. Zoom Scaling

\- \*\*Speed Zoom:\*\* As ship velocity increases, the camera `zoom` should increase from `1.0` to `1.4`. This provides a wider field of view for high-speed maneuvering.



\## 3. Screen Effects

\- \*\*Shake:\*\* Implement a `CameraShakeSystem` that adds a random $x, y$ offset on impact, decaying over time.

\- \*\*Flash:\*\* Briefly tint the camera's color buffer during "Ascension" or heavy damage.

