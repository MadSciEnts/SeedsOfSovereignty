# RULES OF ENGAGEMENT: Lean Sovereign Standard (v1.2.0)

## 1. THE GOLDEN RULE (PHYSICAL SYNC)
- **MANDATORY:** Every file edit MUST follow: Physical READ -> Physical WRITE -> Physical VERIFY.
- **ATOMIC TURNS:** Only one file may be edited and verified per turn. 

## 2. TIMEOUT MITIGATION
- **LEAN REPORTING:** No conversational headers. State the Target File, Goal, Action, and Verification.
- **SURGICAL EDITS:** Prefer `replace_text` for files > 200 lines to conserve output tokens.
- **PAUSE & PURGE:** When Output Limit reaches **3 / 4**, finish the current file and HALT. Wait for "Proceed."

## 3. ECS & PERFORMANCE
- **ZERO ALLOCATIONS:** Use cached vectors/Pools inside systems.
- **Z-INDEX BATCHING:** Group draws by layer (0: Fleet, 1: VFX, 2: AR HUD).

## 4. STATUS INDICATORS
- **MANDATORY:** Always provide `[Context Weight / Max]` and `[Output Limit / Max]` in every turn.
- **DIAGNOSTIC:** Maintain `[current/max:category]` for the active mission.
