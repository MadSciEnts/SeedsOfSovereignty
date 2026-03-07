package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.DockingManager
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.util.ModuleGenerator
import space.earlygrey.shapedrawer.ShapeDrawer

class StationHangarUI(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val world: World,
    private val dockingManager: DockingManager,
    private val marketManager: MarketManager
) : Table(skin) {

    enum class State { WELCOME, WEAPON_MARKET, DRONE_MARKET, CHOICE_MODAL }
    private var currentState = State.WELCOME

    private val shipAnalysisTable = ShipAnalysisTable(skin, drawer, world) { action ->
        handleAnalysisAction(action)
    }

    private var marketTable: MarketTable? = null
    private var reportPopup: EvolutionReportPopup? = null
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
            ShipAnalysisTable.Action.UNDOCK -> dockingManager.undock()
            ShipAnalysisTable.Action.GOTO_WEAPONS -> {
                currentState = State.WEAPON_MARKET
                refreshState()
            }
            ShipAnalysisTable.Action.GOTO_DRONES -> {
                currentState = State.DRONE_MARKET
                refreshState()
            }
        }
    }

    override fun act(delta: Float) {
        super.act(delta)

        val player = world.family { all(PlayerComponent, ChoiceComponent) }.firstOrNull()
        if (player != null) {
            with(world) {
                if (player[ChoiceComponent].isAwaitingChoice && currentState != State.CHOICE_MODAL) {
                    currentState = State.CHOICE_MODAL
                    refreshState()
                }
            }
        }
    }

    fun refreshState() {
        clearChildren()

        val isChoiceActive = currentState == State.CHOICE_MODAL

        if (!isChoiceActive) {
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
        }

        when (currentState) {
            State.WELCOME -> {
                shipAnalysisTable.refresh()
                add(shipAnalysisTable).expand().fill()
            }
            State.WEAPON_MARKET -> {
                marketTable = MarketTable(skin, drawer, world, marketManager, SlotType.WEAPON) {
                    currentState = State.WELCOME
                    refreshState()
                }
                add(marketTable).expand().fill()
            }
            State.DRONE_MARKET -> {
                marketTable = MarketTable(skin, drawer, world, marketManager, SlotType.DRONE) {
                    currentState = State.WELCOME
                    refreshState()
                }
                add(marketTable).expand().fill()
            }
            State.CHOICE_MODAL -> {
                val player = world.family { all(PlayerComponent, ChoiceComponent, WeaponModuleComponent, EvolutionComponent) }.first()
                with(world) {
                    val choice = player[ChoiceComponent]
                    val evo = player[EvolutionComponent]

                    val droneModule = if (player.has(ModuleSlotComponent)) {
                        val dSlot = player[ModuleSlotComponent].slots.find { it.type == SlotType.DRONE }
                        dSlot?.slotEntity?.let { if (it.has(DroneModuleComponent)) it[DroneModuleComponent] else null }
                    } else null

                    reportPopup = EvolutionReportPopup(
                        skin, drawer,
                        evo.currentTier,
                        player[WeaponModuleComponent],
                        droneModule,
                        onConfirm = {
                            choice.isAwaitingChoice = false
                            currentState = State.WELCOME
                            refreshState()
                        }
                    )
                }
                add(reportPopup).expand().center()
            }
        }
    }

    fun resetToWelcome() {
        currentState = State.WELCOME
        refreshState()
    }
}
