\# DNA: CORE MATH \& PROCEDURAL LOGIC



\## 1. The Seed Protocol

\- \*\*Global Seed:\*\* A single Long integer determines the entire galaxy's layout.

\- \*\*Local Entropy:\*\* Every entity (Ship, Station, Star) derives its "Visual DNA" from its unique ID + the Global Seed.

\- \*\*Consistency:\*\* Re-generating the same ID must always produce the same visual result.



\## 2. Symmetry \& Hull Generation

\- \*\*Reflective Symmetry:\*\* Ships must be generated on a central axis (X or Y).

\- \*\*Recursive Subdivision:\*\* Start with a primitive (Rectangle/Triangle), then "grow" detail by subdividing edges based on a noise function.

\- \*\*The "Greeble" Factor:\*\* Add small, non-functional rectangular "bits" to surfaces to imply mechanical complexity.



\## 3. Movement Physics ($f(t)$)

\- \*\*Thruster Sway:\*\* Use Sine waves for engine flicker: $Intensity = \\sin(time \\times frequency) \\times amplitude$.

\- \*\*Inertia Scaling:\*\* $Velocity\_{new} = Velocity\_{old} + (Thrust \\times Delta) - (Drag \\times Velocity\_{old})$.

