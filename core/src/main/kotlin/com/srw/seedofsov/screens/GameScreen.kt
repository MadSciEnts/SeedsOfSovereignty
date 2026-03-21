package com.srw.seedofsov.screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.EntityFactory
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
    private val viewport = ExtendViewport(20f, 20f, camera)
    private val entityFactory = EntityFactory(game.engine)

    private var player: Entity? = null

    override fun show() {
        // Full Priority Execution Stack:
        // 10: Impulse (Logic)
        // 12: Bay (Unit Deployment)
        // 14: Seeking (Steering Logic)
        // 15: Weapons (Logic & Spawning)
        // 16: Drone Combat (Autonomous AI)
        // 18: Effects (Visual Lifecycle)
        // 19: Collision (Impact Logic)
        // 20: Physics (Integration)
        // 30: Camera (Tracking)
        // 40: Starfield (Background)
        // 50: Render (Entities)
        // 100: Interface (HUD Sync)
        game.engine.addSystem(ImpulseSystem())
        game.engine.addSystem(BaySystem(entityFactory))
        game.engine.addSystem(SeekingSystem())
        game.engine.addSystem(WeaponSystem(game))
        game.engine.addSystem(DroneCombatSystem()) // Registering Drone Combat logic
        game.engine.addSystem(EffectSystem())
        game.engine.addSystem(CollisionSystem())
        game.engine.addSystem(PhysicsSystem())
        game.engine.addSystem(CameraSystem(camera))
        game.engine.addSystem(StarfieldSystem(game.batch, viewport))
        game.engine.addSystem(RenderSystem(game.batch, viewport))
        game.engine.addSystem(InterfaceSystem(hud))

        player = spawnPlayer().also {
            hud.linkPlayer(it)
        }

        // Spawn Test Targets (Training Dummies)
        spawnTestTargets()

        Gdx.input.inputProcessor = hud.stage
    }

    private fun spawnPlayer(): Entity {
        return entityFactory.createPlayer(0f, 0f)
    }

    private fun spawnTestTargets() {
        // Create a ring of targets around the player
        val count = 6
        val radius = 10f
        for (i in 0 until count) {
            val angle = i * (360f / count)
            val x = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius
            entityFactory.createTrainingDummy(x, y)
        }
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0.002f, 0.002f, 0.008f, 1f)

        // Bridge Input
        player?.get(InputComponent.mapper)?.let { input ->
            input.impulseAxes.set(hud.navigationOutput)
        }

        // Run ECS Engine Pass
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
        game.engine.removeSystem(game.engine.getSystem(CameraSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(StarfieldSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(RenderSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(ImpulseSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(BaySystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(SeekingSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(WeaponSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(DroneCombatSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(EffectSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(CollisionSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(PhysicsSystem::class.java))
        game.engine.removeSystem(game.engine.getSystem(InterfaceSystem::class.java))
    }

    override fun dispose() {
        _hud?.dispose()
        _hud = null
    }
}
