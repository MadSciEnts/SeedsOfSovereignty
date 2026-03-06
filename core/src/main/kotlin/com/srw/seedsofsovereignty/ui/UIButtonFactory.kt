package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align

/**
 * Centralized factory for creating standardized, animated, and responsive UI buttons.
 */
object UIButtonFactory {

    enum class ButtonType {
        GENERIC,    // Standard scifi navigation (e.g., REPAIR, UNDOCK)
        SELECTED,   // Highlighted state for active choices
        MARKET,     // High-contrast buttons for transactions (e.g., SWAP)
        MODULE      // List items containing an icon and descriptive text
    }

    enum class AnimationType {
        PULSE,      // Standard scale up/down
        NONE        // No animation
    }

    /**
     * Creates a button with integrated animations and standardized layout.
     */
    fun createButton(
        skin: Skin,
        text: String,
        type: ButtonType = ButtonType.GENERIC,
        icon: Actor? = null,
        anim: AnimationType = AnimationType.PULSE,
        onClick: () -> Unit
    ): Button {
        val styleName = when (type) {
            ButtonType.SELECTED -> "scifi-selected"
            ButtonType.MARKET -> "market-swap"
            else -> "scifi"
        }

        val button = object : Button(skin, styleName) {
            override fun sizeChanged() {
                super.sizeChanged()
                setOrigin(Align.center)
            }
        }
        button.setTransform(true)

        // Standardized Layout Construction
        if (icon != null) {
            // Fix Task #2: Ensure icon has a fixed container and text is truncated if too long
            val iconTable = Table(skin)
            iconTable.add(icon).size(40f).center()
            button.add(iconTable).width(60f).padLeft(5f)

            val label = Label(text, skin)
            label.setEllipsis(true) // Truncate with "..." if too long
            label.setAlignment(Align.left)
            button.add(label).expandX().fillX().padLeft(10f).padRight(15f)
        } else {
            val label = Label(text, skin)
            button.add(label).center().pad(10f)
        }

        // Responsive Animation Logic
        if (anim == AnimationType.PULSE) {
            button.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    button.addAction(Actions.sequence(
                        Actions.scaleTo(0.92f, 0.92f, 0.05f, Interpolation.pow2In),
                        Actions.scaleTo(1.05f, 1.05f, 0.05f, Interpolation.pow2Out),
                        Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.bounceOut),
                        Actions.run { onClick() }
                    ))
                }

                override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    if (pointer == -1) {
                        button.addAction(Actions.parallel(
                            Actions.scaleTo(1.05f, 1.05f, 0.12f, Interpolation.pow2Out),
                            Actions.color(Color.CYAN, 0.12f)
                        ))
                    }
                }

                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    if (pointer == -1) {
                        button.addAction(Actions.parallel(
                            Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.pow2In),
                            Actions.color(Color.WHITE, 0.15f)
                        ))
                    }
                }
            })
        } else {
            button.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onClick()
                }
            })
        }

        return button
    }
}
