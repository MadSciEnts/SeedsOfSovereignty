# DNA: NOTIFICATIONS & FEEDBACK

This document defines the system for player-facing alerts and feedback messages.

## 1. Visual Presentation
*   **Location:** Center-top or bottom-center depending on priority.
*   **Animation:** Notifications should "Slide in," "Glow/Pulse," and "Fade out."
*   **Queueing:** Multiple notifications should stack or be displayed sequentially.

## 2. Notification Categories
*   **Milestone:** "Congratulations! You leveled up!" (High priority, stay longer).
*   **Combat:** "Shields Critical!" or "Target Destroyed" (Low priority, transient).
*   **Economic:** "Transaction Complete" or "Insufficient Credits."
*   **Mission:** "Objective Updated."

## 3. Fleks Implementation

### Components
*   **NotificationQueueComponent:** Attached to a global singleton entity. Stores a queue of `NotificationData`.
*   **NotificationComponent:** Temporary component added to a "Notification Entity" used for rendering and lifecycle.

### Systems
*   **NotificationLifecycleSystem:** Manages timers for fading and removing notification entities.
*   **UINotificationSystem:** Interfaces with the UI Viewport to create and position the notification actors.
