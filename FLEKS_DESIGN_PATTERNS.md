# FLEKS ECS: Design Patterns & Architecture

This document defines the mandatory Fleks ECS patterns for "Seeds of Sovereignty." All agents must adhere to these standards to ensure scalability across 48 tiers and optimal Android performance.

## 1. World Configuration & DI
*   **DSL Version:** Use Fleks 2.x `configureWorld { ... }`.
*   **Dependency Injection:** 
    *   Use the `injectables { add(item) }` block for shared services (Camera, Viewports, AssetStorage, EventBus).
    *   Systems must receive dependencies via constructor injection: `class MySystem(val cam: Camera = inject()) : IteratingSystem(...)`.
*   **System Order:** Systems execute in the order they are added. 
    1. Input/AI Systems
    2. Logic/Physics Systems
    3. Attachment/Hierarchy Systems
    4. Camera Systems
    5. Render Systems (Last)

## 2. Component Standards
*   **Pure Data:** Components must be `data class` or `class` with NO logic.
*   **Fleks 2.x Boilerplate:** Every component must implement `Component<T>` and provide a `companion object : ComponentType<T>()`.
    ```kotlin
    data class MyComponent(val x: Float = 0f) : Component<MyComponent> {
        override fun type() = MyComponent
        companion object : ComponentType<MyComponent>()
    }
    ```
*   **Zero-Allocation:** Avoid creating new objects (Vector2, etc.) inside component properties if they will be modified. Use pre-allocated objects or primitives.

## 3. System Patterns
*   **Iteration:** Use `IteratingSystem` for entity-specific logic.
*   **Family Definition:** Use the constructor `family { all(A, B).none(C) }`. Do NOT use `@AllOf` annotations (deprecated/less flexible).
*   **Method Override:** Always override `onTickEntity(entity: Entity)` for logic. Do NOT use `onEntityTick` (1.x naming).
*   **Communication:** Systems communicate via:
    1.  **Components:** Changing data on an entity for another system to read.
    2.  **EventBus:** Using an injected `KtxEventBus` for one-off signals (e.g., `ShipDestroyed`).

## 4. Modular Ship Hierarchy (Parent-Child)
To support multi-module ships (Tiers 2-48):
*   **Root Entity:** The "Core" entity containing `PositionComponent` and `VelocityComponent`.
*   **Module Entity:** Contains `ModuleComponent` and `AttachmentComponent`.
*   **AttachmentComponent:** Must store `val parentId: Int` and `val offset: Vector2`.
*   **Logic:** A `ModuleAttachmentSystem` iterates modules and sets their world position based on `parent.pos + offset`.

## 5. Infinite Space & Warp
*   **Origin Shifting:** A `WorldShiftSystem` must monitor player distance from `(0,0)`. If `> 10,000`, it must subtract the offset from all `PositionComponent` values in the world.
*   **Sector Warp:** To change sectors, use `world.entityService.removeAll()` for non-persistent entities and reset the `SectorContext` injectable.

## 6. Android Optimization
*   **onInit():** Use `onInit()` in systems to cache component holders: `private val posMapper = ComponentMapper<PositionComponent>()`.
*   **Mapper Access:** Access components via mappers for the fastest possible lookup: `val pos = posMapper[entity]`.
