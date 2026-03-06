package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.ui.*
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

    enum class State { WELCOME, WEAPON_MARKET, DRONE_MARKET }
    private var currentState = State.WELCOME

    // High-level UI components for each state
    private val shipAnalysisTable = ShipAnalysisTable(skin, drawer, world) { action ->
        handleAnalysisAction(action)
    }

    private var marketTable: MarketTable? = null
    private val titleLabel = Label("", skin, "title")

    init {
        setTransform(true)
        background = skin.getDrawable("panel")
        pad(20f)
        refreshState()
    }

    private fun handleAnalysisAction(action: ShipAnalysisTable.Action) {
        when (action) {
            ShipAnalysisTable.Action.REPAIR -> {
                world.family { all(PlayerComponent, HealthComponent, ShieldComponent) }.firstOrNull()?.let { p ->
                    with(world) {
                        p[HealthComponent].current = p[HealthComponent].max
                        p[ShieldComponent].current = p[ShieldComponent].max
                    }
                    shipAnalysisTable.refresh()
                }
            }
            ShipAnalysisTable.Action.UNDOCK -> {
                dockingManager.undock()
            }
            ShipAnalysisTable.Action.GOTO_WEAPONS -> {
                currentState = State.WEAPON_MARKET
                refreshState()
            }
            ShipAnalysisTable.Action.GOTO_DRONES -> {
                // Placeholder for when drone market is implemented
                currentState = State.DRONE_MARKET
                refreshState()
            }
        }
    }

    fun refreshState() {
        clearChildren()

        // Update Title based on station
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
        add(titleLabel).padBottom(15f).row()

        when (currentState) {
            State.WELCOME -> {
                shipAnalysisTable.refresh()
                add(shipAnalysisTable).expand().fill()
            }
            State.WEAPON_MARKET -> {
                marketTable = MarketTable(skin, drawer, world, marketManager) {
                    currentState = State.WELCOME
                    refreshState()
                }
                add(marketTable).expand().fill()
            }
            State.DRONE_MARKET -> {
                // Placeholder for Drone Market UI
                val placeholder = Table(skin)
                placeholder.add(Label("DRONE MARKET - COMING SOON", skin, "title")).row()
                val backBtn = UIButtonFactory.createButton(skin, "BACK TO COMMAND") {
                    currentState = State.WELCOME
                    refreshState()
                }
                placeholder.add(backBtn).size(400f, 80f).padTop(50f)
                add(placeholder).expand().fill()
            }
        }
    }

    fun resetToWelcome() {
        currentState = State.WELCOME
        refreshState()
    }
}
