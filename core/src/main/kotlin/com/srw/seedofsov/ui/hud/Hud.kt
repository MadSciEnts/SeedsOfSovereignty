package com.srw.seedofsov.ui.hud

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.ModuleCategory
import com.srw.seedofsov.model.ModuleSlot
import com.srw.seedofsov.ui.Panel
import com.srw.seedofsov.ui.UiAssetListener
import ktx.ashley.get

/**
 * Manages the high-level HUD structure with independent Command Matrix areas.
 * Truth on Disk: Bridges tactical selection state to ECS systems.
 */
class Hud(val game: SovereigntyGame) : Disposable, UiAssetListener {
    val stage = Stage(ScreenViewport(), game.batch)
    val navigationOutput = Vector2()

    private val navMatrix = CommandMatrix(game, isLeftAligned = true)
    private val tacticalMatrix = CommandMatrix(game, isLeftAligned = false)
    private val rightCategoryShelf = Panel()

    private var playerShip: ShipComponent? = null
    private var pendingTacticalSlots: List<ModuleSlot>? = null

    private var _nautilus: NautilusDisplay? = null
    val nautilus: NautilusDisplay
        get() {
            if (_nautilus == null) {
                _nautilus = NautilusDisplay(game)
                stage.addActor(_nautilus)
            }
            return _nautilus!!
        }

    private var _radar: RadarDisplay? = null
    val radar: RadarDisplay
        get() {
            if (_radar == null) {
                _radar = RadarDisplay(game)
                stage.addActor(_radar)
            }
            return _radar!!
        }

    init {
        stage.addActor(navMatrix)
        stage.addActor(tacticalMatrix)
        stage.addActor(rightCategoryShelf)
        game.uiFactory.addListener(this)
    }

    override fun onAssetsReady() {
        rightCategoryShelf.background = game.uiFactory.skin.getDrawable("panel")
        pendingTacticalSlots?.let { populateSlotShelf(it) }
        pendingTacticalSlots = null
    }

    fun getTacticalActiveSlot(): ModuleSlot? = tacticalMatrix.activeSlot

    fun linkPlayer(player: Entity) {
        val ship = player[ShipComponent.mapper] ?: return
        playerShip = ship

        ship.getSlot(ModuleCategory.ENGINE)?.let {
            navMatrix.selectModuleFromSlot(it)
        }

        val tacticalSlots = ship.slots.filter {
            it.category != ModuleCategory.ENGINE && it.category != ModuleCategory.NAVIGATION
        }

        if (game.uiFactory.isLoaded) {
            populateSlotShelf(tacticalSlots)
        } else {
            pendingTacticalSlots = tacticalSlots
        }

        tacticalSlots.firstOrNull()?.let { tacticalMatrix.selectModuleFromSlot(it) }
    }

    private fun populateSlotShelf(slots: List<ModuleSlot>) {
        if (!game.uiFactory.isLoaded) {
            pendingTacticalSlots = slots
            return
        }

        rightCategoryShelf.clearChildren()
        val btnWidth = 220f
        val btnHeight = 80f
        val spacing = 15f
        val shelfWidth = (slots.size * (btnWidth + spacing)) + 20f
        rightCategoryShelf.setSize(shelfWidth, 120f)

        var currentX = rightCategoryShelf.width - btnWidth - 10f
        slots.forEach { slot ->
            val displayName = slot.equippedModule?.name ?: slot.category.displayName
            val btn = game.uiFactory.createButton(displayName).apply {
                setSize(btnWidth, btnHeight)
                setPosition(currentX, 20f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        tacticalMatrix.selectModuleFromSlot(slot)
                    }
                })
            }
            rightCategoryShelf.addActor(btn)
            currentX -= (btnWidth + spacing)
        }
        layout()
    }

    private fun layout() {
        val sWidth = stage.width
        val sHeight = stage.height
        if (sWidth <= 10f || sHeight <= 10f) return

        nautilus.setPosition(20f, sHeight - nautilus.height - 20f)
        radar.setPosition(sWidth - radar.width - 20f, sHeight - radar.height - 20f)
        navMatrix.setPosition(20f, 20f)
        rightCategoryShelf.setPosition(sWidth - rightCategoryShelf.width - 20f, 20f)
        tacticalMatrix.setPosition(sWidth - tacticalMatrix.width - 20f, rightCategoryShelf.y + rightCategoryShelf.height + 10f)
    }

    fun update(delta: Float) {
        stage.act(delta)
        navigationOutput.set(navMatrix.getNavigationOutput())
    }

    fun draw() {
        if (stage.width > 0 && stage.height > 0) {
            layout()
            stage.draw()
        }
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        layout()
    }

    override fun dispose() {
        _nautilus?.dispose()
        _radar?.dispose()
        stage.dispose()
    }
}
