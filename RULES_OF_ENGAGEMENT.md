# RULES OF ENGAGEMENT: Coding Standards

## 1. Kotlin & LibKTX
- **Idiomatic Code:** Use `apply`, `let`, `with`, and extension functions.
- **UI Builders:** Use the `scene2d { ... }` DSL for HUD and Menu construction.

## 2. Performance (Android Focused)
- **Zero Allocations:** No `new` objects inside `onTick` or `render`. Use `Pools` for bullets/particles.
- **Batching:** Keep `SpriteBatch` calls grouped. All textures are procedural.

## 3. The Mantra of Modularization
- **No "God Scripts":** No file should exceed 300 lines of logic.
- **Refactoring Sprints:** Every 4 Tiers, perform a refactoring pass to maintain health.

## 4. Agent-in-Charge Review Protocol (v0.3.13)
To ensure architectural integrity, the Lead Architect must verify all work using this 4-step forensic process:
1. **Forensic Reading:** Read the `TVM_WORKER_LOG.md`. Ensure the agent explicitly lists files changed and provides a justification for why.
2. **Physical Evidence Audit:** Read ALL files the worker claims to have edited. Confirm the code matches the report and mission instructions.
3. **Synthesis & Diagnosis:** Formulate a planned next step based on findings. If implementation differs from expectations, prioritize a "Forward-Fix."
4. **Review Report:** Present a formal findings report to the User listing files touched, designer opinion on the alterations, and a binary Success/Failure verdict.

## 5. Verification & Log Protocol
- **assemble check:** Execute `gradle core:assemble` before signing off a phase.
- **One-File-One-Turn:** Do not modify multiple files in a single turn. Wait for feedback between writes.
- **Reality-Anchor:** Beliefs must be re-anchored to the physical disk state after every session timeout or interruption.
