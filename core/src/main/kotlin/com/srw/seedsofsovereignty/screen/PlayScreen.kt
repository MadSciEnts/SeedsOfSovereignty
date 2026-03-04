package com.srw.seedsofsovereignty.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.PlayerComponent
import com.srw.seedsofsovereignty.component.PositionComponent
import com.srw.seedsofsovereignty.component.VelocityComponent
import com.srw.seedsofsovereignty.system.PlayerInputSystem
import ktx.app.KtxScreen
import ktx.style.*

class PlayScreen(
    private val world: World,
    private val viewport: ExtendViewport
) : KtxScreen {

    private val uiStage = Stage(ExtendViewport(1280f, 720f))
    private val skin: Skin
    private val joystick: Touchpad

    init {
        // Create our Tier 1 Ship Entity
        world.entity {
            it += PlayerComponent()
            it += PositionComponent().apply { pos.set(0f, 0f) }
            it += VelocityComponent()
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
        }

        joystick = Touchpad(10f, skin).apply {
            setPosition(50f, 50f)
            setSize(200f, 200f)
        }
        uiStage.addActor(joystick)

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        // Update input system with joystick data
        world.system<PlayerInputSystem>().joystickVector.set(
            joystick.knobPercentX,
            joystick.knobPercentY
        )

        // Update ECS
        world.update(delta)

        // Draw UI
        uiStage.act(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        uiStage.dispose()
        skin.dispose()
    }
}
