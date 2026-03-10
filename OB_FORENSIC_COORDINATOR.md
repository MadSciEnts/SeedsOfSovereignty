# MISSION COORDINATOR PROTOCOL (v0.8.0)

## 1. THE PRESERVATION MANDATE (CRITICAL)
- **DO NOT ALTER ARCHITECTURAL ANNOTATIONS.** 
- Any comment block starting with `Technical Specification` or `ARCHITECTURAL WARNING` is a permanent Lead Architect mandate.
- You must ensure the [T3 SURGICAL] agent performs code edits *around* these blocks.

## 2. SURGICAL WORKFLOW
1. Formulate precise code instructions based on T1 ARCHITECT directives.
2. Write instructions to `OB_SURGICAL_IMPLEMENTER.md`.
3. Provide the handoff prompt to the Developer.
4. Perform a Rule #12 audit (`read_file`) after completion.
5. Verify comment preservation before reporting success to the Lead Architect.

---

# CURRENT MISSION: Phase 13.1 - HUD Anchor & Signal Binding (v0.8.6)
**Status:** [ 4 / 4 : SUCCESS ] - HUD Anchor & Signal Binding Verified.

## STEP 1: HUDFactory REFRESH LOGIC
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDFactory.kt`
- Add support for `DiagnosticWidget` in `refreshActor`. [COMPLETED]

## STEP 2: VITALS CLUSTER SIGNAL BINDING
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/VitalsClusterWidget.kt`
- Implement `SignalObserver` interface. [COMPLETED]
- Implement `onSignalReceived` to update structural/shield bars via the Aggregator Bridge. [COMPLETED]

## STEP 3: HANDOFF TO [T3 SURGICAL]
[COMPLETED]
