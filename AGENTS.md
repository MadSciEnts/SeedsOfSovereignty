# AI Agent Protocols - Seeds of Sovereignty

This document defines the operational rules and architectural standards for all AI agents (T1, T2, T3) working on the Seeds of Sovereignty project.

## 1. Agent Hierarchy & Delegation
- **T1 (Lead Agent):** Responsible for high-level architecture, project coordination, and complex decision-making.
- **T2/T3 (Specialized Agents):** Tasks that are too intensive or granular should be delegated to these agents for execution.

## 2. Operational Safety & Accuracy
- **Pre-Edit Read Requirement:** Before any file is edited, it **must** be read from disk immediately prior to the edit. This ensures the agent's understanding reflects the current reality on disk and prevents overwriting external changes.
- **Incremental Changes:** Prefer small, verifiable changes over large, monolithic updates.

## 3. Communication Protocol
Every agent response must begin and end with a capacity declaration to monitor cognitive load and stability:
`-[ current / max : Category ]-`

### Standard Categories:
- **Context Weight:** Measure of how much project context is being tracked.
- **Logic Layers:** Complexity of the reasoning chain.
- **Stability:** Reliability of the current state/environment.
- **Task Complexity:** Difficulty of the immediate request.
- **Confidence:** Agent's certainty in the proposed solution.

## 4. Tech Stack Standards
- **Engine:** libGDX
- **Language:** Kotlin
- **Architecture:** Ashley ECS (Entity Component System)
- **Platform:** Android (Primary)

## 5. Coding Standards
- Use idiomatic Kotlin.
- Follow ECS best practices: Keep Components as pure data, and Logic within Systems.
- Ensure resources are managed correctly according to libGDX lifecycle.
