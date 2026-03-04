\# RULES OF ENGAGEMENT: Coding Standards



\## 1. Kotlin \& LibKTX

\- \*\*Idiomatic Code:\*\* Use `apply`, `let`, `with`, and extension functions.

\- \*\*UI Builders:\*\* Use the `scene2d { ... }` DSL for all HUD and Menu construction.

\- \*\*Type Safety:\*\* Use KTX `AssetStorage` or `AssetManager` with Kotlin-friendly wrappers.



\## 2. Fleks ECS (Entity Component System)

\- \*\*Logic Separation:\*\* All logic must live in `IteratingSystem` or `IntervalSystem`.

\- \*\*Components:\*\* Components must be pure Data Classes (POJOs). No logic in components.

\- \*\*Injectables:\*\* Use Fleks' native dependency injection to pass `SpriteBatch`, `ShapeDrawer`, and `Camera`.



\## 3. Performance (Android Focused)

\- \*\*Zero Allocations:\*\* No `new` objects or collection resizing inside `onTick` or `render`. Use `Pools` for bullets/particles.

\- \*\*Batching:\*\* Keep `SpriteBatch` calls grouped. Use `ShapeDrawer` to draw shapes inside the same batch as sprites.

\- \*\*Drawables:\*\* All textures are procedural. Generate `Pixmap` at runtime, convert to `TextureRegion`, and cache.



\## 4. Design Ethics

\- \*\*The "No-Inventory" Rule:\*\* All weapon/ship changes are immediate "Swap or Scrap."

\- \*\*Visual Response:\*\* Every UI tap must trigger a visual tween (Glitch, Flare, or Aperture shift).

