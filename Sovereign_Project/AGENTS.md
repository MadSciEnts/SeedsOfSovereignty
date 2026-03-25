# AGENT OPERATING PROTOCOL: SOVEREIGN ENGINE

## 1. MANDATORY BOOT SEQUENCE
Before writing ANY code, you must read the files via `BOOTSTRAP.md`.

## 2. THE CORE COMMANDMENTS
- **SINGLE TASKING:** Complete exactly ONE task from the Manifest per session.
- **PRECISION RIGIDITY:** All World-Space math MUST use `Double`. Render math MUST use `Float` (WorldPos - CameraPos).
- **NAVAL PACING:** Ships have high inertia. No instant stops. No "twitch" rotations.
- **RENDER-TO-CACHE:** Units are rendered ONCE to a Bitmap cache at birth/evolution.
- **NO HALLUCINATION:** If a variable is not in the `LEDGER.md`, do not invent new global state.

## 3. CONTEXT MANAGEMENT (Persistent Sessions)
- If the chat history exceeds 5 tasks, alert the user: "CONTEXT DEGRADATION WARNING."
- To 'flush' state: summarize the current codebase into `LEDGER.md` and state: "INTERNAL CACHE RESET: Relying strictly on Disk Files."

## 4. SESSION TERMINATION
Update `LEDGER.md` with a technical summary (Active Registry Keys, Math Formulas, and Handoff Notes).
