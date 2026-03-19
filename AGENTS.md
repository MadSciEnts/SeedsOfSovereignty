# SEED OF SOVEREIGNTY: CORE DIRECTIVES

You are an specialized agent for "The Seed of Sovereignty." Your goal is to implement 48 tiers of evolutionary gameplay using Kotlin, Fleks 2.x ECS, and LibKTX.

## Mandatory Pre-flight Protocol
Before writing any code, you MUST include the following statement in your response:

> "I have read `FLEKS_2X_MANDATE.md` and `SKILLS_RECIPES.md`. I confirm this implementation will not use legacy ECS patterns (like `ComponentMapper`). I will adhere to zero-allocation math and Fleks 2.x context-based syntax."

If you fail to provide this statement, your output will be rejected.

## Weather Report Protocol
To maintain synchronization and prevent processing timeouts, every response MUST start and end with a status report. This report MUST use a **vertical format** as shown below:

-[current / max : [category1]]-

-[current / max : [category2]]-

-[current / max : [category3]]-


**Categories to track:**
- **Error Density:** Number of active build/logic errors.
- **Context Weight:** Internal memory load (files tracked/complex logic held).
- **Task Complexity:** Difficulty of the current active directive.
- **Session Stability:** Readiness to process without timing out.

## Contextual Anchors
Refer to these files for every task:
- **Compliance Mandate:** @./FLEKS_2X_MANDATE.md
- **Logic Recipes:** @./SKILLS_RECIPES.md
- **Coding Rules:** @./RULES_OF_ENGAGEMENT.md
- **Timeline:** @./ARCHITECTURE_MASTER.md
- **Current Task:** @./DEV_PROGRESS.log

## Operational Rules
1. **No Hallucinations:** If a Tier's mechanics aren't defined in the MAP files, ask for clarification.
2. **Fleks First:** Always use Fleks ECS systems and components for game logic.
3. **Android Performance:** Prioritize zero-allocation code in render loops.
4. **Update Logs:** After completing a sub-task, update the [ ] status in DEV_PROGRESS.log.

## Strategy to Avoid Prompt Limit Exhaustion
**GOLDEN RULE: YOU MUST ALWAYS REMAIN WITHIN YOUR PROMPT REQUEST LIMITS. NEVER EXCEED YOUR PROMPT REQUEST LIMITS.**

To remain within the prompt request limits and prevent further "timeouts" or system throttles, I will implement the following operational protocols:
1. **Operation Batching:** I will no longer perform sequential single-file reads if I can identify all necessary files at once. I will use list_files and grep more effectively to narrow down targets before opening them.
2. **Zero Hallucination Policy:** I will not "guess" at the contents of a file I read several turns ago. If I need a file, I will perform a fresh read, but I will only do so when I am certain it is the next atomic step in the current task.
3. **Concise Analysis:** I will limit my verbal reasoning to the "Why" and "How" of the logic, avoiding repetitive summaries of the entire project history.
4. **Strict Step Boundaries:** I will only proceed to the next sub-task after receiving your explicit "Proceed" or "Continue" command. I will never "run ahead" into implementation during an investigation phase.
5. **Surgical Edits:** I will favor replace_text for targeted fixes instead of overwriting entire files with write_file when the file size is large, reducing the total tokens exchanged per prompt.
