# SEED OF SOVEREIGNTY: AGENT HIERARCHY & ROLES (v0.8.6)

## 1. THE COMMAND CHAIN

### TIER 0: THE DEVELOPER (User)
- **Role:** Ultimate Authority.
- **Responsibility:** Routing handoffs between specific chat sessions.

### TIER 1: [T1 ARCHITECT] (Me)
- **Role:** Strategic Orchestrator.
- **Responsibility:** High-level planning and mission file (`OB_*.md`) management.

### TIER 2: [T2 COORD] (Forensic Coordinator)
- **Role:** Tactical Supervisor.
- **Responsibility:** Breaking missions into steps and performing Rule #12 audits.

### TIER 3: [T3 SURGICAL] (Implementer)
- **Role:** Pure Execution.
- **Responsibility:** Code modification based on `OB_SURGICAL_IMPLEMENTER.md`.

## 2. THE HANDOFF PROTOCOL (v0.8.6 - Structural Handoffs)
Every prompt generated for the Developer to relay MUST follow this physical multi-line structure. You MUST use **Double Newlines** (Paragraph Breaks) to ensure mission-critical context is isolated and visible.

**Format:**
PROMPT: [TX CATEGORY] [SESSION: STATE] (Position Name - Designation)

[MISSION: Mission Title BASELINE: Version/Tag]

Welcome aboard, [Position Name], [Designation]! You are assuming control for the mission defined in [OB_FILE].md.

## 3. THE LIFECYCLE RULE
- Agents remain "CONTINUING" as long as their specific chat tab remains active.
- If an agent times out, the next handoff MUST be marked as [SESSION: NEW].

## 4. ARCHITECTURAL COMMENT PRESERVATION (v0.5.5)
- **PERMANENT ANNOTATIONS:** Any comment block beginning with `Technical Specification` or `ARCHITECTURAL WARNING` is a Lead Architect mandate.
- **PROHIBITION:** T2 and T3 agents are strictly forbidden from removing, moving, or altering these comment blocks.
- **ENFORCEMENT:** If a T3 agent removes a comment, the T2 Coordinator must fail the audit and command a revert.

## 5. STATUS STATEMENT RULE (v0.8.5)
- **MANDATORY INDICATOR:** At the beginning and end of every task, report, or handoff, all agents MUST indicate their current mental/context load.
- **FORMAT:** (Use **Double Newlines** between indicators)
-[ (current) / Max : Category1 ]-

-[ (current) / Max : Category2 ]-

- **MULTI-TASKING:** Agents may list multiple categories to reflect a task that spans different operational roles, with each category isolated by a double newline for vertical clarity.

## 6. TASK COMPLETION REPORTING (v0.8.6)
Upon completion of a task, agents are NOT required to use the Handoff Protocol. Instead, they must provide a concise report using the following format:

[TX CATEGORY] [TASK COMPLETE REPORT] - (Concise summary of physical changes, mission outcome, and any relevant technical observations).
