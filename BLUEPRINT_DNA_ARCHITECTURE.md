# ARCHITECTURE: SWARM SOVEREIGN BLUEPRINT & DNA SYSTEM

## 1. Overview
This document defines the recursive, data-driven framework for generating every object in "Seeds of Sovereignty." It separates **Definitions** (immutable Blueprints) from **Manifestations** (ECS Entities).

## 2. Fleks 2.x Implementation Pattern
To avoid the "logic knots" and syntax errors encountered previously, we strictly adhere to the following Fleks 2.x patterns:

### A. The "Expression" Model
- **Blueprint (Kotlin Data Class):** The "DNA" source. It is NOT a component.
- **Entity:** The manifestation. It is built by a Factory using the Blueprint.
- **Components:** Pure state containers that hold the *expressed* values (scaled by Tier/Rarity).

### B. Recursive Factory Strategy
- We utilize a `UniversalFactory` that follows a "Top-Down" recursive loop:
  1. `Unit (Ship/Drone)` -> Creates Core Entity.
  2. `Unit` reads its `ChassisDNA` -> Creates `Slot` Entities.
  3. `Slot` calls `spawnModule(ModuleDNA)`.
  4. `Module` reads its `SpawnDNA`.
  5. If `SpawnDNA` is a `Projectile`, configure firing logic.
  6. If `SpawnDNA` is another `Unit`, call `spawnUnit()` recursively.

### C. Context Stability (The syntax fix)
- To prevent build errors, all Factory functions MUST take `world: World` as a parameter.
- Component operations inside the Factory MUST be wrapped in `world.entity { ... }` or `entity.configure { ctx: EntityComponentContext -> ... }`.
- We avoid experimental `context` parameters/receivers in the core factory to ensure 100% build stability across all environments.

---

## 3. Atomic Implementation Roadmap

### Stage 1: The Atomic Schema (Registry Part A)
- **Goal:** Define the Kotlin data classes for `ProjectileDNA`, `ModuleDNA`, `SocketDNA`, and `UnitDNA`.
- **Validation:** Zero build errors. Unit testable data structures.

### Stage 2: The Scaling Engine (DNA Math)
- **Goal:** Implement the `DNAService`. A pure math class that takes a `BaseValue`, `Tier`, and `Subtier` and returns the `ExpressedValue`.
- **Validation:** Assert `Tier 2A > Tier 1C` for base DPS.

### Stage 3: The Recursive Factory (The Engine)
- **Goal:** Implement `UniversalFactory.spawnUnit(dna: UnitDNA)`.
- **Logic:** Must correctly spawn nested entities (Drones inside modules).
- **Validation:** Spawning a Tier 1 Pilot generates exactly: 1 Core, 1 Engine, 1 Cannon, 1 Shield, 1 Hull, and their respective Slot entities.

### Stage 4: The Vector2 Logic Bridge
- **Goal:** Standardize `ModuleChannelsComponent` with a "TARGET_INPUT" channel.
- **Validation:** Point Defense modules and Offensive Drones both read from this same channel.

---

## 4. Verification Loop
Before moving between stages, the following must be verified:
1. `gradle assemble` passes.
2. The `ShipFactory.kt` (or new `UniversalFactory`) is free of "Mapper" hallucinations.
3. No object allocations (`new Vector2`) occur in any system loops.

## 5. Delegate Directives
- **Directives for Sub-Agents:** Refer to `directives/STAGE_X_GUIDE.md` for specific surgical tasks.
