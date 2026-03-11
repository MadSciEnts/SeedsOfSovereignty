# RULES OF ENGAGEMENT: Coding Standards (v1.0.0)

## 1. Kotlin & Diegetic Architecture
- **Idiomatic Code:** Use `apply`, `let`, `with`, and extension functions.
- **Diegetic HUD:** Use the `HUDElementComponent` and `DiegeticHUDSystem` for all UI and AR elements. Standard Scene2D is reserved only for high-level static menus (Welcome/Hangar) and MUST be hidden during flight.

## 2. Performance (Android Focused)
- **Zero Allocations:** No `new` objects inside `onTick` or `render`. Use cached `Vector2`/`Vector3` and `Pools`.
- **Batching:** Keep `SpriteBatch` and `ShapeDrawer` calls grouped by Z-index. All textures must be procedural or pre-loaded.

## 3. The Mantra of Modularization
- **No "God Scripts":** No file should exceed 300 lines of logic.
- **Composition over Inheritance:** HUD visuals are handled via `HUDRendererComponent` functional pointers, not class extension.

## 4. Agent-in-Charge Review Protocol (v0.3.13)
To ensure architectural integrity, the Lead Architect must verify all work using this 4-step forensic process:
1. **Forensic Reading:** Verify `TVM_WORKER_LOG.md` for explicit change logs.
2. **Physical Evidence Audit:** Read ALL edited files. Confirm code matches the report.
3. **Synthesis & Diagnosis:** Formulate next steps based on physical disk state.
4. **Review Report:** Present findings to the User.

## 5. Procedural Stability (The One-File Mandate)
- **One File at a Time:** Do not modify multiple files in a single turn. Verify every write with a subsequent READ.
- **assemble check:** Execute `gradle core:assemble` before signing off a phase.
- **Reality-Anchor:** Re-anchor beliefs to the physical disk state after every session timeout.
