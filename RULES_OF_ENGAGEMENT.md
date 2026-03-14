# RULES OF ENGAGEMENT: Tiered Sovereign Standard (v2.7.0)

## 1. THE GOLDEN RULE (PHYSICAL SYNC)
- **MANDATORY:** Every file edit MUST follow: Physical READ -> Physical WRITE -> Physical VERIFY.
- **ATOMIC TURNS:** Only one file may be edited and verified per turn.

## 2. DECLARATION OF WORK (NEW)
- **RULE:** Any declaration of work (e.g., "I have updated...", "I have fixed...") MUST be accompanied by a physical record of a `write_file` or `replace_text` command in the same turn.
- **PROHIBITION:** Agents are prohibited from claiming a file has been modified without an accompanying tool execution log.

## 3. ABSOLUTE PATH MANDATE (CRITICAL)
- **RULE:** ALL file paths MUST be specified as absolute paths starting from the project root.
- **PROHIBITION:** Agents are STRICTLY PROHIBITED from creating directories or files outside of the established source tree: `core/src/main/kotlin/com/srw/seedsofsovereignty/`.

## 4. DELETION PROTOCOL (T0 ONLY)
- **PROHIBITION:** Agents (T1, T2, T3) are STRICTLY PROHIBITED from deleting files or directories.
- **MANDATE:** Deletion of files must be requested by [T1 ARCHITECT] to [T0 DEVELOPER].

## 5. VERIFICATION & REVIEW MANDATE (CRITICAL)
- **RULE:** Before an agent can comment on the status, state, or validity of any file, they **MUST** perform a `read_file` command on that file in the current turn.

## 6. AGENT DESIGNATIONS & MANDATES
### [T1 ARCHITECT] (Strategic Orchestrator)
- **Protocol:** Must update `OB_FORENSIC_COORDINATOR.md` and review `T2_REPORT.log` before every major KICKOFF.

### [T2 COORD] (Forensic Coordinator)
- **Protocol:** Must update `OB_SURGICAL_IMPLEMENTER.md` with snippets before generating any T3 prompt.
- **Protocol:** Must physically READ `T3_REPORT.log` and the edited file before reporting completion.

### [T3 SURGICAL] (Surgical Implementer)
- **Protocol:** Must perform the edit, then use `analyze_current_file` to verify.
- **Protocol:** Must write results to `T3_REPORT.log` before responding.

## 7. OB & PROMPT HANDOFF (THE SYNC)
- **RULE:** A prompt MUST NOT be generated until the target agent's `OB_*.md` file has been physically updated.

## 8. STATUS INDICATORS
- **MANDATORY:** Always provide `[Context Weight / 40]` and `[Output Limit / 4]` in every turn.
- **DIAGNOSTIC:** Maintain `[current/max:category]` for the active mission.
