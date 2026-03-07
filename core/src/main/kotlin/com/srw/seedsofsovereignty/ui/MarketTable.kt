package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.MarketManager
import com.srw.seedsofsovereignty.util.ModuleGenerator
import space.earlygrey.shapedrawer.ShapeDrawer

class MarketTable(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val world: World,
    private val marketManager: MarketManager,
    private val category: SlotType,
    private val onExit: () -> Unit
) : Table(skin) {

    private val equippedDetails = WeaponDetailsWidget(skin, drawer, "EQUIPPED MODULE")
    private val offeredDetails = WeaponDetailsWidget(skin, drawer, "STATION OFFER")

    private val catalogScrollTable = Table(skin)
    private val catalogScrollPane = ScrollPane(catalogScrollTable, skin)
    private var selectedModuleIndex = 0
    private var selectedOfferButton: Actor? = null

    // Track visible items to map indices correctly
    private val visibleWeaponOffers = mutableListOf<WeaponModuleComponent>()
    private val visibleDroneOffers = mutableListOf<DroneModuleComponent>()

    private val tempVec1 = Vector2()
    private val tempVec2 = Vector2()

    init {
        setTransform(true)
        setupLayout()
        refreshCatalog()
    }

    private fun setupLayout() {
        val centerColumn = Table(skin)
        centerColumn.add(Label("STATION DATA LINK: ${category.name}", skin)).padBottom(5f).row()
        centerColumn.add(catalogScrollPane).width(450f).height(350f).row()

        val swapBtn = UIButtonFactory.createButton(skin, "CONFIRM SWAP", UIButtonFactory.ButtonType.MARKET) {
            val player = world.family { all(PlayerComponent, DockingComponent) }.first()
            val station = with(world) { player[DockingComponent].nearestStation!! }
            val market = with(world) { station[MarketComponent] }

            if (category == SlotType.WEAPON) {
                if (selectedModuleIndex in visibleWeaponOffers.indices) {
                    val absIndex = market.weaponOffers.indexOf(visibleWeaponOffers[selectedModuleIndex])
                    marketManager.swapModule(player, station, absIndex)
                }
            } else {
                if (selectedModuleIndex in visibleDroneOffers.indices) {
                    val absIndex = market.droneOffers.indexOf(visibleDroneOffers[selectedModuleIndex])
                    // We target the first drone slot for this market view
                    marketManager.swapDroneModule(player, station, absIndex, 0)
                }
            }
            refreshCatalog()
        }
        centerColumn.add(swapBtn).size(450f, 90f).padTop(20f).row()

        val exitBtn = UIButtonFactory.createButton(skin, "BACK TO ANALYSIS", UIButtonFactory.ButtonType.GENERIC) {
            onExit()
        }
        centerColumn.add(exitBtn).size(450f, 70f).padTop(10f)

        add(equippedDetails).uniform().expand().fill().pad(10f)
        add(centerColumn).uniform().expand().fill().pad(10f)
        add(offeredDetails).uniform().expand().fill().pad(10f)
    }

    fun refreshCatalog() {
        catalogScrollTable.clear()
        selectedOfferButton = null
        visibleWeaponOffers.clear()
        visibleDroneOffers.clear()

        val player = world.family { all(PlayerComponent, DockingComponent, EvolutionComponent) }.first()
        val playerTier = with(world) { player[EvolutionComponent].currentTier }
        val station = with(world) { player[DockingComponent].nearestStation ?: return }
        val market = with(world) { if (station.has(MarketComponent)) station[MarketComponent] else null } ?: return

        // Task Fix: Restock category if no compatible items exist
        val compatibleWeapons = market.weaponOffers.filter { it.projectileTier == playerTier }
        val compatibleDrones = market.droneOffers.filter { it.weaponDNA.projectileTier == playerTier }

        if ((category == SlotType.WEAPON && compatibleWeapons.isEmpty()) ||
            (category == SlotType.DRONE && compatibleDrones.isEmpty())) {
            // Partial restock based on category need
            if (category == SlotType.WEAPON) market.weaponOffers.clear() else market.droneOffers.clear()
            for (i in 0 until 10) {
                if (category == SlotType.WEAPON) market.weaponOffers.add(ModuleGenerator.generateWeapon(playerTier))
                else market.droneOffers.add(ModuleGenerator.generateDroneModule(playerTier))
            }
        }

        // Build filtered list and populate UI
        if (category == SlotType.WEAPON) {
            visibleWeaponOffers.addAll(market.weaponOffers.filter { it.projectileTier == playerTier })
            visibleWeaponOffers.forEachIndexed { index, offer ->
                addModuleButton(offer.name, offer.projectileTier, offer.projectileColor, index)
            }
        } else {
            visibleDroneOffers.addAll(market.droneOffers.filter { it.weaponDNA.projectileTier == playerTier })
            visibleDroneOffers.forEachIndexed { index, offer ->
                addModuleButton(offer.name, offer.weaponDNA.projectileTier, offer.weaponDNA.projectileColor, index)
            }
        }

        if (selectedModuleIndex >= (if(category == SlotType.WEAPON) visibleWeaponOffers.size else visibleDroneOffers.size)) selectedModuleIndex = 0
        updateSymmetry(player, station)
    }

    private fun addModuleButton(name: String, tier: Int, color: Color, index: Int) {
        val type = if (index == selectedModuleIndex) UIButtonFactory.ButtonType.SELECTED else UIButtonFactory.ButtonType.MODULE
        val icon = ProjectileIcon(drawer, skin, tier, 1.5f, color)

        val btn = UIButtonFactory.createButton(skin, "$name [T$tier]", type, icon) {
            selectedModuleIndex = index
            refreshCatalog()
        }

        if (index == selectedModuleIndex) selectedOfferButton = btn
        catalogScrollTable.add(btn).size(420f, 75f).padBottom(8f).row()
    }

    private fun updateSymmetry(player: com.github.quillraven.fleks.Entity, station: com.github.quillraven.fleks.Entity) {
        with(world) {
            if (category == SlotType.WEAPON) {
                equippedDetails.update(player[WeaponModuleComponent])
                offeredDetails.update(visibleWeaponOffers.getOrNull(selectedModuleIndex))
            } else {
                val slotComp = player[ModuleSlotComponent]
                val dSlot = slotComp.slots.filter { it.type == SlotType.DRONE }.getOrNull(0)
                dSlot?.slotEntity?.let { if (it.has(DroneModuleComponent)) equippedDetails.update(it[DroneModuleComponent].weaponDNA) }
                offeredDetails.update(visibleDroneOffers.getOrNull(selectedModuleIndex)?.weaponDNA)
            }
        }
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        super.drawChildren(batch, parentAlpha)

        if (selectedOfferButton != null) {
            val btn = selectedOfferButton!!
            if (btn.isVisible && btn.stage != null) {
                val scrollPane = catalogScrollPane
                tempVec1.set(0f, 0f)
                scrollPane.localToStageCoordinates(tempVec1)
                val clipRect = Rectangle(tempVec1.x, tempVec1.y, scrollPane.width, scrollPane.height)

                tempVec1.set(btn.width, btn.height / 2f)
                btn.localToStageCoordinates(tempVec1)

                if(clipRect.contains(tempVec1)) {
                    stageToLocalCoordinates(tempVec1)
                    tempVec2.set(0f, offeredDetails.height * 0.78f)
                    offeredDetails.localToStageCoordinates(tempVec2)
                    stageToLocalCoordinates(tempVec2)

                    drawer.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.7f * parentAlpha)
                    val startX = tempVec1.x + 5f
                    val endX = tempVec2.x - 5f
                    val midX = startX + (endX - startX) * 0.4f

                    drawer.line(startX, tempVec1.y, midX, tempVec1.y, 2f)
                    drawer.line(midX, tempVec1.y, midX, tempVec2.y, 2f)
                    drawer.line(midX, tempVec2.y, endX, tempVec2.y, 2f)

                    drawer.filledCircle(startX, tempVec1.y, 5f)
                    drawer.filledRectangle(endX - 5f, tempVec2.y - 12f, 5f, 24f)
                }
            }
        }
    }
}
