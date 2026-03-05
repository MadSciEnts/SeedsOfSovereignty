package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import ktx.app.KtxScreen
import ktx.style.*

class PlayScreen(
    private val world: World,
    private val viewport: Viewport,
    private val uiViewport: Viewport
) : KtxScreen {

    private val uiStage = Stage(uiViewport)
    private val skin: Skin
    private val moveJoystick: Touchpad
    private val aimJoystick: Touchpad
    private val fireButton: TextButton
    private val testEvolveButton: TextButton

    private val repairButton: TextButton
    private val undockButton: TextButton

    init {
        world.entity {
            it += PlayerComponent()
            it += PositionComponent().apply { pos.set(0f, 0f) }
            it += OrientationComponent()
            it += VelocityComponent()
            it += WeaponComponent(fireRate = 1.0f)
            it += SensorComponent(radius = 1000f)
            it += EvolutionComponent(currentTier = 1, subTier = 1, essenceCollected = 0, essenceRequired = 5)
            it += DockingComponent()
            it += ApertureComponent()
        }

        skin = skin {
            val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.WHITE)
                fill()
            }
            val whiteTex = Texture(pixmap)
            pixmap.dispose()

            this["white"] = whiteTex
            this["default"] = BitmapFont()

            touchpad("default") {
                background = this@skin.newDrawable("white", Color(0.2f, 0.2f, 0.2f, 0.5f))
                knob = this@skin.newDrawable("white", Color(0.8f, 0.8f, 0.8f, 0.8f)).apply {
                    minWidth = 40f
                    minHeight = 40f
                }
            }

            touchpad("aim") {
                background = this@skin.newDrawable("white", Color(0.4f, 0.2f, 0.2f, 0.3f))
                knob = this@skin.newDrawable("white", Color(1f, 0.2f, 0.2f, 0.6f)).apply {
                    minWidth = 30f
                    minHeight = 30f
                }
            }

            textButton("default") {
                up = this@skin.newDrawable("white", Color(0.4f, 0.2f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.8f, 0.2f, 0.2f, 0.8f))
                font = this@skin.getFont("default")
            }

            textButton("station") {
                up = this@skin.newDrawable("white", Color(0.1f, 0.4f, 0.4f, 0.7f))
                down = this@skin.newDrawable("white", Color(0.2f, 0.8f, 0.8f, 0.9f))
                font = this@skin.getFont("default")
            }

            textButton("debug") {
                up = this@skin.newDrawable("white", Color(0.2f, 0.4f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.2f, 0.8f, 0.2f, 0.8f))
                font = this@skin.getFont("default")
            }
        }

        moveJoystick = Touchpad(10f, skin, "default").apply { setSize(200f, 200f) }
        aimJoystick = Touchpad(10f, skin, "aim").apply { setSize(200f, 200f) }
        fireButton = TextButton("FIRE", skin).apply { setSize(200f, 200f) }

        repairButton = TextButton("REPAIR SHIP", skin, "station").apply {
            setSize(300f, 80f)
            isVisible = false
        }

        undockButton = TextButton("UNDOCK", skin, "station").apply {
            setSize(300f, 80f)
            isVisible = false
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    val player = world.family { all(PlayerComponent, DockingComponent, PositionComponent) }.firstOrNull()
                    player?.let { p ->
                        with(world) {
                            val docking = p[DockingComponent]
                            val pos = p[PositionComponent].pos
                            val vel = p[VelocityComponent].v

                            docking.isDocked = false
                            docking.undockCooldown = 3f // Mandatory wait before re-docking

                            // Push away from station
                            docking.nearestStation?.let { station ->
                                val sPos = station[PositionComponent].pos
                                val pushDir = com.badlogic.gdx.math.Vector2(pos).sub(sPos).nor()
                                if (pushDir.isZero) pushDir.set(1f, 1f).nor()
                                vel.set(pushDir).scl(500f)
                            } ?: run {
                                vel.set(-300f, -300f)
                            }
                        }
                    }
                }
            })
        }

        testEvolveButton = TextButton("DEV: EVOLVE", skin, "debug").apply {
            setSize(150f, 50f)
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    val family = world.family { all(PlayerComponent, EvolutionComponent) }
                    if (family.numEntities > 0) {
                        val player = family.first()
                        with(world) {
                            val evo = player[EvolutionComponent]
                            evo.essenceCollected = evo.essenceRequired
                        }
                    }
                }
            })
        }

        uiStage.addActor(moveJoystick)
        uiStage.addActor(aimJoystick)
        uiStage.addActor(fireButton)
        uiStage.addActor(repairButton)
        uiStage.addActor(undockButton)
        uiStage.addActor(testEvolveButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        val playerFamily = world.family { all(PlayerComponent, EvolutionComponent, DockingComponent, ApertureComponent) }
        val player = playerFamily.firstOrNull()

        if (player != null) {
            with(world) {
                val currentTier = player[EvolutionComponent].currentTier
                val docking = player[DockingComponent]
                val aperture = player[ApertureComponent]
                val isTwinStickTier = currentTier >= 4

                if (docking.isDocked && aperture.state == ApertureComponent.State.OPEN) {
                    moveJoystick.isVisible = false
                    aimJoystick.isVisible = false
                    fireButton.isVisible = false
                    repairButton.isVisible = true
                    undockButton.isVisible = true
                } else {
                    moveJoystick.isVisible = true
                    aimJoystick.isVisible = isTwinStickTier
                    fireButton.isVisible = !isTwinStickTier
                    repairButton.isVisible = false
                    undockButton.isVisible = false
                }

                inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)
                if (isTwinStickTier) {
                    inputSystem.aimVector.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
                    inputSystem.isFiringRequested = !inputSystem.aimVector.isZero
                } else {
                    inputSystem.aimVector.set(0f, 0f)
                    inputSystem.isFiringRequested = fireButton.isPressed
                }
            }
        }

        world.update(delta)
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)
        moveJoystick.setPosition(50f, 50f)
        aimJoystick.setPosition(uiStage.width - aimJoystick.width - 50f, 50f)
        fireButton.setPosition(uiStage.width - fireButton.width - 50f, 50f)
        repairButton.setPosition(uiStage.width / 2f - repairButton.width / 2f, uiStage.height / 2f + 20f)
        undockButton.setPosition(uiStage.width / 2f - undockButton.width / 2f, uiStage.height / 2f - 100f)
        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 70f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
