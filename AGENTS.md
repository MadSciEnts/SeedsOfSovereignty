# SEED OF SOVEREIGNTY: AGENT HIERARCHY & ROLES (v0.5.5)

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
- **Comment Mandate:** Must verify that T3 agents have preserved all architectural annotations.

### TIER 3: [T3 SURGICAL] (Implementer)
- **Role:** Pure Execution.
- **Responsibility:** Code modification based on `OB_SURGICAL_IMPLEMENTER.md`.

## 2. THE HANDOFF PROTOCOL (v0.5.4 - Mandatory Prefixes)
Every prompt generated for the Developer to relay MUST begin with the tier/category and session state.
**Format:** `[TX CATEGORY] [SESSION: STATE] ...`

## 3. THE LIFECYCLE RULE
- Agents remain "CONTINUING" as long as their specific chat tab remains active.
- If an agent times out, the next handoff MUST be marked as [SESSION: NEW].

## 4. ARCHITECTURAL COMMENT PRESERVATION (v0.5.5)
- **PERMANENT ANNOTATIONS:** Any comment block beginning with `Technical Specification` or `ARCHITECTURAL WARNING` is a Lead Architect mandate.
- **PROHIBITION:** T2 and T3 agents are strictly forbidden from removing, moving, or altering these comment blocks.
- **ENFORCEMENT:** If a T3 agent removes a comment, the T2 Coordinator must fail the audit and command a revert.
- **SURGICAL PRECISION:** All code edits must be performed *around* these annotations.
