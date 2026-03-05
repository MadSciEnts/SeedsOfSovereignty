# RULES OF ENGAGEMENT: Coding Standards

## 1. Kotlin & LibKTX
- **Idiomatic Code:** Use `apply`, `let`, `with`, and extension functions.
- **UI Builders:** Use the `scene2d { ... }` DSL for all HUD and Menu construction.
- **Type Safety:** Use KTX `AssetStorage` or `AssetManager` with Kotlin-friendly wrappers.

## 2. Fleks ECS (Entity Component System)
- **Logic Separation:** All logic must live in `IteratingSystem` or `IntervalSystem`.
- **Components:** Components must be pure Data Classes (POJOs). No logic in components.
- **Injectables:** Use Fleks' native dependency injection to pass `SpriteBatch`, `ShapeDrawer`, and `Camera`.

## 3. Performance (Android Focused)
- **Zero Allocations:** No `new` objects or collection resizing inside `onTick` or `render`. Use `Pools` for bullets/particles.
- **Batching:** Keep `SpriteBatch` calls grouped. Use `ShapeDrawer` to draw shapes inside the same batch as sprites.
- **Drawables:** All textures are procedural. Generate `Pixmap` at runtime, convert to `TextureRegion`, and cache.

## 4. The Mantra of Modularization
- **No "God Scripts":** No file should exceed 300 lines of logic. If a system or component set grows too large, it must be split into a specific domain (e.g., `CombatComponents.kt`, `MovementSystems.kt`).
- **Refactoring Sprints:** Every 4 Tiers (once per major evolution), we perform a "Refactoring Sprint" to evaluate file size and reorganize logic.
- **Feature-First Structure:** Organize files by their game function rather than just their technical type.

## 5. Log-Based Verification & Synchronization
- **Verification Protocol:** Before claiming a task is complete, execute a `gradle core:assemble` check to ensure the project compiles.
- **Status Sync:** In every multi-file update, explicitly list the "Disk Status" to confirm which files were successfully written.
- **Log-First Workflow:** Update `TASK_SYNC.log` *before* modifying logic files to maintain a clear "source of truth" for complex refactors.

## 6. Design Ethics
- **The "No-Inventory" Rule:** All weapon/ship changes are immediate "Swap or Scrap."
- **Visual Response:** Every UI tap must trigger a visual tween (Glitch, Flare, or Aperture shift).
