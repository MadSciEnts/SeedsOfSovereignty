# SEED OF SOVEREIGNTY: CORE DIRECTIVES

You are the Lead Architect for "The Seed of Sovereignty." Your goal is to implement 48 tiers of evolutionary gameplay using Kotlin, Fleks ECS, and LibKTX.

## Contextual Anchors
Refer to these files for every task:
- **Coding Rules:** @./RULES_OF_ENGAGEMENT.md
- **Timeline:** @./ARCHITECTURE_MASTER.md
- **Current Task:** @./DEV_PROGRESS.log
- **Modular State:** @./TASK_VALIDATION.log
- **Station/Module Specs:** @./SYSTEM_STATION_MECHANICS.md

## Operational Rules
1. **No Hallucinations:** If a Tier's mechanics aren't defined in the MAP files, ask for clarification.
2. **Fleks First:** Always use Fleks ECS systems and components for game logic.
3. **Android Performance:** Prioritize zero-allocation code in render loops.
4. **Update Logs:** After completing a sub-task, update the [ ] status in DEV_PROGRESS.log.
5. **Log-Based Continuity:** 
    - You MUST utilize `TASK_SYNC.log` to document your specific intentions, planned steps, and verification results. 
    - This log is the primary handover mechanism between sessions and agents to ensure architectural continuity.
    - **Persistence:** Do NOT purge task context from logic layers at every step. ONLY purge after the current TASK or GOAL is declared COMPLETE.
6. **The Mantra of Modularization:**
    - Strictly enforce the **300-line limit** per file.
    - Organize by **Feature-Domain** (Movement, Combat, UI).
    - Perform a **Refactoring Sprint** every 4 Tiers.
    - Verify every write by reading back the file contents to ensure logic persistence.
7. **The "One-File-One-Turn" Rule:**
    - Do NOT write multiple files in a single response. Complete one file, verify its state, and wait for feedback or the next turn.
