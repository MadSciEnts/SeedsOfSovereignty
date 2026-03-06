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

    enum class State { WELCOME, WEAPON_MARKET, DRONE_MARKET, CHOICE_MODAL }
    private var currentState = State.WELCOME

    private val shipAnalysisTable = ShipAnalysisTable(skin, drawer, world) { action ->
        handleAnalysisAction(action)
    }

    private var marketTable: MarketTable? = null
    private var choicePopup: ChoicePopup? = null
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

        // 1. Contextual Header: Only show Station Info if NOT in a critical choice modal
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

        // 2. State Resolution
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
                val placeholder = Table(skin)
                placeholder.add(Label("DRONE MARKET - COMING SOON", skin, "title")).row()
                val backBtn = UIButtonFactory.createButton(skin, "BACK TO COMMAND") {
                    currentState = State.WELCOME
                    refreshState()
                }
                placeholder.add(backBtn).size(400f, 80f).padTop(50f)
                add(placeholder).expand().fill()
            }
            State.CHOICE_MODAL -> {
                val player = world.family { all(PlayerComponent, ChoiceComponent, WeaponModuleComponent, EvolutionComponent) }.first()
                with(world) {
                    val choice = player[ChoiceComponent]
                    val evo = player[EvolutionComponent]

                    choicePopup = ChoicePopup(
                        skin, drawer,
                        choice.choiceType,
                        "TIER ${evo.currentTier} -> ${evo.currentTier + 1}",
                        player[WeaponModuleComponent],
                        choice.pendingModule!!,
                        onKeep = {
                            evo.currentTier++
                            evo.subTier = 1
                            choice.isAwaitingChoice = false
                            currentState = State.WELCOME
                            refreshState()
                        },
                        onSwap = {
                            val current = player[WeaponModuleComponent]
                            val pending = choice.pendingModule!!
                            current.name = pending.name
                            current.description = pending.description
                            current.projectileTier = pending.projectileTier
                            current.rarity = pending.rarity
                            current.fireRate = pending.fireRate
                            current.projectileSpeed = pending.projectileSpeed
                            current.projectileColor = pending.projectileColor
                            current.effects.clear()
                            current.effects.addAll(pending.effects)

                            evo.currentTier++
                            evo.subTier = 1
                            if (player.has(ModuleSlotComponent)) {
                                player[ModuleSlotComponent].slots.find { it.type == SlotType.WEAPON }?.let { weaponSlot ->
                                    weaponSlot.equippedName = current.name
                                    weaponSlot.equippedTier = current.projectileTier
                                }
                            }

                            choice.isAwaitingChoice = false
                            currentState = State.WELCOME
                            refreshState()
                        }
                    )
                }
                add(choicePopup).expand().center()
            }
        }
    }

    fun resetToWelcome() {
        currentState = State.WELCOME
        refreshState()
    }
}
