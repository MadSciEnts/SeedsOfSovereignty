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
    private val onExit: () -> Unit
) : Table(skin) {

    private val equippedDetails = WeaponDetailsWidget(skin, drawer, "EQUIPPED MODULE")
    private val offeredDetails = WeaponDetailsWidget(skin, drawer, "STATION OFFER")

    private val catalogScrollTable = Table(skin)
    private val catalogScrollPane = ScrollPane(catalogScrollTable, skin)
    private var selectedModuleIndex = 0
    private var selectedOfferButton: Actor? = null

    private val tempVec1 = Vector2()
    private val tempVec2 = Vector2()

    init {
        setTransform(true)
        setupLayout()
        refreshCatalog()
    }

    private fun setupLayout() {
        val centerColumn = Table(skin)
        centerColumn.add(Label("STATION DATA LINK", skin)).padBottom(5f).row()
        centerColumn.add(catalogScrollPane).width(450f).height(350f).row()

        val swapBtn = UIButtonFactory.createButton(skin, "CONFIRM SWAP", UIButtonFactory.ButtonType.MARKET) {
            val player = world.family { all(PlayerComponent, DockingComponent) }.first()
            val station = with(world) { player[DockingComponent].nearestStation!! }

            // Map the relative UI selection index back to the absolute station inventory index
            val playerTier = with(world) { player[EvolutionComponent].currentTier }
            val market = with(world) { station[MarketComponent] }
            val visibleOffers = market.weaponOffers.filter { it.projectileTier == playerTier }

            if (selectedModuleIndex in visibleOffers.indices) {
                val targetModule = visibleOffers[selectedModuleIndex]
                val absoluteIndex = market.weaponOffers.indexOf(targetModule)
                marketManager.swapModule(player, station, absoluteIndex)
                refreshCatalog()
            }
        }
        centerColumn.add(swapBtn).size(450f, 90f).padTop(20f).row()

        val exitBtn = UIButtonFactory.createButton(skin, "EXIT CATEGORY", UIButtonFactory.ButtonType.GENERIC) {
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

        val player = world.family { all(PlayerComponent, DockingComponent, EvolutionComponent) }.first()
        val playerTier = with(world) { player[EvolutionComponent].currentTier }
        val station = with(world) { player[DockingComponent].nearestStation ?: return }
        val market = with(world) { if (station.has(MarketComponent)) station[MarketComponent] else null } ?: return

        // ENFORCE RULE #1: Filter for compatible modules
        var visibleOffers = market.weaponOffers.filter { it.projectileTier == playerTier }

        // Dynamic Restocking: If no compatible items (like evolving at the Alpha Outpost)
        if (visibleOffers.isEmpty()) {
            market.weaponOffers.clear() // Clear old tier items
            for (i in 0 until 10) {
                market.weaponOffers.add(ModuleGenerator.generateWeapon(playerTier))
            }
            visibleOffers = market.weaponOffers
        }

        visibleOffers.forEachIndexed { index, offer ->
            val type = if (index == selectedModuleIndex) UIButtonFactory.ButtonType.SELECTED else UIButtonFactory.ButtonType.MODULE
            val icon = ProjectileIcon(drawer, offer.projectileTier, 1.5f, offer.projectileColor)

            val offerBtn = UIButtonFactory.createButton(skin, "${offer.name} [T${offer.projectileTier}]", type, icon) {
                selectedModuleIndex = index
                refreshCatalog()
            }

            if (index == selectedModuleIndex) selectedOfferButton = offerBtn
            catalogScrollTable.add(offerBtn).size(420f, 75f).padBottom(8f).row()
        }

        // Ensure index stays in bounds after a refresh/filter
        if (selectedModuleIndex >= visibleOffers.size) selectedModuleIndex = 0

        updateSymmetry(player, station, visibleOffers)
    }

    private fun updateSymmetry(player: com.github.quillraven.fleks.Entity, station: com.github.quillraven.fleks.Entity, visibleOffers: List<WeaponModuleComponent>) {
        val currentModule = with(world) { player[WeaponModuleComponent] }
        equippedDetails.update(currentModule)

        val selectedOffer = if (selectedModuleIndex >= 0 && selectedModuleIndex < visibleOffers.size) {
            visibleOffers[selectedModuleIndex]
        } else null
        offeredDetails.update(selectedOffer)
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
