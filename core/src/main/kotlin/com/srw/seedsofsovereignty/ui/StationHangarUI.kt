package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.MarketManager
import space.earlygrey.shapedrawer.ShapeDrawer

class StationHangarUI(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val world: World,
    private val dockingManager: DockingManager,
    private val marketManager: MarketManager
) : Table(skin) {

    enum class State { WELCOME, CATEGORIES, WEAPON_MARKET }
    private var currentState = State.WELCOME

    private val shipPreview = ShipPreviewWidget(drawer)
    private val welcomeTable = Table(skin)
    private val categoriesTable = Table(skin)
    private var marketTable: MarketTable? = null

    private val titleLabel = Label("", skin, "title")
    private val hullStatusLabel = Label("", skin)
    private val shieldStatusLabel = Label("", skin)
    private val evoProgressLabel = Label("", skin)

    init {
        setTransform(true)
        background = skin.getDrawable("panel")
        pad(20f)

        setupWelcome()
        setupCategories()

        refreshState()
    }

    private fun setupWelcome() {
        welcomeTable.clear()
        val buttonTable = Table(skin)
        val repairBtn = TextButton("REPAIR HULL", skin, "scifi")
        val marketBtn = TextButton("STATION MARKET", skin, "scifi")
        val undockBtn = TextButton("UNDOCK", skin, "scifi")

        repairBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                world.family { all(PlayerComponent, HealthComponent, ShieldComponent) }.firstOrNull()?.let { p ->
                    with(world) {
                        p[HealthComponent].current = p[HealthComponent].max
                        p[ShieldComponent].current = p[ShieldComponent].max
                    }
                    updateStatsLabels()
                }
            }
        })

        marketBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentState = State.CATEGORIES
                refreshState()
            }
        })

        undockBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                dockingManager.undock()
            }
        })

        buttonTable.add(repairBtn).size(350f, 80f).pad(10f).row()
        buttonTable.add(marketBtn).size(350f, 80f).pad(10f).row()
        buttonTable.add(undockBtn).size(350f, 80f).pad(10f).row()

        val shipInfoTable = Table(skin)
        shipInfoTable.add(Label("VESSEL CONFIGURATION", skin)).padBottom(10f).row()
        shipInfoTable.add(shipPreview).size(300f).padBottom(10f).row()

        val statsTable = Table(skin)
        statsTable.add(Label("HULL:", skin)).left().padRight(15f)
        statsTable.add(hullStatusLabel).right().row()
        statsTable.add(Label("SHIELD:", skin)).left().padRight(15f)
        statsTable.add(shieldStatusLabel).right().row()
        statsTable.add(Label("EVO:", skin)).left().padRight(15f)
        statsTable.add(evoProgressLabel).right().row()

        shipInfoTable.add(statsTable).fillX()

        welcomeTable.add(buttonTable).expand().left()
        welcomeTable.add(shipInfoTable).expand().right()
    }

    private fun setupCategories() {
        categoriesTable.clear()
        val weaponsBtn = TextButton("WEAPONRY SUBSYSTEMS", skin, "scifi")
        val backBtn = TextButton("BACK TO COMMAND", skin, "scifi")

        weaponsBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentState = State.WEAPON_MARKET
                refreshState()
            }
        })

        backBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                currentState = State.WELCOME
                refreshState()
            }
        })

        categoriesTable.add(Label("SELECT MODULE CATEGORY", skin)).padBottom(30f).row()
        categoriesTable.add(weaponsBtn).size(500f, 90f).pad(15f).row()
        categoriesTable.add(backBtn).size(500f, 90f).pad(15f).row()
    }

    private fun updateStatsLabels() {
        world.family { all(PlayerComponent, HealthComponent, ShieldComponent, EvolutionComponent) }.firstOrNull()?.let { player ->
            with(world) {
                val hp = player[HealthComponent]; val sp = player[ShieldComponent]; val evo = player[EvolutionComponent]
                hullStatusLabel.setText("${(hp.current / hp.max * 100).toInt()}%")
                shieldStatusLabel.setText("${(sp.current / sp.max * 100).toInt()}%")
                evoProgressLabel.setText("T${evo.currentTier} [${evo.essenceCollected}/${evo.essenceRequired}]")
                shipPreview.tier = evo.currentTier
                shipPreview.subTier = evo.subTier
            }
        }
    }

    fun refreshState() {
        clearChildren()

        world.family { all(PlayerComponent, DockingComponent) }.firstOrNull()?.let { player ->
            with(world) {
                val station = player[DockingComponent].nearestStation
                if (station != null && station.has(StationComponent)) {
                    titleLabel.setText("DATA-LINK ACTIVE: ${station[StationComponent].name.uppercase()}")
                } else {
                    titleLabel.setText("NO CONNECTION")
                }
            }
        }

        updateStatsLabels()
        add(titleLabel).padBottom(15f).row()

        when (currentState) {
            State.WELCOME -> add(welcomeTable).expand().fill()
            State.CATEGORIES -> add(categoriesTable).expand().fill()
            State.WEAPON_MARKET -> {
                marketTable = MarketTable(skin, drawer, world, marketManager) {
                    currentState = State.CATEGORIES
                    refreshState()
                }
                add(marketTable).expand().fill()
            }
        }
    }

    fun resetToWelcome() {
        currentState = State.WELCOME
        refreshState()
    }
}
