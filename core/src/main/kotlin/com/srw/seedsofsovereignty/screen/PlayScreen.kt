package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
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
    private val joystick: Touchpad
    private val fireButton: TextButton

    init {
        // Create our Tier 1 Ship Entity
        world.entity {
            it += PlayerComponent()
            it += PositionComponent().apply { pos.set(0f, 0f) }
            it += OrientationComponent() // ADDED: Missing component
            it += VelocityComponent()
            it += WeaponComponent(fireRate = 1.0f) // Start with 1 shot/sec (Tier 1a)
            it += SensorComponent(radius = 1000f)
            it += EvolutionComponent(currentTier = 1, subTier = 1, essenceCollected = 0, essenceRequired = 5)
        }

        // Setup UI Skin
        skin = skin {
            val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.WHITE)
                fill()
            }
            val whiteTex = Texture(pixmap)
            pixmap.dispose()

            this["white"] = whiteTex

            touchpad("default") {
                background = this@skin.newDrawable("white", Color(0.2f, 0.2f, 0.2f, 0.5f))
                knob = this@skin.newDrawable("white", Color(0.8f, 0.8f, 0.8f, 0.8f)).apply {
                    minWidth = 40f
                    minHeight = 40f
                }
            }

            textButton("default") {
                up = this@skin.newDrawable("white", Color(0.4f, 0.2f, 0.2f, 0.5f))
                down = this@skin.newDrawable("white", Color(0.8f, 0.2f, 0.2f, 0.8f))
                font = com.badlogic.gdx.graphics.g2d.BitmapFont()
            }
        }

        joystick = Touchpad(10f, skin).apply {
            setSize(200f, 200f)
        }

        fireButton = TextButton("FIRE", skin).apply {
            setSize(200f, 200f)
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {}
            })
        }

        uiStage.addActor(joystick)
        uiStage.addActor(fireButton)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        val inputSystem = world.system<PlayerInputSystem>()
        inputSystem.joystickVector.set(joystick.knobPercentX, joystick.knobPercentY)
        inputSystem.isFiringRequested = fireButton.isPressed

        world.update(delta)

        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)

        // Reposition UI elements
        joystick.setPosition(50f, 50f)
        fireButton.setPosition(uiStage.width - fireButton.width - 50f, 50f)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
