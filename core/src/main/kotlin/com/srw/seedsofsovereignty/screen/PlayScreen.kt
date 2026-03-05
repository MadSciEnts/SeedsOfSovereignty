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

            textButton("debug") {
                up = this@skin.newDrawable("white", Color(0.2f, 0.4f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.2f, 0.8f, 0.2f, 0.8f))
                font = this@skin.getFont("default")
            }
        }

        moveJoystick = Touchpad(10f, skin, "default").apply { setSize(200f, 200f) }
        aimJoystick = Touchpad(10f, skin, "aim").apply { setSize(200f, 200f) }

        fireButton = TextButton("FIRE", skin).apply {
            setSize(200f, 200f)
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {}
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
        uiStage.addActor(testEvolveButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()

        // Find player's current tier
        val playerFamily = world.family { all(PlayerComponent, EvolutionComponent) }
        val player = playerFamily.firstOrNull()
        val currentTier = if (player != null) {
            with(world) { player[EvolutionComponent].currentTier }
        } else 1

        // Dynamic UI Visibility based on Tier
        val isTwinStickTier = currentTier >= 4
        aimJoystick.isVisible = isTwinStickTier
        fireButton.isVisible = !isTwinStickTier

        // Update Input System
        inputSystem.joystickVector.set(moveJoystick.knobPercentX, moveJoystick.knobPercentY)

        if (isTwinStickTier) {
            inputSystem.aimVector.set(aimJoystick.knobPercentX, aimJoystick.knobPercentY)
            inputSystem.isFiringRequested = !inputSystem.aimVector.isZero
        } else {
            inputSystem.aimVector.set(0f, 0f)
            inputSystem.isFiringRequested = fireButton.isPressed
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
        testEvolveButton.setPosition(uiStage.width / 2f - testEvolveButton.width / 2f, uiStage.height - 70f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
