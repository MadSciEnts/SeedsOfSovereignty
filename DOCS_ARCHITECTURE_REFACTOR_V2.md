# MASTER ARCHITECTURE REFACTOR V2: THE HARDWARE BUS

## 1. VISION
Unify all ship behavior under a single **Delegated Execution** model. The **Ship Core** acts as a hardware bus. Modules register with the Ship and are executed in a specific lifecycle order.

## 2. COMPLIANCE MANDATE
- **Fleks 2.x:** Square-bracket access only.
- **Zero Allocation:** Fixed-size FloatArrays for resources. No Map iteration.
- **Ship-First:** Logic flows Ship -> Module.

## 3. DATA INFRASTRUCTURE

### 3.1 ShipManifestComponent
- `modules: MutableList<Entity>`: Direct links to hardware.
- `lastMaintenance: Float`: Staggered cleanup timer.

### 3.2 ShipStateBusComponent
- `capacity: FloatArray(PowerCategory.size)`: Current available power per category.
- `load: FloatArray(PowerCategory.size)`: Total power requested by active modules.
- `totalOutput: Float`: Raw energy from the active Power Core.

### 3.3 Pilot/Matrix Module
- Acts as the signal bridge. For AI, it converts heuristics into `StandardChannels.TARGET/TRIGGER` inputs. For Players, the Matrix UI handles this.

## 4. EXECUTION LIFECYCLE (ShipExecutionSystem)

### Phase 1: Resource Generation
- Process the active `POWER_CORE`. Write `totalOutput` based on health.

### Phase 2: Distribution & Load Balancing
- Read UI/AI priority sliders.
- Divide `totalOutput` into `bus.capacity` buckets.
- Zero out `bus.load` for the new frame.

### Phase 3: Functional Execution
- Iterate manifest.
- **Module Update:** Modules request power (add to `bus.load`).
- **Logic Branch:** Execute logic (Thrust/Fire) scaled by `min(1.0, bus.capacity[cat] / bus.load[cat])`.

### Phase 4: Maintenance
- If `time > lastMaintenance + 5s`, remove dead entities from `modules`.

## 5. RE-ROUTING LOGIC
- Players can change a module's `PowerCategory` at runtime.
- The Engine module, for example, will then draw from the `AUX_1` capacity instead of `ENGINE`.
