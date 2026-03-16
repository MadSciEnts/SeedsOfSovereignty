# SKILLS RECIPES: FLEKS 2.x GOLD STANDARDS

Use these exact patterns for all implementation tasks. These recipes ensure build stability and Android performance.

## 1. SPAWNING ENTITIES
Always use the `world.entity { it += ... }` DSL.

```kotlin
// In a Factory class
fun spawnSomething(world: World, position: Vector2) {
    world.entity { entity ->
        entity += PositionComponent(Vector2(position)) // Use copy constructor for stability
        entity += VelocityComponent()
        entity += ShapeComponent(type = ShapeType.CIRCLE, color = Color.RED)
    }
}
```

## 2. ACCESSING COMPONENTS (SYSTEMS)
Inside an `IteratingSystem`, use the square bracket operator (which uses mappers internally).

```kotlin
class MySystem : IteratingSystem(family { all(PositionComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[PositionComponent] // Fleks 2.x standard extension
        pos.pos.add(1f, 0f)
    }
}
```

## 3. ACCESSING COMPONENTS (OUTSIDE SYSTEMS)
Use `world.run` or `with(world)` to gain the `EntityComponentContext`.

```kotlin
// In UI or Event Listeners
world.run {
    if (entity.has(PlayerComponent)) {
        val stats = entity.get(StatsComponent)
        println(stats.hp)
    }
}
```

## 4. MODIFYING COMPONENTS (STRUCTURAL CHANGES)
To add or remove components after an entity is spawned, use `configure`.

```kotlin
entity.configure { 
    it += BuffComponent() // Add
    it -= DamageComponent // Remove (using the Companion Object)
}
```

## 5. THE UI-TO-ECS BRIDGE (MODULE CHANNELS)
UI must NEVER trigger structural changes directly. It writes to existing channel data.

```kotlin
// In CommandMatrixView widget listener
val input = channels.inputs["AIM"] ?: return
input.vector.set(knobX, knobY)
input.triggered = true

// In ModuleActionSystem (Logic)
val input = channels.inputs["AIM"]
if (input?.triggered == true) {
    // execute logic...
    input.triggered = false // Reset if one-shot, or leave for continuous
}
```

## 6. ZERO-ALLOCATION MATH
Never create `Vector2()` inside `onTickEntity`.

```kotlin
class MoveSystem : IteratingSystem(...) {
    private val tempVec = Vector2() // Pre-allocated at class level

    override fun onTickEntity(entity: Entity) {
        val vel = entity[VelocityComponent].v
        tempVec.set(vel).scl(deltaTime) // Use temp for calculation
        entity[PositionComponent].pos.add(tempVec)
    }
}
```
