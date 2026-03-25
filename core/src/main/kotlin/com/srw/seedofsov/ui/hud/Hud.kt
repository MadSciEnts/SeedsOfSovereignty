package com.srw.seedofsov.ui.hud

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
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
 * Truth on Disk: Bridges tactical selection state to ECS systems and handles Debug access.
 * Implements two-tier Command Matrix selection: Category Shelf (Bottom) -> Module Shelf (Top).
 */
class Hud(val game: SovereigntyGame) : Disposable, UiAssetListener {
    val stage = Stage(ScreenViewport(), game.batch)
    val navigationOutput = Vector2()

    private val navMatrix = CommandMatrix(game, isLeftAligned = true)
    private val tacticalMatrix = CommandMatrix(game, isLeftAligned = false)
    private val categoryShelf = Panel()
    private val moduleShelf = Panel()
    private val debugDialog = DebugDialog(game)
    private var debugBtn: TextButton? = null

    // Technical FX Layer for ribbons, sparks, and energy
    val fxLayer = HudFxLayer()

    // Technical log overlay
    private val combatLogPanel = Panel()
    private val logLabels = mutableListOf<Label>()

    private var playerShip: Entity? = null
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
        // Truth on Disk: Matrices go first (Bottom)
        stage.addActor(navMatrix)
        stage.addActor(tacticalMatrix)

        // FX Layer goes ON TOP of matrices but below dialogs
        stage.addActor(fxLayer)

        stage.addActor(categoryShelf)
        stage.addActor(moduleShelf)
        stage.addActor(debugDialog)

        combatLogPanel.setSize(600f, 250f)
        stage.addActor(combatLogPanel)

        game.uiFactory.addListener(this)
    }

    override fun onAssetsReady() {
        categoryShelf.background = game.uiFactory.skin.getDrawable("panel")
        moduleShelf.background = game.uiFactory.skin.getDrawable("panel")

        // Populate log labels with increased scale for readability
        repeat(5) {
            val label = game.uiFactory.createLabel("").apply {
                color = Color.GREEN
                setFontScale(1.5f)
            }
            logLabels.add(label)
            combatLogPanel.addActor(label)
        }

        debugBtn = game.uiFactory.createButton("DEBUG").apply {
            setSize(120f, 60f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    debugDialog.toggle()
                }
            })
        }
        stage.addActor(debugBtn)

        pendingTacticalSlots?.let { populateCategoryShelf(it) }
        pendingTacticalSlots = null
        layout()
    }

    fun getTacticalActiveSlot(): ModuleSlot? = tacticalMatrix.activeSlot

    fun linkPlayer(player: Entity) {
        playerShip = player
        val ship = player[ShipComponent.mapper] ?: return
        debugDialog.linkPlayer(player)

        ship.getSlot(ModuleCategory.ENGINE)?.let {
            navMatrix.selectModuleFromSlot(it)
        }

        val tacticalSlots = ship.slots.filter {
            it.category != ModuleCategory.ENGINE && it.category != ModuleCategory.NAVIGATION
        }

        if (game.uiFactory.isLoaded) {
            populateCategoryShelf(tacticalSlots)
        } else {
            pendingTacticalSlots = tacticalSlots
        }

        // Default selection: First available tactical category
        tacticalSlots.firstOrNull()?.let {
            selectCategory(it.category, tacticalSlots)
            tacticalMatrix.selectModuleFromSlot(it)
        }
    }

    private fun populateCategoryShelf(slots: List<ModuleSlot>) {
        if (!game.uiFactory.isLoaded) {
            pendingTacticalSlots = slots
            return
        }

        categoryShelf.clearChildren()
        val categories = slots.map { it.category }.distinct()
        val btnWidth = 200f
        val btnHeight = 60f
        val spacing = 10f
        val shelfWidth = (categories.size * (btnWidth + spacing)) + 20f
        categoryShelf.setSize(shelfWidth, 100f)

        var currentX = categoryShelf.width - btnWidth - 10f
        categories.forEach { category ->
            val btn = game.uiFactory.createButton(category.displayName).apply {
                setSize(btnWidth, btnHeight)
                setPosition(currentX, 20f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        selectCategory(category, slots)
                    }
                })
            }
            categoryShelf.addActor(btn)
            currentX -= (btnWidth + spacing)
        }
        layout()
    }

    private fun selectCategory(category: ModuleCategory, allSlots: List<ModuleSlot>) {
        val filteredSlots = allSlots.filter { it.category == category }
        populateModuleShelf(filteredSlots)

        // Truth on Disk: If only 1 module in category, auto-select it.
        if (filteredSlots.size == 1) {
            tacticalMatrix.selectModuleFromSlot(filteredSlots[0])
        }
    }

    private fun populateModuleShelf(slots: List<ModuleSlot>) {
        if (!game.uiFactory.isLoaded) return

        moduleShelf.clearChildren()
        val btnWidth = 220f
        val btnHeight = 70f
        val spacing = 10f
        val shelfWidth = (slots.size * (btnWidth + spacing)) + 20f
        moduleShelf.setSize(shelfWidth, 110f)

        var currentX = moduleShelf.width - btnWidth - 10f
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
            moduleShelf.addActor(btn)
            currentX -= (btnWidth + spacing)
        }
        layout()
    }

    private fun layout() {
        val sWidth = stage.width
        val sHeight = stage.height
        if (sWidth <= 10f || sHeight <= 10f) return

        fxLayer.setSize(sWidth, sHeight)

        nautilus.setPosition(20f, sHeight - nautilus.height - 20f)
        radar.setPosition(sWidth - radar.width - 20f, sHeight - radar.height - 20f)
        navMatrix.setPosition(20f, 20f)

        categoryShelf.setPosition(sWidth - categoryShelf.width - 20f, 20f)
        tacticalMatrix.setPosition(sWidth - tacticalMatrix.width - 20f, categoryShelf.y + categoryShelf.height + 10f)
        moduleShelf.setPosition(sWidth - moduleShelf.width - 20f, tacticalMatrix.y + tacticalMatrix.height + 10f)

        debugBtn?.let { btn ->
            btn.setPosition(sWidth / 2f - btn.width / 2f, sHeight - btn.height - 20f)
            btn.toFront()
        }

        combatLogPanel.setPosition(20f, sHeight - 450f)
        logLabels.forEachIndexed { index, label ->
            label.setPosition(0f, 200f - (index * 40f))
        }

        debugDialog.toFront()
        debugDialog.setPosition((sWidth - debugDialog.width) / 2f, (sHeight - debugDialog.height) / 2f)

        // Truth on Disk: FX Layer must be above matrices but below handle interaction
        fxLayer.toFront()
        debugDialog.toFront()
        debugBtn?.toFront()
    }

    fun update(delta: Float) {
        stage.act(delta)
        navigationOutput.set(navMatrix.getNavigationOutput())

        playerShip?.get(CombatLogComponent.mapper)?.let { log ->
            log.events.forEachIndexed { index, event ->
                if (index < logLabels.size) {
                    logLabels[index].setText(event)
                }
            }
        }
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
        fxLayer.dispose()
        stage.dispose()
    }
}
