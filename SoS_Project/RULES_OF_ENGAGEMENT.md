# Seeds of Sovereignty - Rules of Engagement

This document is the "Constitutional Law" for all AI Agents participating in this project. Compliance is mandatory.

## 1. Sacred Documentation
*   All files within the `SoS_Project/` directory are **SACRED**.
*   Only the **T1 Project Lead** has authorization to modify these files.
*   Agents may read these files to understand their context but must never attempt to write to or delete them.

## 2. Self-Monitoring Protocol
*   Every major response must begin and end with a **Context & Logic Load** indicator.
*   **Format:** `-[ current / max :: Category ]-`
*   **Categories:**
    *   `Context`: Scope of project awareness (Modules, Files, Tiers).
    *   `Logic`: Complexity of the current computational/coding task.
    *   `Tokens`: Remaining capacity before hitting the model's response limit.

## 3. Architecture & Coding Standards
*   **Pure Kotlin:** No third-party frameworks (LibGDX, Fleks, etc.) unless explicitly authorized by T1.
*   **Stateless Systems:** Logic must be decoupled from data.
*   **Procedural First:** No external assets. All textures must be generated and cached.
*   **No God Scripts:** Files must stay below 300 lines. Break down complexity immediately.

## 4. Communication
*   Agents must refer to the `BLUEPRINT.md` and `ARCHITECTURE.md` for all technical implementations.
*   If a task conflicts with the sacred documentation, the agent must stop and request clarification from the T1 Lead.

## 5. Continuity & Graceful Pausing
*   If an agent's **Context**, **Logic**, or **Token** limits are approached (e.g., >80% capacity), the agent must:
    1. Complete any pending file operations (finish the current file write).
    2. State their current status clearly using the monitoring format.
    3. Explicitly request permission from the user to 'continue' or 'proceed'.
*   Do not attempt to start new files or initiate complex logic cycles if remaining capacity is insufficient to complete them.
