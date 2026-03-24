package com.srw.seedofsov.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.utils.LongMap
import com.srw.seedofsov.ecs.components.TransformComponent
import com.srw.seedofsov.ecs.components.ShipStatsComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * ARCHITECTURAL STANDARDS: SPATIAL PARTITIONING
 *
 * This system provides a broad-phase broad-phase optimization for collision detection.
 * It bucketizes physical targets into a 2D spatial hash grid to reduce O(N*M) complexity to O(N).
 *
 * OPERATIONAL RULES:
 * 1. DO NOT ALTER the CELL_SIZE without cross-referencing max ship standoff radii.
 * 2. DO NOT ALTER the HASH KEY logic as it is optimized for LongMap performance.
 * 3. This system MUST run before CollisionSystem in the execution stack.
 */
class SpatialPartitionSystem : IteratingSystem(
    allOf(TransformComponent::class, ShipStatsComponent::class).get(),
    18 // Priority 18: Runs immediately before Collision and Physics logic
) {
    companion object {
        const val CELL_SIZE = 5.0f
    }

    // High-performance bucket storage
    private val grid = LongMap<MutableList<Entity>>()
    private val pool = mutableListOf<MutableList<Entity>>()

    override fun update(deltaTime: Float) {
        // Clear grid and return lists to internal pool to minimize GC pressure
        val iterator = grid.values()
        while (iterator.hasNext) {
            val list = iterator.next()
            list.clear()
            pool.add(list)
        }
        grid.clear()

        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper] ?: return

        val gx = (transform.position.x / CELL_SIZE).toInt()
        val gy = (transform.position.y / CELL_SIZE).toInt()
        val key = getHashKey(gx, gy)

        val bucket = grid.get(key) ?: (if (pool.isNotEmpty()) pool.removeAt(pool.size - 1) else mutableListOf())
        bucket.add(entity)
        grid.put(key, bucket)
    }

    /**
     * Returns a list of entities in the specified cell and its 8 neighbors.
     * Used by CollisionSystem to narrow the search space for projectiles.
     */
    fun getNearbyEntities(worldX: Float, worldY: Float, result: MutableList<Entity>) {
        val gx = (worldX / CELL_SIZE).toInt()
        val gy = (worldY / CELL_SIZE).toInt()

        for (ix in gx - 1..gx + 1) {
            for (iy in gy - 1..gy + 1) {
                grid.get(getHashKey(ix, iy))?.let { result.addAll(it) }
            }
        }
    }

    private fun getHashKey(x: Int, y: Int): Long {
        return (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
    }
}
