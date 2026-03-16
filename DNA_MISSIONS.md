# DNA: MISSIONS & OBJECTIVES

This document defines the structure for mission generation, tracking, and rewards.

## 1. Mission Types
*   **Extermination:** Kill a specific boss or N enemies at coordinates (X, Y).
*   **Acquisition:** Find and retrieve a specific item/cargo.
*   **Escort:** Protect a friendly entity until it reaches a destination.
*   **Exploration:** Visit a specific sector or coordinate.

## 2. Mission Generation
*   **Location:** Missions are primarily offered at Space Stations.
*   **Difficulty:** Scaled based on the distance of the objective from (0,0) and current player Tier.
*   **Persistence:** Active missions are saved in the `MissionComponent`.

## 3. Rewards
*   **Currency:** Credits for use in Station Shops.
*   **Tier Advancement:** Some missions are required for "Evolutionary Ascension" between Epochs.
*   **Modules:** Rare modules provided as direct rewards.

## 4. Fleks Implementation

### Components
*   **MissionComponent:** Attached to player. Stores a list of active `MissionData`.
*   **MissionObjectiveComponent:** Attached to world entities (targets/items) to link them to an active mission.

### Systems
*   **MissionTrackingSystem:** Monitors objective completion (e.g., target entity death, proximity reached).
*   **RewardSystem:** Handles the distribution of credits/items upon mission success.
