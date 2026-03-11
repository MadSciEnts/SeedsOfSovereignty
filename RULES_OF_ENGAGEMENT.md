# RULES OF ENGAGEMENT: Coding Standards (v1.1.0)

## 1. The GOLDEN RULE of Physical Sync (v1.1.0)
- **MANDATORY FOR ALL AGENTS:** Every file edit MUST follow this exact 4-step sequence:
    1. **Physical READ:** Read the target file to ground beliefs in current disk reality.
    2. **Physical WRITE:** Execute the targeted surgical edit.
    3. **Physical VERIFY:** Perform an immediate subsequent READ of the same file.
    4. **Physical AUDIT:** State verification success or provide intention to re-attempt if the write failed.
- **PURPOSE:** Eradicates "Ghost Edits" and ensures physical disk state matches agent contextual beliefs.

## 2. Kotlin & Diegetic Architecture
- **Idiomatic Code:** Use `apply`, `let`, `with`, and extension functions.
- **Diegetic HUD:** Use the `HUDElementComponent` and `DiegeticHUDSystem` for all UI and AR elements. Standard Scene2D is reserved only for high-level static menus.

## 3. Performance (Android Focused)
- **Zero Allocations:** No `new` objects inside `onTick` or `render`. Use cached `Vector2`/`Vector3` and `Pools`.
- **Batching:** Keep `SpriteBatch` and `ShapeDrawer` calls grouped by Z-index.

## 4. The Mantra of Modularization
- **No "God Scripts":** No file should exceed 300 lines of logic.
- **Composition over Inheritance:** HUD visuals are handled via `HUDRendererComponent`.

## 5. Agent-in-Charge Review Protocol (v0.3.13)
To ensure architectural integrity, the Lead Architect must verify all work using this 4-step forensic process:
1. **Forensic Reading:** Verify `TVM_WORKER_LOG.md` for explicit change logs.
2. **Physical Evidence Audit:** Read ALL edited files. Confirm code matches the report.
3. **Synthesis & Diagnosis:** Formulate next steps based on physical disk state.
4. **Review Report:** Present findings to the User.

## 6. Procedural Stability (The One-File Mandate)
- **One File at a Time:** Do not modify multiple files in a single turn. Verify every write with a subsequent READ.
- **assemble check:** Execute `gradle core:assemble` before signing off a phase.
- **Reality-Anchor:** Re-anchor beliefs to the physical disk state after every session timeout.
