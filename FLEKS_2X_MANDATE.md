# FLEKS 2.x MANDATE: NEGATIVE CONSTRAINTS & CODING STANDARDS

This project uses **Fleks 2.12**. Legacy patterns from Ashley ECS or Fleks 1.x will cause crashes and build failures. All agents MUST adhere to these constraints.

## 1. THE FORBIDDEN LEXICON
If you find yourself typing these, you are using the wrong architecture. STOP and refer to the "Syntax Bridge" below.

*   **NEVER USE:** `ComponentMapper` (Ashley legacy)
*   **NEVER USE:** `mapper[entity]` or `mapper.get(entity)`
*   **NEVER USE:** `EntitySubscription` (Fleks 1.x legacy)
*   **NEVER USE:** `ImmutableArray` (Ashley legacy)
*   **NEVER USE:** `family.getEntities()` (Fleks 1.x logic)
*   **NEVER USE:** `world.entityService` (Unstable internal API)
*   **NEVER USE:** `object : ChangeListener() { ... }` inside a high-frequency loop (Allocates memory).

## 2. THE SYNTAX BRIDGE

| Operation | **WRONG (Legacy)** | **RIGHT (Fleks 2.x)** |
| :--- | :--- | :--- |
| **Check Component** | `mapper.has(entity)` | `world.run { entity.has(Component) }` |
| **Get Data** | `mapper[entity]` | `with(world) { entity.get(Component) }` |
| **Add Component** | `entity.add(Comp())` | `entity.configure { it += Comp() }` |
| **Remove Component** | `entity.remove<Comp>()` | `entity.configure { it -= Comp }` |
| **Remove Entity** | `world.remove(entity)` | `entity.remove()` or `world -= entity` |
| **Injection** | `@Inject lateinit val...` | `private val sys: Service = inject()` |

## 3. UI-TO-ECS FIREWALL (THE "CRASH BARRIER")
UI code (Scene2D) and ECS Logic run on the same thread but have different scoping rules.

1.  **Read-Only UI:** UI widgets should read from pre-initialized `OutputChannelData`.
2.  **Input Buffering:** UI listeners (buttons/joysticks) MUST write to existing `InputChannelData`. 
3.  **No Structural Shifts in UI:** Never call `entity.configure` directly inside a UI `ChangeListener` unless it is wrapped in a `world.run` block and is absolutely necessary.
4.  **Stable References:** Always capture `skin` and `world` as local variables if using them inside long-lived listener blocks to avoid closure-based `NullPointerExceptions`.

## 4. ZERO-ALLOCATION ENFORCEMENT
*   **Math:** Use pre-allocated `Vector2` or `tmp` objects defined at the class level.
*   **Tick Loop:** No `new` objects, no `mutableListOf()`, no `mapOf()` inside `onTickEntity`.
*   **Mappers:** While `ComponentMapper` is forbidden, Fleks 2.x allows `world.componentService.holder(Component)` for high-performance indexing. Prefer the `entity.get` extension for readability unless in a heavy render loop.
