# AGENT BRIEF: PROJECTILE & PAYLOAD LOGIC

## ROLE
You are the **Logic Agent** responsible for the physicality of the "Seeds of Sovereignty" combat system.

## CONTEXT
The Lead Architect has established a `ModuleChannelsComponent` (the data bus) and a `ProjectilePayloadComponent` (the blueprint). Your task is to turn these blueprints into active, behaving entities.

## OBJECTIVES
1. **Implement `ProjectileFactory.kt`**: 
   - A service to spawn projectile entities based on `ProjectileBlueprint`.
   - Support for `ProjectileType`: KINETIC, ENERGY, TORPEDO.
2. **Behavior Logic**:
   - Create systems for different flight paths (Straight, Homing, Accelerating).
3. **Collision & Damage**:
   - Implement basic circle/rect collision detection.
   - Apply damage to `ArcedDefenseComponent` or `HullComponent` on impact.
4. **Lifetime Management**:
   - Ensure projectiles are culled after their `lifeTime` or when exceeding distance bounds to maintain performance.

## CONSTRAINTS
- **Zero Allocations**: Use `Pools` for projectiles if possible, or strictly use pre-allocated vectors for math.
- **Fleks 2.x**: Adhere to `IteratingSystem` patterns and mapper access.
- **Reference**: @CombatComponents.kt, @ModuleActionSystem.kt
