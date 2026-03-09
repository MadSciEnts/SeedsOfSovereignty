# SEED OF SOVEREIGNTY: AGENT HIERARCHY & ROLES (v0.5.4)

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
- **Lifecycle:** Must maintain a single persistent T3 session. If no T3 session exists, it must generate a [NEW] prompt.

### TIER 3: [T3 SURGICAL] (Implementer)
- **Role:** Pure Execution.
- **Responsibility:** Code modification based on `OB_SURGICAL_IMPLEMENTER.md`.

## 2. THE HANDOFF PROTOCOL (v0.5.4 - Mandatory Prefixes)
Every prompt generated for the Developer to relay MUST begin with the tier/category and session state.

**Format:** `[TX CATEGORY] [SESSION: STATE] ...`

**Example:**
`[T3 SURGICAL] [SESSION: NEW] Welcome aboard. Please read your onboarding file...`

## 3. THE LIFECYCLE RULE
- Agents remain "CONTINUING" as long as their specific chat tab remains active in the Developer's IDE.
- If an agent times out or the chat is cleared, the next handoff MUST be marked as [SESSION: NEW].
