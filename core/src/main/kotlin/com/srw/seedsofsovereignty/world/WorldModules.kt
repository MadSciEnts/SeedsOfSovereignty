package com.srw.seedsofsovereignty.world

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.WorldConfiguration
import com.srw.seedsofsovereignty.factory.ProjectileFactory
import com.srw.seedsofsovereignty.factory.ShipFactory
import com.srw.seedsofsovereignty.service.TextureService
import com.srw.seedsofsovereignty.system.*
import com.srw.seedsofsovereignty.ui.CommandMatrixView
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * GAME CONTEXT: Container for non-ECS dependencies.
 * Grouping these makes injection cleaner and easier for AI to trace.
 */
data class GameContext(
    val batch: Batch,
    val drawer: ShapeDrawer,
    val textureService: TextureService,
    val gameViewport: Viewport,
    val uiViewport: Viewport,
    val camera: OrthographicCamera,
    val joystick: Touchpad,
    val shipFactory: ShipFactory,
    val projectileFactory: ProjectileFactory,
    val commandMatrix: CommandMatrixView
)

/**
 * WORLD MODULES: Modular registration of systems and injectables.
 * This structure helps AI agents isolate specific game layers.
 */
object WorldModules {

    fun WorldConfiguration.registerContext(ctx: GameContext) {
        injectables {
            add(ctx.batch)
            add("batch", ctx.batch)
            add(ctx.drawer)
            add("drawer", ctx.drawer)
            add(ctx.textureService)
            add<Viewport>("gameViewport", ctx.gameViewport)
            add<Viewport>("uiViewport", ctx.uiViewport)
            add(ctx.camera)
            add("camera", ctx.camera)
            add("joystick", ctx.joystick)
            add(ctx.projectileFactory)
            add(ctx.shipFactory)
            add(ctx.commandMatrix)
            add(ctx)
        }
    }

    fun WorldConfiguration.registerSystems() {
        systems {
            // LOGIC LAYER
            add(PlayerInputSystem())
            add(EnginePropulsionSystem())
            add(MoveSystem())
            add(CollisionSystem())
            add(GeometrySystem())       // Process impact angles
            add(BallisticsSystem())     // Resolve damage & apply status
            add(StatusEffectSystem())   // Lingering DOT logic
            add(ModuleAttachmentSystem())
            add(ModuleActionSystem())
            add(ProjectileSystem())
            add(ImpactFXSystem())       // Transient visuals
            add(PowerThrottlingSystem())
            add(RadarSensorSystem())
            add(EvolutionSystem())
            add(StatSyncSystem())
            add(StationSystem())
            add(EnemyAISystem())
            add(EnemySpawnSystem())

            // RENDER LAYER
            add(StarfieldRenderSystem())
            add(TrailSystem())
            add(TrailRenderSystem())
            add(WorldRenderSystem())
            add(TacticalRenderSystem())
            add(RadarRenderSystem())
            add(CameraFollowSystem())

            // UI BRIDGE LAYER
            add(UIBridgeSystem())
        }
    }
}
