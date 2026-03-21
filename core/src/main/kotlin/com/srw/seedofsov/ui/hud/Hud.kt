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
 * Truth on Disk: Implements UiAssetListener to ensure safe asynchronous widget generation.
 * Evolution Path: Designed to transition from Single-Ship Pilot to Fleet Commander (Epoch 3).
 */
class Hud(val game: SovereigntyGame) : Disposable, UiAssetListener {
    val stage = Stage(ScreenViewport(), game.batch)
    val navigationOutput = Vector2()

    // Command Matrices: Left for Nav/Engines, Right for Tactical/Modules
    private val navMatrix = CommandMatrix(game, isLeftAligned = true)
    private val tacticalMatrix = CommandMatrix(game, isLeftAligned = false)

    // The "Shelf" holds category or ship selection buttons
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

        // Register for asset readiness
        game.uiFactory.addListener(this)
    }

    override fun onAssetsReady() {
        // Update background drawable once assets are generated
        rightCategoryShelf.background = game.uiFactory.skin.getDrawable("panel")

        // Process any shelf population that was waiting for assets
        pendingTacticalSlots?.let { populateSlotShelf(it) }
        pendingTacticalSlots = null
    }

    /**
     * Links the HUD to a specific ship entity.
     * In Epoch 3, this can be called repeatedly to switch 'Remote Ship Command' focus.
     */
    fun linkPlayer(player: Entity) {
        val ship = player[ShipComponent.mapper] ?: return
        playerShip = ship

        // 1. Navigation Matrix: Bind to the primary engine slot
        ship.getSlot(ModuleCategory.ENGINE)?.let {
            navMatrix.selectModuleFromSlot(it)
        }

        // 2. Tactical Matrix: Filter for non-nav modules
        val tacticalSlots = ship.slots.filter {
            it.category != ModuleCategory.ENGINE && it.category != ModuleCategory.NAVIGATION
        }

        if (game.uiFactory.isLoaded) {
            populateSlotShelf(tacticalSlots)
        } else {
            pendingTacticalSlots = tacticalSlots
        }

        // 3. Default to the first tactical slot
        tacticalSlots.firstOrNull()?.let { tacticalMatrix.selectModuleFromSlot(it) }
    }

    /**
     * Populates the right-hand selection shelf with buttons for each module/slot.
     */
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

        // Populate shelf from right to left
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

    /**
     * Orchestrates the precise placement of UI components based on screen dimensions.
     * Logic: Anchors elements to corners with safe-area offsets.
     */
    private fun layout() {
        val sWidth = stage.width
        val sHeight = stage.height
        if (sWidth <= 10f || sHeight <= 10f) return // Prevent layout on uninitialized viewport

        // Top-Left: Nautilus (Vitals)
        nautilus.setPosition(20f, sHeight - nautilus.height - 20f)

        // Top-Right: Radar
        radar.setPosition(sWidth - radar.width - 20f, sHeight - radar.height - 20f)

        // Bottom-Left: Navigation Matrix
        navMatrix.setPosition(20f, 20f)

        // Bottom-Right: Tactical Shelf (Selection)
        rightCategoryShelf.setPosition(sWidth - rightCategoryShelf.width - 20f, 20f)

        // Above Shelf: Tactical Matrix (Controls)
        tacticalMatrix.setPosition(
            sWidth - tacticalMatrix.width - 20f,
            rightCategoryShelf.y + rightCategoryShelf.height + 10f
        )
    }

    fun update(delta: Float) {
        stage.act(delta)
        navigationOutput.set(navMatrix.getNavigationOutput())
    }

    fun draw() {
        // Only draw and layout if we have a valid screen area
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
