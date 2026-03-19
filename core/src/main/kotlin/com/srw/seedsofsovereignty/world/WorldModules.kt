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
    val navMatrix: CommandMatrixView,
    val tacticalMatrix: CommandMatrixView
)

/**
 * WORLD MODULES: Modular registration of systems and injectables.
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
            add("navMatrix", ctx.navMatrix)
            add("tacticalMatrix", ctx.tacticalMatrix)
            add(ctx)
        }
    }

    fun WorldConfiguration.registerSystems() {
        systems {
            add(ShipExecutionSystem())
            add(PowerDistributionSystem()) // HW BUS: PHASE 3
            add(MoveSystem())
            add(ModuleAttachmentSystem())
            add(ProjectileSystem())
            add(ImpactFXSystem())
            add(RadarSensorSystem())
            add(EvolutionSystem())
            add(StationSystem())

            // TACTICAL LAYER: Order Sync before AI Interpretation
            add(SquadCommandSystem())
            add(EnemyAISystem())
            add(EnemySpawnSystem())

            add(StarfieldRenderSystem())
            add(TrailSystem())
            add(TrailRenderSystem())
            add(WorldRenderSystem())
            add(TacticalRenderSystem())
            add(RadarRenderSystem())
            add(CameraFollowSystem())

            add(UIBridgeSystem())
        }
    }
}
