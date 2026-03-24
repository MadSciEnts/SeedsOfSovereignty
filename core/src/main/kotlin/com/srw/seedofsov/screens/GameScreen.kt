package com.srw.seedofsov.screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.InputComponent
import com.srw.seedofsov.ecs.systems.*
import com.srw.seedofsov.ui.hud.Hud
import ktx.app.KtxScreen
import ktx.ashley.get

class GameScreen(
    private val game: SovereigntyGame
) : KtxScreen {
    private var _hud: Hud? = null
    private val hud: Hud
        get() {
            if (_hud == null) {
                _hud = Hud(game)
            }
            return _hud!!
        }

    private val camera = OrthographicCamera()
    private val viewport = ExtendViewport(48f, 48f, camera)

    private var player: Entity? = null

    override fun show() {
        // Enacted High-Fidelity Execution Stack:
        // 10: Impulse (Input)
        // 11: Shields (Defensive Logic)
        // 15: Weapons (Logic)
        // 17: Projectiles (Lifecycle)
        // 18: Spatial Partition (Broad-phase Optimization)
        // 20: Physics (Linear Integration)
        // 25: Collision (Entry-Point / Standoff Logic)
        // 48: HUD FX (Technical Visualization System)
        // 49: Trails (Mesh Ribbon Rendering)
        // 50: Render (Entities)

        game.engine.addSystem(ImpulseSystem())
        game.engine.addSystem(ShieldSystem())
        game.engine.addSystem(BaySystem(game.entityFactory))
        game.engine.addSystem(SeekingSystem())
        game.engine.addSystem(WeaponSystem(game))
        game.engine.addSystem(DroneCombatSystem())
        game.engine.addSystem(ProjectileSystem())
        game.engine.addSystem(EffectSystem())

        val spatialSystem = SpatialPartitionSystem()
        game.engine.addSystem(spatialSystem)

        // Truth on Disk: HudFxSystem now bridges to the UI Hud instance
        game.engine.addSystem(HudFxSystem(hud))

        game.engine.addSystem(PhysicsSystem())
        game.engine.addSystem(CollisionSystem(spatialSystem))

        game.engine.addSystem(TacticalSystem(hud))
        game.engine.addSystem(CameraSystem(camera))
        game.engine.addSystem(StarfieldSystem(game.batch, viewport))
        game.engine.addSystem(TrailSystem(viewport))
        game.engine.addSystem(RenderSystem(game.batch, viewport))
        game.engine.addSystem(InterfaceSystem(hud))
        game.engine.addSystem(SimpleEnemyAiSystem())
        game.engine.addSystem(RadarSystem(hud))

        player = spawnPlayer().also {
            hud.linkPlayer(it)
        }

        spawnTestTargets()

        Gdx.input.inputProcessor = hud.stage
    }

    private fun spawnPlayer(): Entity {
        return game.entityFactory.createPlayer(0f, 0f)
    }

    private fun spawnTestTargets() {
        val count = 6
        val radius = 20f
        for (i in 0 until count) {
            val angle = i * (360f / count)
            val x = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius
            game.entityFactory.createEnemy(x, y, 1)
        }
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0.002f, 0.002f, 0.008f, 1f)

        player?.get(InputComponent.mapper)?.let { input ->
            input.impulseAxes.set(hud.navigationOutput)
        }

        game.engine.update(delta)

        hud.update(delta)
        hud.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        _hud?.resize(width, height)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        val engine = game.engine

        // Final Cleanup Pass
        engine.removeSystem(engine.getSystem(CameraSystem::class.java))
        engine.removeSystem(engine.getSystem(StarfieldSystem::class.java))
        engine.removeSystem(engine.getSystem(RenderSystem::class.java))
        engine.removeSystem(engine.getSystem(ImpulseSystem::class.java))
        engine.removeSystem(engine.getSystem(ShieldSystem::class.java))
        engine.removeSystem(engine.getSystem(BaySystem::class.java))
        engine.removeSystem(engine.getSystem(SeekingSystem::class.java))
        engine.removeSystem(engine.getSystem(WeaponSystem::class.java))
        engine.removeSystem(engine.getSystem(DroneCombatSystem::class.java))
        engine.removeSystem(engine.getSystem(ProjectileSystem::class.java))
        engine.removeSystem(engine.getSystem(EffectSystem::class.java))
        engine.removeSystem(engine.getSystem(SpatialPartitionSystem::class.java))
        engine.removeSystem(engine.getSystem(CollisionSystem::class.java))
        engine.removeSystem(engine.getSystem(PhysicsSystem::class.java))
        engine.removeSystem(engine.getSystem(TacticalSystem::class.java))
        engine.removeSystem(engine.getSystem(InterfaceSystem::class.java))
        engine.removeSystem(engine.getSystem(SimpleEnemyAiSystem::class.java))
        engine.removeSystem(engine.getSystem(RadarSystem::class.java))
        engine.removeSystem(engine.getSystem(TrailSystem::class.java))
        engine.removeSystem(engine.getSystem(HudFxSystem::class.java))
    }

    override fun dispose() {
        _hud?.dispose()
        _hud = null
    }
}
